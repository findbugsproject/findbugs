package edu.umd.cs.findbugs.classfile.engine.bcel;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class DepthFirstSearchFactory extends NoDataflowAnalysisFactory<DepthFirstSearch> {
	/**
	 * @param name
	 */
	public DepthFirstSearchFactory() {
		super("depth first search", DepthFirstSearch.class);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
	 */
	public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
		CFG cfg = getCFG(analysisCache, descriptor);
		DepthFirstSearch dfs = new DepthFirstSearch(cfg);
		dfs.search();
		return dfs;
	}

//	@Override
//	protected DepthFirstSearch analyze(JavaClass jclass, Method method) throws CFGBuilderException {
//		CFG cfg = getCFG(jclass, method);
//		DepthFirstSearch dfs = new DepthFirstSearch(cfg);
//		dfs.search();
//		return dfs;
//	}
}
