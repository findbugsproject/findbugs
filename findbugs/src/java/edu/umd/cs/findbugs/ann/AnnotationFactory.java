/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.ann;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.IndexedInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.IClassAnnotation;
import edu.umd.cs.findbugs.IFieldAnnotation;
import edu.umd.cs.findbugs.ILocalVariableAnnotation;
import edu.umd.cs.findbugs.IMethodAnnotation;
import edu.umd.cs.findbugs.ISourceLineAnnotation;
import edu.umd.cs.findbugs.LocalVariableAnnotation;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.Hierarchy;
import edu.umd.cs.findbugs.ba.JavaClassAndMethod;
import edu.umd.cs.findbugs.ba.Location;
import edu.umd.cs.findbugs.ba.OpcodeStackScanner;
import edu.umd.cs.findbugs.ba.ProgramPoint;
import edu.umd.cs.findbugs.ba.SourceInfoMap;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.ba.vna.ValueNumber;
import edu.umd.cs.findbugs.ba.vna.ValueNumberFrame;
import edu.umd.cs.findbugs.ba.vna.ValueNumberSourceInfo;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.util.ClassName;
import edu.umd.cs.findbugs.visitclass.DismantleBytecode;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
 * Helper class to create any kinds of annotations for detectors. Should be not used
 * from FB core, as usually only detectors need the code below.
 * <p>
 * Implementation notes: most of the methods here are copied from different places around
 * *Annotations classes or from BugInstance.add*Annotation*, and renamed by more or less
 * consistent shema: create + what.
 * 
 * @see DetectorUtil
 * 
 * @author Andrei
 */
public class AnnotationFactory {

	/**
	 * Add a method annotation for the method which is called by given
	 * instruction.
	 *
	 * @param cpg the constant pool for the method containing the call
	 * @param inv the InvokeInstruction
	 * @return this object
	 */
	public static IMethodAnnotation createCalledMethod(ConstantPoolGen cpg, InvokeInstruction inv) {
		String className = inv.getClassName(cpg);
		String methodName = inv.getMethodName(cpg);
		String methodSig = inv.getSignature(cpg);
		IMethodAnnotation annotation = createMethod(className, methodName, methodSig, inv.getOpcode() == Constants.INVOKESTATIC);
		annotation.setDescription(IMethodAnnotation.METHOD_CALLED);
		return annotation;
	}

	/**
	 * Add a method annotation for the method which has been called
	 * by the method currently being visited by given visitor.
	 * Assumes that the visitor has just looked at an invoke instruction
	 * of some kind.
	 *
	 * @param visitor the DismantleBytecode object
	 * @return this object
	 */
	public static IMethodAnnotation createCalledMethod(DismantleBytecode visitor) {
		String className = visitor.getDottedClassConstantOperand();
		String methodName = visitor.getNameConstantOperand();
		String methodSig = visitor.getSigConstantOperand();

		IMethodAnnotation annotation = createCalledMethod(className, methodName, methodSig,
				visitor.getOpcode() == Constants.INVOKESTATIC);
		return annotation;
	}

	/**
	 * Create a MethodAnnotation from a method that is not
	 * directly accessible.  We will use the repository to
	 * try to find its class in order to populate the information
	 * as fully as possible.
	 * 
	 * @param className  class containing called method
	 * @param methodName name of called method
	 * @param methodSig  signature of called method
	 * @param isStatic   true if called method is static
	 * @return the MethodAnnotation for the called method
	 */
	public static IMethodAnnotation createCalledMethod(String className, String methodName, String methodSig, boolean isStatic) {
		IMethodAnnotation methodAnnotation = createMethod(className, methodName, methodSig, isStatic);
		methodAnnotation.setDescription(IMethodAnnotation.METHOD_CALLED);
		return methodAnnotation;
	}

	/**
	 * Add a class annotation.  If this is the first class annotation added,
	 * it becomes the primary class annotation.
	 * 
	 * @param classDescriptor the class to add
	 * @return this object
	 */
	public static IClassAnnotation createClass(ClassDescriptor classDescriptor) {
		return new ClassAnnotation(classDescriptor.toDottedClassName());
	}

