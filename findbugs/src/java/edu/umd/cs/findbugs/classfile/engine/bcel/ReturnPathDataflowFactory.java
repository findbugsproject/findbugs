package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.ba.ReturnPathAnalysis;
import edu.umd.cs.findbugs.ba.ReturnPathDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class ReturnPathDataflowFactory extends DataflowAnalysisFactory<ReturnPathDataflow> {
    public ReturnPathDataflowFactory() {
	    super("return path analysis", ReturnPathDataflow.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	DepthFirstSearch dfs = getDepthFirstSearch(analysisCache, descriptor);
    	ReturnPathAnalysis analysis = new ReturnPathAnalysis(dfs);
    	ReturnPathDataflow dataflow = new ReturnPathDataflow(cfg, analysis);
    	dataflow.execute();
    	return dataflow;
    }

//    @Override
//    protected ReturnPathDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    	CFG cfg = getCFG(jclass, method);
//    	DepthFirstSearch dfs = getDepthFirstSearch(jclass, method);
//    	ReturnPathAnalysis analysis = new ReturnPathAnalysis(dfs);
//    	ReturnPathDataflow dataflow = new ReturnPathDataflow(cfg, analysis);
//    	dataflow.execute();
//    	return dataflow;
//    }
}
