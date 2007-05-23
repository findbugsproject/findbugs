package edu.umd.cs.findbugs.classfile.engine.bcel;

import java.util.BitSet;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.Hierarchy;
import edu.umd.cs.findbugs.ba.InnerClassAccess;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.ba.vna.LoadedFieldSet;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * Factory to determine which fields are loaded and stored
 * by the instructions in a method, and the overall method.
 * The main purpose is to support efficient redundant load elimination
 * and forward substitution in ValueNumberAnalysis (there is no need to
 * remember stores of fields that are never read,
 * or loads of fields that are only loaded in one location).
 * However, it might be useful for other kinds of analysis.
 *
 * <p> The tricky part is that in addition to fields loaded and stored
 * with get/putfield and get/putstatic, we also try to figure
 * out field accessed through calls to inner-class access methods.
 */
public class LoadedFieldSetFactory extends NoExceptionAnalysisFactory<LoadedFieldSet> {

	static final BitSet fieldInstructionOpcodeSet = new BitSet();
	static {
		fieldInstructionOpcodeSet.set(Constants.GETFIELD);
		fieldInstructionOpcodeSet.set(Constants.PUTFIELD);
		fieldInstructionOpcodeSet.set(Constants.GETSTATIC);
		fieldInstructionOpcodeSet.set(Constants.PUTSTATIC);
	}

	public LoadedFieldSetFactory() {
		super("loaded field set factory", LoadedFieldSet.class);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
	 */
	public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
		MethodGen methodGen = getMethodGen(analysisCache, descriptor);
		if (methodGen == null) return null;
		InstructionList il = methodGen.getInstructionList();

		LoadedFieldSet loadedFieldSet = new LoadedFieldSet(methodGen);
		JavaClass jclass = getJavaClass(analysisCache, descriptor.getClassDescriptor());
		ConstantPoolGen cpg = getConstantPoolGen(analysisCache, descriptor.getClassDescriptor());

		for (InstructionHandle handle = il.getStart(); handle != null; handle = handle.getNext()) {
			Instruction ins = handle.getInstruction();
			short opcode = ins.getOpcode();
			try {
				if (opcode == Constants.INVOKESTATIC) {
					INVOKESTATIC inv = (INVOKESTATIC) ins;
					if (Hierarchy.isInnerClassAccess(inv, cpg)) {
						InnerClassAccess access = Hierarchy.getInnerClassAccess(inv, cpg);
						/*
    									if (access == null) {
    										System.out.println("Missing inner class access in " +
    											SignatureConverter.convertMethodSignature(methodGen) + " at " +
    											inv);
    									}
						 */
						if (access != null) {
							if (access.isLoad())
								loadedFieldSet.addLoad(handle, access.getField());
							else
								loadedFieldSet.addStore(handle, access.getField());
						}
					}
				} else if (fieldInstructionOpcodeSet.get(opcode)) {
					boolean isLoad = (opcode == Constants.GETFIELD || opcode == Constants.GETSTATIC);
					XField field = Hierarchy.findXField((FieldInstruction) ins, cpg);
					if (field != null) {
						if (isLoad)
							loadedFieldSet.addLoad(handle, field);
						else
							loadedFieldSet.addStore(handle, field);
					}
				}
			} catch (ClassNotFoundException e) {
				AnalysisContext.currentAnalysisContext().getLookupFailureCallback().reportMissingClass(e);
			}
		}

		return loadedFieldSet;
	}

//	@Override
//	protected LoadedFieldSet analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		MethodGen methodGen = getMethodGen(jclass, method);
//		if (methodGen == null) return null;
//		InstructionList il = methodGen.getInstructionList();
//
//		LoadedFieldSet loadedFieldSet = new LoadedFieldSet(methodGen);
//
//		for (InstructionHandle handle = il.getStart(); handle != null; handle = handle.getNext()) {
//			Instruction ins = handle.getInstruction();
//			short opcode = ins.getOpcode();
//			try {
//				if (opcode == Constants.INVOKESTATIC) {
//					INVOKESTATIC inv = (INVOKESTATIC) ins;
//					if (Hierarchy.isInnerClassAccess(inv, getConstantPoolGen(jclass))) {
//						InnerClassAccess access = Hierarchy.getInnerClassAccess(inv, getConstantPoolGen(jclass));
//						/*
//    									if (access == null) {
//    										System.out.println("Missing inner class access in " +
//    											SignatureConverter.convertMethodSignature(methodGen) + " at " +
//    											inv);
//    									}
//						 */
//						if (access != null) {
//							if (access.isLoad())
//								loadedFieldSet.addLoad(handle, access.getField());
//							else
//								loadedFieldSet.addStore(handle, access.getField());
//						}
//					}
//				} else if (fieldInstructionOpcodeSet.get(opcode)) {
//					boolean isLoad = (opcode == Constants.GETFIELD || opcode == Constants.GETSTATIC);
//					XField field = Hierarchy.findXField((FieldInstruction) ins, getConstantPoolGen(jclass));
//					if (field != null) {
//						if (isLoad)
//							loadedFieldSet.addLoad(handle, field);
//						else
//							loadedFieldSet.addStore(handle, field);
//					}
//				}
//			} catch (ClassNotFoundException e) {
//				AnalysisContext.currentAnalysisContext().getLookupFailureCallback().reportMissingClass(e);
//			}
//		}
//
//		return loadedFieldSet;
//	}
}
