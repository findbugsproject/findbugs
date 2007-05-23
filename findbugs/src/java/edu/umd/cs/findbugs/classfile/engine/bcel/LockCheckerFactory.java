package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.LockChecker;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * Analysis engine to produce LockChecker objects for analyzed methods.
 * 
 * @author David Hovemeyer
 */
public class LockCheckerFactory extends DataflowAnalysisFactory<LockChecker> {
    public LockCheckerFactory() {
	    super("lock checker meta-analysis", LockChecker.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	LockChecker lockChecker = new LockChecker(descriptor);

    	lockChecker.execute();
    	return lockChecker;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.ClassContext.AnalysisFactory#analyze(org.apache.bcel.classfile.Method)
     */
//    @Override
//    protected LockChecker analyze(JavaClass jclass, Method method) throws CFGBuilderException,
//    		DataflowAnalysisException {
//    	MethodDescriptor methodDescriptor =
//    		new MethodDescriptor(jclass.getClassName().replace('.', '/'), method.getName(), method.getSignature(), method.isStatic());
//    	LockChecker lockChecker = new LockChecker(methodDescriptor);
//    	
//    	try {
//	        lockChecker.execute();
//	    	return lockChecker;
//        } catch (DataflowAnalysisException e) {
//        	throw e;
//        } catch (CFGBuilderException e) {
//        	throw e;
//        } catch (CheckedAnalysisException e) {
//        	IllegalStateException ise = new IllegalStateException("should not happen");
//        	ise.initCause(e);
//        	throw ise;
//        }
//    }
}
