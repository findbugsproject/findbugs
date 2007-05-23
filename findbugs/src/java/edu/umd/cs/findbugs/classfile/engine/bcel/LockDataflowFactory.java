package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.ba.LockAnalysis;
import edu.umd.cs.findbugs.ba.LockDataflow;
import edu.umd.cs.findbugs.ba.MethodUnprofitableException;
import edu.umd.cs.findbugs.ba.vna.ValueNumberDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class LockDataflowFactory extends DataflowAnalysisFactory<LockDataflow> {
    public LockDataflowFactory() {
	    super("lock set analysis", LockDataflow.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	MethodGen methodGen = getMethodGen(analysisCache, descriptor);
    	if (methodGen == null) throw new MethodUnprofitableException(descriptor);
    	ValueNumberDataflow vnaDataflow = getValueNumberDataflow(analysisCache, descriptor);
    	DepthFirstSearch dfs = getDepthFirstSearch(analysisCache, descriptor);
    	CFG cfg = getCFG(analysisCache, descriptor);

    	LockAnalysis analysis = new LockAnalysis(methodGen, vnaDataflow, dfs);
    	LockDataflow dataflow = new LockDataflow(cfg, analysis);
    	dataflow.execute();
    	return dataflow;
    	
    }

//    @Override
//    protected LockDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException{
//    	MethodGen methodGen = getMethodGen(jclass, method);
//    	if (methodGen == null) throw new MethodUnprofitableException(jclass,method);
//    	ValueNumberDataflow vnaDataflow = getValueNumberDataflow(jclass, method);
//    	DepthFirstSearch dfs = getDepthFirstSearch(jclass, method);
//    	CFG cfg = getCFG(jclass, method);
//    
//    	LockAnalysis analysis = new LockAnalysis(methodGen, vnaDataflow, dfs);
//    	LockDataflow dataflow = new LockDataflow(cfg, analysis);
//    	dataflow.execute();
//    	return dataflow;
//    }
}
