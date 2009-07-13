/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2005, University of Maryland
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

package edu.umd.cs.findbugs.detect;


import java.util.ArrayList;
import java.util.Collection;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.IFieldAnnotation;
import edu.umd.cs.findbugs.ILocalVariableAnnotation;
import edu.umd.cs.findbugs.IMethodAnnotation;
import edu.umd.cs.findbugs.ISourceLineAnnotation;
import edu.umd.cs.findbugs.IntAnnotation;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.ann.AnnotationFactory;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.XFactory;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.FieldDescriptor;
import edu.umd.cs.findbugs.visitclass.Util;

public class FindPuzzlers extends OpcodeStackDetector {

	static FieldDescriptor SYSTEM_OUT =  new FieldDescriptor("java/lang/System", "out", "Ljava/io/PrintStream;", true);
	static FieldDescriptor SYSTEM_ERR =  new FieldDescriptor("java/lang/System", "err", "Ljava/io/PrintStream;", true);
	
	final BugReporter bugReporter;
	final BugAccumulator bugAccumulator;
	public FindPuzzlers(BugReporter bugReporter) {
		this.bugReporter =  bugReporter;
		this.bugAccumulator = new BugAccumulator(bugReporter);
	}



	@Override
	public void visit(Code obj) {
		prevOpcodeIncrementedRegister = -1;
		best_priority_for_ICAST_INTEGER_MULTIPLY_CAST_TO_LONG = LOW_PRIORITY+1;
		prevOpCode = NOP;
		previousMethodInvocation = null;
		badlyComputingOddState = 0;
		resetIMulCastLong();
		imul_distance = 10000;
		ternaryConversionState = 0;
		super.visit(obj);
		bugAccumulator.reportAccumulatedBugs();
	}

	int imul_constant;
	int imul_distance;
	boolean imul_operand_is_parameter;
	int prevOpcodeIncrementedRegister;
	int valueOfConstantArgumentToShift;
	int best_priority_for_ICAST_INTEGER_MULTIPLY_CAST_TO_LONG ;
	boolean constantArgumentToShift;
	boolean shiftOfNonnegativeValue;
	int ternaryConversionState = 0;

	int badlyComputingOddState;
	int prevOpCode;
	XMethod previousMethodInvocation;
	boolean isTigerOrHigher;
	
	Constant value_LDC2_W;

	@Override
	public void visit(JavaClass obj) {
		isTigerOrHigher = obj.getMajor() >= MAJOR_1_5;
	}

