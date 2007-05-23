package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.heap.StoreAnalysis;
import edu.umd.cs.findbugs.ba.heap.StoreDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class StoreDataflowFactory extends DataflowAnalysisFactory<StoreDataflow> {
	public StoreDataflowFactory() {
		super("field store analysis", StoreDataflow.class);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
	 */
	public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
		MethodGen methodGen = getMethodGen(analysisCache, descriptor);
		if (methodGen == null)
			return null;
		StoreAnalysis analysis = new StoreAnalysis(
				getDepthFirstSearch(analysisCache, descriptor),
				getConstantPoolGen(analysisCache, descriptor.getClassDescriptor())
		);
		StoreDataflow dataflow = new StoreDataflow(getCFG(analysisCache, descriptor), analysis);
		dataflow.execute();
		return dataflow;
	}

//	@Override @CheckForNull
//	protected StoreDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		MethodGen methodGen = getMethodGen(jclass, method);
//		if (methodGen == null)
//			return null;
//		StoreAnalysis analysis = new StoreAnalysis(
//				getDepthFirstSearch(jclass, method),
//				getConstantPoolGen(jclass)
//		);
//		StoreDataflow dataflow = new StoreDataflow(getCFG(jclass, method), analysis);
//		dataflow.execute();
//		return dataflow;
//	}
}
