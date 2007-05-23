package edu.umd.cs.findbugs.classfile.engine.bcel;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.Dataflow;
import edu.umd.cs.findbugs.ba.PostDominatorsAnalysis;
import edu.umd.cs.findbugs.ba.ReverseDepthFirstSearch;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class NonExceptionPostdominatorsAnalysisFactory extends DataflowAnalysisFactory<NonExceptionPostdominatorsAnalysis> {
    public NonExceptionPostdominatorsAnalysisFactory() {
	    super("non-exception postdominators analysis", NonExceptionPostdominatorsAnalysis.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	ReverseDepthFirstSearch rdfs = getReverseDepthFirstSearch(analysisCache, descriptor);
    	NonExceptionPostdominatorsAnalysis analysis = new NonExceptionPostdominatorsAnalysis(cfg, rdfs, getDepthFirstSearch(analysisCache, descriptor));
    	Dataflow<java.util.BitSet, PostDominatorsAnalysis> dataflow =
    			new Dataflow<java.util.BitSet, PostDominatorsAnalysis>(cfg, analysis);
    	dataflow.execute();
    	return analysis;
    }

//    @Override
//    protected NonExceptionPostdominatorsAnalysis analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    	CFG cfg = getCFG(jclass, method);
//    	ReverseDepthFirstSearch rdfs = getReverseDepthFirstSearch(jclass, method);
//    	NonExceptionPostdominatorsAnalysis analysis = new NonExceptionPostdominatorsAnalysis(cfg, rdfs, getDepthFirstSearch(jclass, method));
//    	Dataflow<java.util.BitSet, PostDominatorsAnalysis> dataflow =
//    			new Dataflow<java.util.BitSet, PostDominatorsAnalysis>(cfg, analysis);
//    	dataflow.execute();
//    	return analysis;
//    }
}