	/**
	 * Add a class annotation.  If this is the first class annotation added,
	 * it becomes the primary class annotation.
	 *
	 * @param className the name of the class
	 * @return this object
	 */
	public static IClassAnnotation createClass(@DottedClassName String className) {
		return new ClassAnnotation(ClassName.toDottedClassName(className));
	}

	/**
	 * Add a field annotation for the field which is being visited by
	 * given visitor.
	 *
	 * @param visitor the visitor
	 * @return this object
	 */
	public static IFieldAnnotation createField(PreorderVisitor visitor) {
		return new FieldAnnotation(visitor.getDottedClassName(),
				visitor.getFieldName(), visitor.getFieldSig(),
				visitor.getFieldIsStatic());
	}

	/**
	 * Add a field annotation for an XField.
	 *
	 * @param xfield the XField, must be not null
	 * @return this object
	 */
	public static IFieldAnnotation createField(@NonNull XField xfield) {
		return new FieldAnnotation(
				xfield.getClassName(),
				xfield.getName(),
				xfield.getSignature(),
				xfield.getSourceSignature(),
				xfield.isStatic());
	}

	public static @CheckForNull BugAnnotation createFieldOrMethodValueSource(@CheckForNull OpcodeStack.Item item) {
		if (item == null) {
			return null;
		}
		XField xField = item.getXField();
		if (xField != null) {
			IFieldAnnotation a = createField(xField);
			a.setDescription(IFieldAnnotation.LOADED_FROM_ROLE);
			return a;
		}

		XMethod xMethod = item.getReturnValueOf();
		if (xMethod != null) {
			IMethodAnnotation a = createMethod(xMethod);
			a.setDescription(IMethodAnnotation.METHOD_RETURN_VALUE_OF);
			return a;
		}
		return null;
	}

	/**
	 * Add a method annotation.  If this is the first method annotation added,
	 * it becomes the primary method annotation.
	 * If the method has source line information, then a SourceLineAnnotation
	 * is added to the method.
	 *
	 * @param javaClass the class the method is defined in
	 * @param method    the method
	 * @return this object
	 */
	public static IMethodAnnotation createMethod(JavaClass javaClass, Method method) {
		IMethodAnnotation methodAnnotation =
			new MethodAnnotation(javaClass.getClassName(), method.getName(), method.getSignature(), method.isStatic());
		ISourceLineAnnotation methodSourceLines = forEntireMethod(javaClass, method);
		methodAnnotation.setSourceLines(methodSourceLines);
		return methodAnnotation;
	}

	/**
	 * Add a method annotation.  If this is the first method annotation added,
	 * it becomes the primary method annotation.
	 * If the method has source line information, then a SourceLineAnnotation
	 * is added to the method.
	 *
	 * @param methodGen  the MethodGen object for the method
	 * @param sourceFile source file method is defined in
	 * @return this object
	 */
	public static IMethodAnnotation createMethod(MethodGen methodGen, String sourceFile) {
		String className = methodGen.getClassName();
		IMethodAnnotation methodAnnotation =
			new MethodAnnotation(className, methodGen.getName(), methodGen.getSignature(), methodGen.isStatic());

		LineNumberTable lineNumberTable = methodGen.getLineNumberTable(methodGen.getConstantPool());
		int codeSize = methodGen.getInstructionList().getLength();

		ISourceLineAnnotation lineAnnotation;
		if (lineNumberTable == null) {
			lineAnnotation = SourceLineAnnotation.createUnknown(className, sourceFile, 0, codeSize - 1);
		} else {
			lineAnnotation = forEntireMethod(className, sourceFile, lineNumberTable, codeSize);
		}
		addSourceLinesForMethod(methodAnnotation, lineAnnotation);
		return methodAnnotation;
	}

