package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.heap.LoadAnalysis;
import edu.umd.cs.findbugs.ba.heap.LoadDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class LoadDataflowFactory extends DataflowAnalysisFactory<LoadDataflow> {
	public LoadDataflowFactory() {
		super("field load analysis", LoadDataflow.class);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
	 */
	public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
		MethodGen methodGen = getMethodGen(analysisCache, descriptor);
		if (methodGen == null)
			return null;
		LoadAnalysis analysis = new LoadAnalysis(
				getDepthFirstSearch(analysisCache, descriptor),
				getConstantPoolGen(analysisCache, descriptor.getClassDescriptor())
		);
		LoadDataflow dataflow = new LoadDataflow(getCFG(analysisCache, descriptor), analysis);
		dataflow.execute();
		return dataflow;
	}

//	@Override @CheckForNull
//	protected LoadDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		MethodGen methodGen = getMethodGen(jclass, method);
//		if (methodGen == null)
//			return null;
//		LoadAnalysis analysis = new LoadAnalysis(
//				getDepthFirstSearch(jclass, method),
//				getConstantPoolGen(jclass)
//		);
//		LoadDataflow dataflow = new LoadDataflow(getCFG(jclass, method), analysis);
//		dataflow.execute();
//		return dataflow;
//	}
}
