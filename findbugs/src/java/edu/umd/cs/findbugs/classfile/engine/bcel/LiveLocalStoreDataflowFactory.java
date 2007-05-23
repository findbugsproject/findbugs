package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.LiveLocalStoreAnalysis;
import edu.umd.cs.findbugs.ba.LiveLocalStoreDataflow;
import edu.umd.cs.findbugs.ba.ReverseDepthFirstSearch;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class LiveLocalStoreDataflowFactory extends DataflowAnalysisFactory<LiveLocalStoreDataflow> {
    public LiveLocalStoreDataflowFactory() {
	    super("live local stores analysis", LiveLocalStoreDataflow.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	MethodGen methodGen = getMethodGen(analysisCache, descriptor);
    	if (methodGen == null) {
    		return null;
    	}
    	CFG cfg = getCFG(analysisCache, descriptor);

    	ReverseDepthFirstSearch rdfs = getReverseDepthFirstSearch(analysisCache, descriptor);

    	LiveLocalStoreAnalysis analysis =
    		new LiveLocalStoreAnalysis(methodGen, rdfs, getDepthFirstSearch(analysisCache, descriptor));
    	LiveLocalStoreDataflow dataflow =
    		new LiveLocalStoreDataflow(cfg, analysis);

    	dataflow.execute();

    	return dataflow;
    }

//    @Override
//    protected LiveLocalStoreDataflow analyze(JavaClass jclass, Method method)
//    	throws DataflowAnalysisException, CFGBuilderException {
//    		MethodGen methodGen = getMethodGen(jclass, method);
//    		if (methodGen == null) return null;
//    		CFG cfg = getCFG(jclass, method);
//    
//    		ReverseDepthFirstSearch rdfs = getReverseDepthFirstSearch(jclass, method);
//    
//    		LiveLocalStoreAnalysis analysis = new LiveLocalStoreAnalysis(methodGen, rdfs, getDepthFirstSearch(jclass, method));
//    		LiveLocalStoreDataflow dataflow = new LiveLocalStoreDataflow(cfg, analysis);
//    
//    		dataflow.execute();
//    
//    		return dataflow;
//    }
}