	/**
	 * Add a method annotation for the method which the given visitor is currently visiting.
	 * If the method has source line information, then a SourceLineAnnotation
	 * is added to the method.
	 *
	 * @param visitor the BetterVisitor
	 * @return this object
	 */
	public static IMethodAnnotation createMethod(PreorderVisitor visitor) {
		IMethodAnnotation methodAnnotation = new MethodAnnotation(
				visitor.getDottedClassName(),
				visitor.getMethodName(),
				visitor.getMethodSig(),
				visitor.getMethod().isStatic());

		// Try to find the source lines for the method
		ISourceLineAnnotation sourceLine = getSourceAnnotationForMethod(
				visitor.getDottedClassName(), visitor.getMethodName(), visitor.getMethodSig());
		addSourceLinesForMethod(methodAnnotation, sourceLine);
		return methodAnnotation;
	}

	/**
	 * Factory method to create the MethodAnnotation from
	 * the classname, method name, signature, etc.
	 * The method tries to look up source line information for
	 * the method.
	 * 
	 * @param className  name of the class containing the method
	 * @param methodName name of the method
	 * @param methodSig  signature of the method
	 * @param isStatic   true if the method is static, false otherwise
	 * @return the MethodAnnotation
	 */
	public static IMethodAnnotation createMethod(
			String className, String methodName, String methodSig, boolean isStatic) {

		// FIXME: would be nice to do this without using BCEL

		className = ClassName.toDottedClassName(className);

		// Create MethodAnnotation.
		// It won't have source lines yet.
		IMethodAnnotation methodAnnotation =
			new MethodAnnotation(className, methodName, methodSig, isStatic);

		ISourceLineAnnotation sourceLines = getSourceAnnotationForMethod(
				className, methodName, methodSig);

		methodAnnotation.setSourceLines(sourceLines);
		return methodAnnotation;
	}


	/**
	 * Add a method annotation.  If this is the first method annotation added,
	 * it becomes the primary method annotation.
	 *
	 * @param className  name of the class containing the method
	 * @param methodName name of the method
	 * @param methodSig  type signature of the method
	 * @param accessFlags   accessFlags for the method
	 * @return this object
	 */
	public static IMethodAnnotation createMethod(String className, String methodName, String methodSig, int accessFlags) {
		return createMethod(className, methodName, methodSig, (accessFlags & Constants.ACC_STATIC) != 0);
	}

	/**
	 * Add a MethodAnnotation from an XMethod.
	 * 
	 * @param xmethod the XMethod
	 * @return this object
	 */
	public static IMethodAnnotation createMethod(XMethod xmethod) {
		return createMethod(xmethod.getClassName(), xmethod.getName(), xmethod.getSignature(), xmethod.isStatic());
	}

	/**
	 * Get a local variable annotation describing a parameter.
	 * 
	 * @param method a Method
	 * @param local  the local variable containing the parameter
	 * @return LocalVariableAnnotation describing the parameter
	 */
	public static ILocalVariableAnnotation createParameter(Method method, int local) {
		return createVariable(method, local, 0, 0);
	}

	/**
	 * Add a field annotation for the field which has just been accessed
	 * by the method currently being visited by given visitor.
	 * Assumes that a getfield/putfield or getstatic/putstatic
	 * has just been seen.
	 *
	 * @param visitor the DismantleBytecode object
	 * @return this object
	 */
	public static IFieldAnnotation createReferencedField(DismantleBytecode visitor) {
		String className = visitor.getDottedClassConstantOperand();
		return new FieldAnnotation(className,
				visitor.getNameConstantOperand(),
				visitor.getSigConstantOperand(), visitor.getRefFieldIsStatic());
	}

