package edu.umd.cs.findbugs.classfile.engine.bcel;

import java.util.BitSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.Dataflow;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.ba.DominatorsAnalysis;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class DominatorsAnalysisFactory extends DataflowAnalysisFactory<DominatorsAnalysis> {
    public DominatorsAnalysisFactory() {
	    super("non-exception dominators analysis", DominatorsAnalysis.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	DepthFirstSearch dfs = getDepthFirstSearch(analysisCache, descriptor);
    	DominatorsAnalysis analysis = new DominatorsAnalysis(cfg, dfs, true);
    	Dataflow<java.util.BitSet, DominatorsAnalysis> dataflow =
    		new Dataflow<java.util.BitSet, DominatorsAnalysis>(cfg, analysis);
    	dataflow.execute();
    	return analysis;
    }

//    @Override
//    protected DominatorsAnalysis analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    	CFG cfg = getCFG(jclass, method);
//    	DepthFirstSearch dfs = getDepthFirstSearch(jclass, method);
//    	DominatorsAnalysis analysis = new DominatorsAnalysis(cfg, dfs, true);
//    	Dataflow<java.util.BitSet, DominatorsAnalysis> dataflow =
//    			new Dataflow<java.util.BitSet, DominatorsAnalysis>(cfg, analysis);
//    	dataflow.execute();
//    	return analysis;
//    }
}
