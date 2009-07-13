package edu.umd.cs.findbugs.detect;


import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugAccumulator;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ISourceLineAnnotation;
import edu.umd.cs.findbugs.SystemProperties;
import edu.umd.cs.findbugs.ann.AnnotationFactory;

public class IDivResultCastToDouble extends BytecodeScanningDetector {
	private static final boolean DEBUG = SystemProperties.getBoolean("idcd.debug");

	private final BugAccumulator bugAccumulator;
	private int prevOpCode;
	private ISourceLineAnnotation pendingIdivCastToDivBugLocation;

	public IDivResultCastToDouble(BugReporter bugReporter) {
		this.bugAccumulator = new BugAccumulator(bugReporter);
	}

	@Override
	public void visit(Method obj) {
		if (DEBUG) System.out.println("Visiting " + obj);
	}
	
	@Override
	public void visit(Code obj) {
		super.visit(obj);
		bugAccumulator.reportAccumulatedBugs();
	}
	
	@Override
	public void sawOpcode(int seen) {

		if (DEBUG) System.out.println("Saw opcode " + OPCODE_NAMES[seen] + " " + pendingIdivCastToDivBugLocation);



		if ((prevOpCode  == I2D || prevOpCode == L2D)
						&& seen == INVOKESTATIC
								&& getClassConstantOperand().equals("java/lang/Math")
								&& getNameConstantOperand().equals("ceil")) {
			bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(new BugInstance(this, 
				"ICAST_INT_CAST_TO_DOUBLE_PASSED_TO_CEIL", 
				HIGH_PRIORITY), this), AnnotationFactory.createSourceLine(this));
			pendingIdivCastToDivBugLocation = null;
		}
		else if ((prevOpCode  == I2F || prevOpCode == L2F)
				&& seen == INVOKESTATIC
						&& getClassConstantOperand().equals("java/lang/Math")
						&& getNameConstantOperand().equals("round")) {
			bugAccumulator.accumulateBug(DetectorUtil.addClassAndMethod(new BugInstance(this, 
					"ICAST_INT_CAST_TO_FLOAT_PASSED_TO_ROUND", 
					NORMAL_PRIORITY), this), AnnotationFactory.createSourceLine(this));
			pendingIdivCastToDivBugLocation = null;
		}
		else if (pendingIdivCastToDivBugLocation != null) {
			bugAccumulator.accumulateBug(
					DetectorUtil.addClassAndMethod(
							new BugInstance(this, "ICAST_IDIV_CAST_TO_DOUBLE", NORMAL_PRIORITY), this), 
				pendingIdivCastToDivBugLocation);
			pendingIdivCastToDivBugLocation = null;
		}

		if (prevOpCode  == IDIV && (seen == I2D|| seen == I2F)
			|| prevOpCode  == LDIV && (seen == L2D || seen==L2F))
			pendingIdivCastToDivBugLocation = AnnotationFactory.createSourceLine(this);
		prevOpCode = seen;
		}
}