	private void resetIMulCastLong() {
		imul_constant = 1;
		imul_operand_is_parameter = false;
	}
	private int adjustPriority(int factor, int priority) {
		if (factor <= 4) return LOW_PRIORITY+2;
		if (factor <= 10000) return priority+1;
		if (factor <= 60*60*1000) return priority;
		return priority-1;
	}
	private int adjustMultiplier(Object constant, int mul) {
		if (!(constant instanceof Integer)) return mul;
		return Math.abs(((Integer) constant).intValue()) * mul;

	}
	@Override
	public void sawOpcode(int seen) {
		

		if (seen == INVOKEVIRTUAL &&   getNameConstantOperand().equals("hashCode")
				&&   getSigConstantOperand().equals("()I")
				&& stack.getStackDepth() > 0) {
			OpcodeStack.Item item0 = stack.getStackItem(0);
			if (item0.getSignature().charAt(0) == '[') {
	            BugInstance bugInstance = DetectorUtil.addClassAndMethod(new BugInstance(this, "DMI_INVOKING_HASHCODE_ON_ARRAY", NORMAL_PRIORITY), this);
	            BugAnnotation annotation = AnnotationFactory.createValueSource(getMethod(), item0, getPC());
	            if(annotation != null){
	            	bugInstance.add(annotation);
	            }
	            bugReporter.reportBug(bugInstance.add(AnnotationFactory.createSourceLine(this)));
            }
		}
		if (seen != RETURN && isReturn(seen) && isRegisterStore(getPrevOpcode(1))) {
			
			int priority = Priorities.NORMAL_PRIORITY;
			if  (getMethodSig().endsWith(")Z")) priority =  Priorities.HIGH_PRIORITY;
			else {
				if (getMethodSig().endsWith(")Ljava/lang/String;")) priority =  Priorities.LOW_PRIORITY;
				if (getPC() == getCode().getCode().length-1 ) priority++;
			}
			bugReporter.reportBug(DetectorUtil.addClassAndMethod(new BugInstance(this, "DLS_DEAD_LOCAL_STORE_IN_RETURN", priority), this).add(AnnotationFactory.createSourceLine(this)));
		}
		// System.out.println(getPC() + " " + OPCODE_NAMES[seen] + " " + ternaryConversionState);
		if (seen == IMUL) {
			if (imul_distance != 1) resetIMulCastLong();
			imul_distance = 0;
			if (stack.getStackDepth() > 1) {
				OpcodeStack.Item item0 = stack.getStackItem(0);
				OpcodeStack.Item item1 = stack.getStackItem(1);
				imul_constant = adjustMultiplier(item0.getConstant(), imul_constant);
				imul_constant = adjustMultiplier(item1.getConstant(), imul_constant);

				if (item0.isInitialParameter() || item1.isInitialParameter())
					imul_operand_is_parameter = true;
			}} else {
				imul_distance++;
			}

		if (prevOpCode == IMUL && seen == I2L) {
			int priority = adjustPriority(imul_constant, NORMAL_PRIORITY);
			if (priority >= LOW_PRIORITY && imul_constant != 1000 && imul_operand_is_parameter) priority = NORMAL_PRIORITY;
			if (priority <= best_priority_for_ICAST_INTEGER_MULTIPLY_CAST_TO_LONG) {
				best_priority_for_ICAST_INTEGER_MULTIPLY_CAST_TO_LONG = priority;
				bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(new BugInstance(this, 
						"ICAST_INTEGER_MULTIPLY_CAST_TO_LONG", 
						priority), this), AnnotationFactory.createSourceLine(this));
			}
		}

