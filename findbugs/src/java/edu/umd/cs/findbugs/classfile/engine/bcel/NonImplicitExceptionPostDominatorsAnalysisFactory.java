package edu.umd.cs.findbugs.classfile.engine.bcel;

import java.util.BitSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.Dataflow;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.PostDominatorsAnalysis;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class NonImplicitExceptionPostDominatorsAnalysisFactory extends DataflowAnalysisFactory<NonImplicitExceptionPostDominatorsAnalysis> {
    public NonImplicitExceptionPostDominatorsAnalysisFactory() {
	    super("non-implicit-exception postdominators analysis", NonImplicitExceptionPostDominatorsAnalysis.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	NonImplicitExceptionPostDominatorsAnalysis analysis = new NonImplicitExceptionPostDominatorsAnalysis(
    			cfg,
    			getReverseDepthFirstSearch(analysisCache, descriptor),
    			getDepthFirstSearch(analysisCache, descriptor));
    	Dataflow<BitSet, PostDominatorsAnalysis> dataflow =
    		new Dataflow<BitSet, PostDominatorsAnalysis>(cfg, analysis);
    	dataflow.execute();

    	return analysis;
    }

//    @Override
//    protected NonImplicitExceptionPostDominatorsAnalysis analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    	CFG cfg = getCFG(jclass, method);
//    	NonImplicitExceptionPostDominatorsAnalysis analysis = new NonImplicitExceptionPostDominatorsAnalysis(
//    			cfg,
//    			getReverseDepthFirstSearch(jclass, method),
//    			getDepthFirstSearch(jclass, method));
//    	Dataflow<BitSet, PostDominatorsAnalysis> dataflow =
//    		new Dataflow<BitSet, PostDominatorsAnalysis>(cfg, analysis);
//    	dataflow.execute();
//    
//    	return analysis;
//    }
}
