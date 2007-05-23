package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.ba.ReverseDepthFirstSearch;
import edu.umd.cs.findbugs.ba.npe.ReturnPathTypeAnalysis;
import edu.umd.cs.findbugs.ba.npe.ReturnPathTypeDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class ReturnPathTypeDataflowFactory extends DataflowAnalysisFactory<ReturnPathTypeDataflow> {
    public ReturnPathTypeDataflowFactory() {
	    super("return path type dataflow", ReturnPathTypeDataflow.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	DepthFirstSearch dfs = getDepthFirstSearch(analysisCache, descriptor);
    	ReverseDepthFirstSearch rdfs = getReverseDepthFirstSearch(analysisCache, descriptor);
    	ReturnPathTypeAnalysis analysis = new ReturnPathTypeAnalysis(cfg, rdfs, dfs);
    	ReturnPathTypeDataflow dataflow = new ReturnPathTypeDataflow(cfg, analysis);

    	dataflow.execute();

    	return dataflow;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.ba.ClassContext.AnalysisFactory#analyze(org.apache.bcel.classfile.Method)
     */
//    @Override
//    protected ReturnPathTypeDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    	CFG cfg = getCFG(jclass, method);
//    	DepthFirstSearch dfs = getDepthFirstSearch(jclass, method);
//    	ReverseDepthFirstSearch rdfs = getReverseDepthFirstSearch(jclass, method);
//    	ReturnPathTypeAnalysis analysis = new ReturnPathTypeAnalysis(cfg, rdfs, dfs);
//    	ReturnPathTypeDataflow dataflow = new ReturnPathTypeDataflow(cfg, analysis);
//    
//    	dataflow.execute();
//    
//    	return dataflow;
//    }
}