		if (getMethodName().equals("<clinit>") && (seen == PUTSTATIC || seen == GETSTATIC || seen == INVOKESTATIC)) {
			String clazz = getClassConstantOperand();
			if (!clazz.equals(getClassName())) {
				try {
					JavaClass targetClass = Repository.lookupClass(clazz);
					if (Repository.instanceOf(targetClass, getThisClass())) {
						int priority = NORMAL_PRIORITY;
						if (seen == GETSTATIC) priority--;
						if (!targetClass.isPublic()) priority++;
						bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(new BugInstance(this, 
								"IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION", 
								priority), this)
						.add(AnnotationFactory.createClass(getClassConstantOperand())),
						AnnotationFactory.createSourceLine(this));
						
					}
				} catch (ClassNotFoundException e) {
					// ignore it
				}

			}
		}
		if (false && (seen == INVOKEVIRTUAL)
				&&   getNameConstantOperand().equals("equals")
				&&   getSigConstantOperand().equals("(Ljava/lang/Object;)Z")
				&& stack.getStackDepth() > 1) {
			OpcodeStack.Item item0 = stack.getStackItem(0);
			OpcodeStack.Item item1 = stack.getStackItem(1);

			if (item0.isArray() || item1.isArray()) {
				bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
						new BugInstance(this, "EC_BAD_ARRAY_COMPARE", NORMAL_PRIORITY), this), 
						AnnotationFactory.createSourceLine(this));
			}
		}


		if (seen >= IALOAD && seen <= SALOAD || seen >= IASTORE && seen <= SASTORE ) {
			Item index  = stack.getStackItem(0);
			if (index.getSpecialKind() == Item.AVERAGE_COMPUTED_USING_DIVISION) {
				ISourceLineAnnotation where;
				if (index.getPC() >= 0)
					where = AnnotationFactory.createSourceLine(this, index.getPC());
				else where = AnnotationFactory.createSourceLine(this);
				bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
						new BugInstance(this, "IM_AVERAGE_COMPUTATION_COULD_OVERFLOW", NORMAL_PRIORITY), this), where);
			}
				
		}

		if ((seen == IFEQ || seen == IFNE) && getPrevOpcode(1) == IMUL
				&& ( getPrevOpcode(2) == SIPUSH
						|| getPrevOpcode(2) == BIPUSH
				)
				&& getPrevOpcode(3) == IREM
		)
			
		bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
				new BugInstance(this, "IM_MULTIPLYING_RESULT_OF_IREM", LOW_PRIORITY), this), 
				AnnotationFactory.createSourceLine(this));
	

		if (seen == I2S && getPrevOpcode(1) == IUSHR && !shiftOfNonnegativeValue && 
				(!constantArgumentToShift || valueOfConstantArgumentToShift % 16 != 0)
				|| seen == I2B && getPrevOpcode(1) == IUSHR && !shiftOfNonnegativeValue
				&& (!constantArgumentToShift || valueOfConstantArgumentToShift % 8 != 0)) {
	        bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
					new BugInstance(this, "ICAST_QUESTIONABLE_UNSIGNED_RIGHT_SHIFT", NORMAL_PRIORITY), this), 
					AnnotationFactory.createSourceLine(this));
        }


		constantArgumentToShift = false;
		shiftOfNonnegativeValue = false;
		if ( (seen == IUSHR 
				|| seen == ISHR 
				|| seen == ISHL )) {
			if (stack.getStackDepth() <= 1) {
				// don't understand; lie so other detectors won't get concerned
				constantArgumentToShift = true;
				valueOfConstantArgumentToShift = 8;
			}
			else {
				Object rightHandSide
				= stack.getStackItem(0).getConstant();

				Object leftHandSide 
				=  stack.getStackItem(1).getConstant();
				shiftOfNonnegativeValue = stack.getStackItem(1).isNonNegative();
				if (rightHandSide instanceof Integer) {
					constantArgumentToShift = true;
					valueOfConstantArgumentToShift = ((Integer) rightHandSide).intValue();
					if (valueOfConstantArgumentToShift < 0 || valueOfConstantArgumentToShift >= 32)
						bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
								new BugInstance(this, "ICAST_BAD_SHIFT_AMOUNT", 
								valueOfConstantArgumentToShift < 0 ? LOW_PRIORITY 
										: (valueOfConstantArgumentToShift == 32 && getMethodName().equals("hashCode") 
												? NORMAL_PRIORITY : HIGH_PRIORITY)), this)
						.addInt(valueOfConstantArgumentToShift).describe(IntAnnotation.INT_SHIFT), 
						AnnotationFactory.createSourceLine(this));
				}
				if (leftHandSide instanceof Integer
						&& ((Integer) leftHandSide).intValue() > 0) {
					// boring; lie so other detectors won't get concerned
					constantArgumentToShift = true;
					valueOfConstantArgumentToShift = 8;
				}
			}
		}



		if (seen == INVOKEVIRTUAL && stack.getStackDepth() > 0
				&& (getClassConstantOperand().equals("java/util/Date") 
						|| getClassConstantOperand().equals("java/sql/Date"))
				&& getNameConstantOperand().equals("setMonth")
				&& getSigConstantOperand().equals("(I)V")) {
			OpcodeStack.Item item = stack.getStackItem(0);
			Object o = item.getConstant();
			if (o != null && o instanceof Integer) {
				int v = ((Integer) o).intValue();
				if (v < 0 || v > 11) {
	                bugReporter.reportBug(DetectorUtil.addClassAndMethod(new BugInstance(this, "DMI_BAD_MONTH", HIGH_PRIORITY), this)
					.addInt(v).describe(IntAnnotation.INT_VALUE)
					.add(AnnotationFactory.createCalledMethod(this))
					.add(AnnotationFactory.createSourceLine(this)));
                }
			}
		}

		if (seen == INVOKEVIRTUAL && stack.getStackDepth() > 1
				&& getClassConstantOperand().equals("java/util/Calendar")
				&& getNameConstantOperand().equals("set")

				||
				seen == INVOKESPECIAL && stack.getStackDepth() > 1
				&& getClassConstantOperand().equals("java/util/GregorianCalendar")
				&& getNameConstantOperand().equals("<init>")

		) {
			String sig = getSigConstantOperand();
			if (sig.startsWith("(III")) {
				int pos = sig.length() - 5;
				OpcodeStack.Item item = stack.getStackItem(pos);
				Object o = item.getConstant();
				if (o != null && o instanceof Integer) {
					int v = ((Integer) o).intValue();
					if (v < 0 || v > 11) {
	                    bugReporter.reportBug(DetectorUtil.addClassAndMethod(new BugInstance(this, "DMI_BAD_MONTH", NORMAL_PRIORITY), this)
						.addInt(v).describe(IntAnnotation.INT_VALUE)
						.add(AnnotationFactory.createCalledMethod(this))
						.add(AnnotationFactory.createSourceLine(this))
						);
                    }
				}
			}
		}

		if (isRegisterStore() && (seen == ISTORE 
				|| seen == ISTORE_0
				|| seen == ISTORE_1
				|| seen == ISTORE_2
				|| seen == ISTORE_3)
				&& getRegisterOperand() == prevOpcodeIncrementedRegister) {
			bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
					new BugInstance(this, "DLS_OVERWRITTEN_INCREMENT", HIGH_PRIORITY), this), 
					AnnotationFactory.createSourceLine(this));			
		}
		
		if (seen == IINC) {
			prevOpcodeIncrementedRegister = getRegisterOperand();	
		} else {
	        prevOpcodeIncrementedRegister = -1;
        }

		// Java Puzzlers, Chapter 2, puzzle 1
		// Look for ICONST_2 IREM ICONST_1  IF_ICMPNE L1

		switch (badlyComputingOddState) {
		case 0:
			if (seen == ICONST_2) badlyComputingOddState++;
			break;
		case 1:
			if (seen == IREM) {
				OpcodeStack.Item item = stack.getStackItem(1);
				if (!item.isNonNegative() && item.getSpecialKind() != OpcodeStack.Item.MATH_ABS)
					badlyComputingOddState++;
				else  badlyComputingOddState = 0;
			}
			else badlyComputingOddState = 0;
			break;
		case 2:
			if (seen == ICONST_1) badlyComputingOddState++;
			else badlyComputingOddState = 0;
			break;
		case 3:
			if (seen == IF_ICMPEQ || seen == IF_ICMPNE)  {
				bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
						new BugInstance(this, "IM_BAD_CHECK_FOR_ODD", NORMAL_PRIORITY), this), 
						AnnotationFactory.createSourceLine(this));
			}
			badlyComputingOddState = 0;
			break;
		}

		// Java Puzzlers, chapter 3, puzzle 12
		if (seen == INVOKEVIRTUAL && stack.getStackDepth() > 0 
				&& (getNameConstantOperand().equals("toString")
						&& getSigConstantOperand().equals("()Ljava/lang/String;")
						|| getNameConstantOperand().equals("append")
						&& getSigConstantOperand().equals("(Ljava/lang/Object;)Ljava/lang/StringBuilder;") && getClassConstantOperand().equals("java/lang/StringBuilder")
						|| getNameConstantOperand().equals("append")
						&& getSigConstantOperand().equals("(Ljava/lang/Object;)Ljava/lang/StringBuffer;") && getClassConstantOperand().equals("java/lang/StringBuffer")
						|| (getNameConstantOperand().equals("print") || getNameConstantOperand().equals("println"))
						 	&& getSigConstantOperand().equals("(Ljava/lang/Object;)V")
				)
		) {
			OpcodeStack.Item item = stack.getStackItem(0);
			String signature = item.getSignature();
			if (signature != null && signature.startsWith("[")) {
				boolean debuggingContext = signature.equals("[Ljava/lang/StackTraceElement;");
				
				if (!debuggingContext) {
					for(CodeException e : getCode().getExceptionTable()) {
						if (e.getHandlerPC()<= getPC() && e.getHandlerPC() +30 >= getPC())
							debuggingContext = true;
					}
						

					for(int i = 1; !debuggingContext &&  i < stack.getStackDepth(); i++) {
					OpcodeStack.Item e = stack.getStackItem(i);
					
					if (e.getSignature().indexOf("Logger") >= 0 || e.getSignature().indexOf("Exception") >= 0) debuggingContext = true;
					
					XField f = e.getXField();
					if (f != null && (SYSTEM_ERR.equals(f.getFieldDescriptor()) || SYSTEM_OUT.equals(f.getFieldDescriptor())))
							debuggingContext = true;
					}
				}
				int reg = item.getRegisterNumber();
				Collection<BugAnnotation> as = new ArrayList<BugAnnotation>();
				XField field = item.getXField();
				IFieldAnnotation fieldAnnotation = null;
				if (field != null) {
					fieldAnnotation = AnnotationFactory.createField(field);
					fieldAnnotation.setDescription(IFieldAnnotation.LOADED_FROM_ROLE);
				}
					
					
				if(reg != -1) {
					ILocalVariableAnnotation lva =
						AnnotationFactory.createVariable(
							getMethod(), reg, getPC(), getPC()-1);
					if (lva.isNamed()) {
						as.add(lva);
						if (fieldAnnotation != null)
							as.add(fieldAnnotation);
					} else {
						if (fieldAnnotation != null) 
							as.add(fieldAnnotation);
						as.add(lva);
					}
				} else if (fieldAnnotation != null)
						as.add(fieldAnnotation);
				else {
					XMethod m = item.getReturnValueOf();
					if (m != null) {
						IMethodAnnotation methodAnnotation = AnnotationFactory.createMethod(m);
						methodAnnotation.setDescription(IMethodAnnotation.METHOD_RETURN_VALUE_OF);
						as.add(methodAnnotation);
					}
				}
				int priority = debuggingContext ? NORMAL_PRIORITY : HIGH_PRIORITY;
				if(!as.isEmpty()) {
					BugInstance bugInstance = DetectorUtil.addClassAndMethod(new BugInstance(this, "DMI_INVOKING_TOSTRING_ON_ARRAY", priority), this);
					addAnnotations(bugInstance, as);
					bugAccumulator.accumulateBug(bugInstance, AnnotationFactory.createSourceLine(this));
				} else {
					bugAccumulator.accumulateBug(
							DetectorUtil.addClassAndMethod(
									new BugInstance(this, "DMI_INVOKING_TOSTRING_ON_ANONYMOUS_ARRAY", priority), this), 
									AnnotationFactory.createSourceLine(this));
				}
			}
		}

		if (isTigerOrHigher) {
			if (previousMethodInvocation != null && prevOpCode == INVOKESPECIAL && seen == INVOKEVIRTUAL) {
				String classNameForPreviousMethod = previousMethodInvocation.getClassName();
				String classNameForThisMethod = getClassConstantOperand();
				if (classNameForPreviousMethod.startsWith("java.lang.") 
						&& classNameForPreviousMethod.equals(classNameForThisMethod.replace('/','.'))
						&& getNameConstantOperand().endsWith("Value")
						&& getSigConstantOperand().length() == 3) {
					if (getSigConstantOperand().charAt(2) == previousMethodInvocation.getSignature().charAt(1)) {
	                    bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
	                    		new BugInstance(this, "BX_BOXING_IMMEDIATELY_UNBOXED", NORMAL_PRIORITY), this), 
	                    		AnnotationFactory.createSourceLine(this));
                    } else {
	                    bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
	                    		new BugInstance(this, "BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION", NORMAL_PRIORITY), this), 
	                    		AnnotationFactory.createSourceLine(this));
                    }
						
					ternaryConversionState = 1;
				} else ternaryConversionState = 0;

			} else if (false && seen == INVOKEVIRTUAL) {
				if (getClassConstantOperand().startsWith("java/lang") 
						&& getNameConstantOperand().endsWith("Value") 
						&& getSigConstantOperand().length() == 3) {
	                ternaryConversionState = 1;
                } else {
	                ternaryConversionState = 0;
                }
			} else if (ternaryConversionState == 1) {
				if (I2L < seen && seen <= I2S) 
					ternaryConversionState = 2;
				else ternaryConversionState = 0;
			} else if (ternaryConversionState == 2) {
				ternaryConversionState = 0;
				if (seen == GOTO) {
	                bugReporter.reportBug(DetectorUtil.addClassAndMethod(new BugInstance(this, "BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR", NORMAL_PRIORITY), this)
					.add(AnnotationFactory.createSourceLine(this)));
                }
			} else {
	            ternaryConversionState = 0;
            }
		}

		if (seen == INVOKESTATIC)
			if ((getNameConstantOperand().startsWith("assert") || getNameConstantOperand().startsWith("fail")) && getMethodName().equals("run")
					&& implementsRunnable(getThisClass())) {
				try {
					int size1 = Util.getSizeOfSurroundingTryBlock(getConstantPool(), getMethod().getCode(),
							"java/lang/Throwable", getPC());
					int size2 = Util.getSizeOfSurroundingTryBlock(getConstantPool(), getMethod().getCode(),
							"java/lang/Error", getPC());
					int size3 = Util.getSizeOfSurroundingTryBlock(getConstantPool(), getMethod().getCode(),
							"java/lang/AssertionFailureError", getPC());
					int size = Math.min(Math.min( size1, size2), size3);
					if (size == Integer.MAX_VALUE) {
						JavaClass targetClass = AnalysisContext.currentAnalysisContext().lookupClass(getClassConstantOperand().replace('/', '.'));
						if (targetClass.getSuperclassName().startsWith("junit")) {
							bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
									new BugInstance(this, "IJU_ASSERT_METHOD_INVOKED_FROM_RUN_METHOD", NORMAL_PRIORITY), this), 
									AnnotationFactory.createSourceLine(this));
						}
					}
				} catch (ClassNotFoundException e) {
					AnalysisContext.reportMissingClass(e);
				}
			}
		if (seen == INVOKESPECIAL && getClassConstantOperand().startsWith("java/lang/")  && getNameConstantOperand().equals("<init>")
				&& getSigConstantOperand().length() == 4
		) {
	        previousMethodInvocation = XFactory.createReferencedXMethod(this);
        } else if (seen == INVOKESTATIC && getClassConstantOperand().startsWith("java/lang/")  
				&& getNameConstantOperand().equals("valueOf")
				&& getSigConstantOperand().length() == 4) {
	        previousMethodInvocation = XFactory.createReferencedXMethod(this);
        } else {
	        previousMethodInvocation = null;
        }
		
		
		
		if (seen == LDC2_W) {
			value_LDC2_W = getConstantRefOperand();
		} else if (seen == L2I && getPrevOpcode(1) == LAND && getPrevOpcode(2) == LDC2_W && value_LDC2_W instanceof ConstantLong) {
			ConstantLong longValue = (ConstantLong) value_LDC2_W;
			if (longValue.getBytes() == 0xEFFFFFFF) {
	            bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(
	            		new BugInstance(this, "UNKNOWN", NORMAL_PRIORITY), this), 
	            		AnnotationFactory.createSourceLine(this));
            }
		}
		prevOpCode = seen;

		}
	
	boolean implementsRunnable(JavaClass obj) {
		if (obj.getSuperclassName().equals("java.lang.Thread")) return true;
		for(String s : obj.getInterfaceNames())
			if (s.equals("java.lang.Runnable")) return true;
		return false;
	}

	/**
	 * Add a Collection of BugAnnotations.
	 * 
	 * @param annotationCollection Collection of BugAnnotations
	 */
	private void addAnnotations(BugInstance bug, Collection<? extends BugAnnotation> annotationCollection) {
		for (BugAnnotation annotation : annotationCollection) {
			bug.add(annotation);
		}
	}
}