	public static @CheckForNull BugAnnotation createSomeSource(ClassContext classContext, Method method, Location location, OpcodeStack stack, int stackPos) {
		int pc = location.getHandle().getPosition();

		try {
			ValueNumberFrame vnaFrame = classContext.getValueNumberDataflow(method).getFactAtLocation(location);
			if (vnaFrame.isValid()) {
				ValueNumber valueNumber = vnaFrame.getStackValue(stackPos);
				if (valueNumber.hasFlag(ValueNumber.CONSTANT_CLASS_OBJECT)) {
					return null;
				}
				BugAnnotation variableAnnotation = ValueNumberSourceInfo.findAnnotationFromValueNumber(method, location,
						valueNumber, vnaFrame, "VALUE_OF");
				if (variableAnnotation != null) {
					return variableAnnotation;
				}

			}
		} catch (DataflowAnalysisException e) {
			AnalysisContext.logError("Couldn't find value source", e);
		} catch (CFGBuilderException e) {
			AnalysisContext.logError("Couldn't find value source", e);
		}

		return createValueSource(method, stack.getStackItem(stackPos), pc);
	}

	public static BugAnnotation [] createSomeSourceForTopTwoStackValues(ClassContext classContext, Method method, Location location) {
		int pc = location.getHandle().getPosition();
		OpcodeStack stack = OpcodeStackScanner.getStackAt(classContext.getJavaClass(), method, pc);
		BugAnnotation a1 = createSomeSource(classContext, method, location, stack, 1);
		BugAnnotation a0 = createSomeSource(classContext, method, location, stack, 0);
		return new BugAnnotation[]{a0, a1};
	}

	public static @CheckForNull BugAnnotation createSourceForTopStackValue(ClassContext classContext, Method method, Location location) {
		int pc = location.getHandle().getPosition();
		OpcodeStack stack = OpcodeStackScanner.getStackAt(classContext.getJavaClass(), method, pc);
		return createSomeSource(classContext, method, location, stack, 0);
	}

	/**
	 * Add a source line annotation for instruction currently being visited
	 * by given visitor.
	 * Note that if the method does not have line number information, then
	 * unknown source line annotation will be added.
	 *
	 * @param visitor a BytecodeScanningDetector visitor that is currently visiting the instruction
	 * @return Never returns null. If we do not have line number information
	 *         for the instruction, returns unknown line annotation
	 */
	public static ISourceLineAnnotation createSourceLine(BytecodeScanningDetector visitor) {
		int pc = visitor.getPC();
		return fromVisitedInstructionRange(visitor, pc, pc);
	}

	/**
	 * Creates a source line annotation for instruction whose PC is given
	 * in the method that the given visitor is currently visiting.
	 * Note that if the method does not have line number information, then
	 * unknown source line annotation will be created.
	 *
	 * @param visitor a BytecodeScanningDetector that is currently visiting the method
	 * @param pc      bytecode offset of the instruction
	 * @return Never returns null. If we do not have line number information
	 *         for the instruction, returns unknown line annotation
	 */
	public static ISourceLineAnnotation createSourceLine(BytecodeScanningDetector visitor, int pc) {
		return fromVisitedInstructionRange(visitor, pc, pc);
	}

	/**
	 * Add source line annotation for given Location in a method.
	 * 
	 * @param classContext the ClassContext
	 * @param method       the Method
	 * @param handle       InstructionHandle of an instruction in the method
	 * @return this BugInstance
	 */
	public static ISourceLineAnnotation createSourceLine(ClassContext classContext,
			Method method, InstructionHandle handle) {
		return createSourceLine(classContext.getJavaClass(), method, handle.getPosition());
	}

	/**
	 * Create from Method and bytecode offset in a visited class.
	 * 
	 * @param jclass       JavaClass of visited class
	 * @param method       Method in visited class
	 * @param pc           bytecode offset in visited method
	 * @return SourceLineAnnotation describing visited instruction
	 */
	public static ISourceLineAnnotation createSourceLine(JavaClass jclass, Method method, int pc) {
		LineNumberTable lineNumberTable = method.getCode().getLineNumberTable();
		String className = jclass.getClassName();
		String sourceFile = jclass.getSourceFileName();
		if (lineNumberTable == null) {
			return SourceLineAnnotation.createUnknown(className, sourceFile, pc, pc);
		}

		int startLine = lineNumberTable.getSourceLine(pc);
		return new SourceLineAnnotation(className, sourceFile, startLine, startLine, pc, pc);
	}

