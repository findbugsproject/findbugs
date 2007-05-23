package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ReverseDepthFirstSearch;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class ReverseDepthFirstSearchFactory extends NoDataflowAnalysisFactory<ReverseDepthFirstSearch> {
    /**
     * @param name
     */
    public ReverseDepthFirstSearchFactory() {
	    super("reverse depth first search", ReverseDepthFirstSearch.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	ReverseDepthFirstSearch rdfs = new ReverseDepthFirstSearch(cfg);
    	rdfs.search();
    	return rdfs;
    }

//    @Override
//    protected ReverseDepthFirstSearch analyze(JavaClass jclass, Method method) throws CFGBuilderException {
//    	CFG cfg = getCFG(jclass, method);
//    	ReverseDepthFirstSearch rdfs = new ReverseDepthFirstSearch(cfg);
//    	rdfs.search();
//    	return rdfs;
//    }
}
