package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CompactLocationNumbering;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * Analysis engine to produce CompactLocationNumbering objects for methods.
 * 
 * @author David Hovemeyer
 */
public class CompactLocationNumberingFactory extends NoDataflowAnalysisFactory<CompactLocationNumbering> {
    public CompactLocationNumberingFactory() {
	    super("compact location numbering", CompactLocationNumbering.class);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	Method method = analysisCache.getMethodAnalysis(Method.class, descriptor);
    	
    	if (method.getCode() == null) {
    		return null;
    	}

    	CFG cfg = getCFG(analysisCache, descriptor);
    	return new CompactLocationNumbering(cfg);
    }
    
//    /* (non-Javadoc)
//     * @see edu.umd.cs.findbugs.ba.ClassContext.AnalysisFactory#analyze(org.apache.bcel.classfile.Method)
//     */
//    @Override
//    protected CompactLocationNumbering analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException{
//    	if (method.getCode() == null) {
//    		return null;
//    	}
//    
//    	CFG cfg = getCFG(jclass, method);
//    	return new CompactLocationNumbering(cfg);
//    }
}