	public static ISourceLineAnnotation createSourceLine(MethodDescriptor methodDescriptor, int position) {
		try {
			IAnalysisCache analysisCache = Global.getAnalysisCache();
			JavaClass jclass = analysisCache.getClassAnalysis(JavaClass.class, methodDescriptor.getClassDescriptor());
			Method method = analysisCache.getMethodAnalysis(Method.class, methodDescriptor);
			return createSourceLine(jclass, method, position);
		} catch (CheckedAnalysisException e) {
			return SourceLineAnnotation.createReallyUnknown(methodDescriptor.getClassDescriptor().toDottedClassName());
		}
	}

	/**
	 * Add a source line annotation for the given instruction in the given method.
	 * Note that if the method does not have line number information, then
	 * no source line annotation will be added.
	 *
	 * @param methodGen  the method being visited
	 * @param sourceFile source file the method is defined in
	 * @param handle     the InstructionHandle containing the visited instruction
	 * @return the SourceLineAnnotation, or null if we do not have line number information
	 *         for the instruction
	 */
	public static ISourceLineAnnotation createSourceLine(MethodGen methodGen, String sourceFile, @NonNull InstructionHandle handle) {
		LineNumberTable table = methodGen.getLineNumberTable(methodGen.getConstantPool());
		String className = methodGen.getClassName();

		int bytecodeOffset = handle.getPosition();

		if (table == null) {
			return SourceLineAnnotation.createUnknown(className, sourceFile, bytecodeOffset, bytecodeOffset);
		}

		int lineNumber = table.getSourceLine(handle.getPosition());
		return new SourceLineAnnotation(
				className, sourceFile, lineNumber, lineNumber, bytecodeOffset, bytecodeOffset);
	}

	/**
	 * Factory method for creating a source line annotation describing
	 * the source line numbers for a range of instruction in a method.
	 *
	 * @param methodGen the method
	 * @param start     the start instruction
	 * @param end       the end instruction (inclusive)
	 */
	public static ISourceLineAnnotation createSourceLine(
			MethodGen methodGen, String sourceFile, InstructionHandle start, InstructionHandle end) {
		LineNumberTable lineNumberTable = methodGen.getLineNumberTable(methodGen.getConstantPool());
		String className = methodGen.getClassName();

		if (lineNumberTable == null) {
			return SourceLineAnnotation.createUnknown(className, sourceFile, start.getPosition(), end.getPosition());
		}

		int startLine = lineNumberTable.getSourceLine(start.getPosition());
		int endLine = lineNumberTable.getSourceLine(end.getPosition());
		return new SourceLineAnnotation(
				className, sourceFile, startLine, endLine, start.getPosition(), end.getPosition());
	}

	/**
	 * Add a source line annotation for given program point
	 * Note that if the method does not have line number information, then
	 * unknown source line annotation will be added.
	 *
	 * @param p program point, not null
	 * @return Never returns null. If we do not have line number information
	 *         for the instruction, returns unknown line annotation
	 */
	public static ISourceLineAnnotation createSourceLine(ProgramPoint p) {
		return createSourceLine(p.method.getMethodDescriptor(), p.pc);
	}

	/**
	 * Add a source line annotation describing the
	 * source line numbers for a range of instructions in the method being
	 * visited by the given visitor.
	 * Note that if the method does not have line number information, then
	 * unknown source line annotation will be added.
	 *
	 * @param visitor a BetterVisitor which is visiting the method
	 * @param startPC the bytecode offset of the start instruction in the range
	 * @param endPC   the bytecode offset of the end instruction in the range
	 * @return Never returns null. If we do not have line number information
	 *         for the instruction, returns unknown line annotation
	 */
	public static ISourceLineAnnotation createSourceLineRange(PreorderVisitor visitor, int startPC, int endPC) {
		return fromVisitedInstructionRange(visitor, startPC, endPC);
	}

	public static @CheckForNull BugAnnotation createValueSource(Method method, OpcodeStack.Item item, int pc) {
		ILocalVariableAnnotation lv = createVariable(method, item, pc);
		if (lv != null && lv.isNamed()) {
			return lv;
		}
		return createFieldOrMethodValueSource(item);
	}

	public static @CheckForNull ILocalVariableAnnotation createVariable(
			DismantleBytecode visitor, OpcodeStack.Item item) {
		int register = item.getRegisterNumber();
		if (register < 0) {
			return null;
		}
		// TODO: Andrei: we had duplicated implementation: some used
		// visitor.getPC(), item.getPC(), some visitor.getPC()-1, visitor.getPC()
		// neither of two had any documentation...
		return createVariable(
				visitor.getMethod(), register, visitor.getPC(), item.getPC());
	}

	public static ILocalVariableAnnotation createVariable(
			Method method, int local, int position1, int position2) {

		LocalVariableTable localVariableTable = method.getLocalVariableTable();
		String localName = "?";
		if (localVariableTable != null) {
			LocalVariable lv1 = localVariableTable.getLocalVariable(local, position1);
			if (lv1 == null) {
				lv1 = localVariableTable.getLocalVariable(local, position2);
				position1 = position2;
			}
			if (lv1 != null) {
				localName = lv1.getName();
			} else {
				for (LocalVariable lv : localVariableTable.getLocalVariableTable()) {
					if (lv.getIndex() == local) {
						if (!localName.equals("?") && !localName.equals(lv.getName())) {
							// not a single consistent name
							localName = "?";
							break;
						}
						localName = lv.getName();
					}
				}
			}
		}
		LineNumberTable lineNumbers = method.getLineNumberTable();
		if (lineNumbers == null) {
			return new LocalVariableAnnotation(localName, local, position1);
		}
		int line = lineNumbers.getSourceLine(position1);
		return new LocalVariableAnnotation(localName, local, position1, line);
	}

	public static @CheckForNull ILocalVariableAnnotation createVariable(Method method, Item item, int pc) {
		int reg = item.getRegisterNumber();
		if (reg < 0) {
			return null;
		}
		return createVariable(method, reg, pc, item.getPC());
	}

	public static ILocalVariableAnnotation createVariable(
			Method method, Location location, IndexedInstruction ins) {
		int local = ins.getIndex();
		InstructionHandle handle = location.getHandle();
		int position1 = handle.getNext().getPosition();
		int position2 = handle.getPosition();
		return createVariable(method, local, position1, position2);
	}

	private static void addSourceLinesForMethod(IMethodAnnotation methodAnnotation, ISourceLineAnnotation sourceLineAnnotation) {
		if (sourceLineAnnotation != null) {
			// Note: we don't add the source line annotation directly to
			// the bug instance.  Instead, we stash it in the MethodAnnotation.
			// It is much more useful there, and it would just be distracting
			// if it were displayed in the UI, since it would compete for attention
			// with the actual bug location source line annotation (which is much
			// more important and interesting).
			methodAnnotation.setSourceLines(sourceLineAnnotation);
		}
	}

	/**
	 * Create a SourceLineAnnotation covering an entire method.
	 * 
	 * @param javaClass JavaClass containing the method
	 * @param method    the method
	 * @return a SourceLineAnnotation for the entire method
	 */
	private static ISourceLineAnnotation forEntireMethod(JavaClass javaClass, @CheckForNull Method method) {
		String sourceFile = javaClass.getSourceFileName();
		if (method == null) {
			return SourceLineAnnotation.createUnknown(javaClass.getClassName(), sourceFile);
		}
		Code code = method.getCode();
		LineNumberTable lineNumberTable = method.getLineNumberTable();
		if (code == null || lineNumberTable == null) {
			return SourceLineAnnotation.createUnknown(javaClass.getClassName(), sourceFile);
		}

		return forEntireMethod(javaClass.getClassName(), sourceFile, lineNumberTable, code.getLength());
	}

	/**
	 * Create a SourceLineAnnotation covering an entire method.
	 * 
	 * @param className       name of the class the method is in
	 * @param sourceFile      source file containing the method
	 * @param lineNumberTable the method's LineNumberTable
	 * @param codeSize        size in bytes of the method's code
	 * @return a SourceLineAnnotation covering the entire method
	 */
	private static ISourceLineAnnotation forEntireMethod(String className, String sourceFile,
			LineNumberTable lineNumberTable, int codeSize) {
		LineNumber[] table = lineNumberTable.getLineNumberTable();
		if (table != null && table.length > 0) {
			LineNumber first = table[0];
			LineNumber last = table[table.length - 1];
			return new SourceLineAnnotation(className, sourceFile, first.getLineNumber(), last.getLineNumber(),
					0, codeSize - 1);
		}
		return SourceLineAnnotation.createUnknown(className, sourceFile, 0, codeSize - 1);
	}

	/**
	 * Factory method for creating a source line annotation describing the
	 * source line numbers for a range of instructions in the method being
	 * visited by the given visitor.
	 *
	 * @param visitor a BetterVisitor which is visiting the method
	 * @param startPC the bytecode offset of the start instruction in the range
	 * @param endPC   the bytecode offset of the end instruction in the range
	 * @return Never returns null. If we do not have line number information
	 *         for the instruction, returns unknown line annotation
	 */
	private static ISourceLineAnnotation fromVisitedInstructionRange(PreorderVisitor visitor, int startPC, int endPC) {
		if (startPC > endPC) {
			throw new IllegalArgumentException("Start pc " + startPC + " greater than end pc " + endPC);
		}
		Code code = visitor.getMethod().getCode();
		LineNumberTable lineNumberTable = code != null ? code.getLineNumberTable() : null;
		String className = visitor.getDottedClassName();
		String sourceFile = visitor.getSourceFile();

		if (lineNumberTable == null) {
			return SourceLineAnnotation.createUnknown(className, sourceFile, startPC, endPC);
		}

		int startLine = lineNumberTable.getSourceLine(startPC);
		int endLine = lineNumberTable.getSourceLine(endPC);
		return new SourceLineAnnotation(className, sourceFile, startLine, endLine, startPC, endPC);
	}

	private static ISourceLineAnnotation getSourceAnnotationForMethod(
			String className, String methodName, String methodSig) {
		JavaClassAndMethod targetMethod = null;
		Code code = null;

		try {
			JavaClass targetClass = AnalysisContext.currentAnalysisContext().lookupClass(className);
			targetMethod = Hierarchy.findMethod(targetClass, methodName, methodSig);
			if (targetMethod != null) {
				Method method = targetMethod.getMethod();
				if (method != null) {
					code = method.getCode();
				}
			}

		} catch (ClassNotFoundException e) {
			AnalysisContext.reportMissingClass(e);
		}
		SourceInfoMap sourceInfoMap = AnalysisContext.currentAnalysisContext().getSourceInfoMap();
		SourceInfoMap.SourceLineRange range = sourceInfoMap.getMethodLine(className, methodName, methodSig);

		if (range != null) {
			return new SourceLineAnnotation(
					className,
					AnalysisContext.currentAnalysisContext().lookupSourceFile(className),
					range.getStart().intValue(),
					range.getEnd().intValue(),
					0,
					code == null ? -1 : code.getLength());
		}

		if (sourceInfoMap.fallBackToClassfile() && targetMethod != null) {
			return  forEntireMethod(targetMethod.getJavaClass(), targetMethod.getMethod());
		}

		// If we couldn't find the source lines,
		// create an unknown source line annotation referencing
		// the class and source file.
		String sourceFile = AnalysisContext.currentAnalysisContext().lookupSourceFile(className);
		return SourceLineAnnotation.createUnknown(className, sourceFile);
	}
}
