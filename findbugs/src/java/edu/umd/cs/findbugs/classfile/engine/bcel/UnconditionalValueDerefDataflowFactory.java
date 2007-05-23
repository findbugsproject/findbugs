package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.MethodUnprofitableException;
import edu.umd.cs.findbugs.ba.deref.UnconditionalValueDerefAnalysis;
import edu.umd.cs.findbugs.ba.deref.UnconditionalValueDerefDataflow;
import edu.umd.cs.findbugs.ba.npe.IsNullValueDataflow;
import edu.umd.cs.findbugs.ba.type.TypeDataflow;
import edu.umd.cs.findbugs.ba.vna.ValueNumberDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class UnconditionalValueDerefDataflowFactory extends DataflowAnalysisFactory<UnconditionalValueDerefDataflow> {
    public UnconditionalValueDerefDataflowFactory() {
	    super("unconditional value dereference analysis", UnconditionalValueDerefDataflow.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	MethodGen methodGen =getMethodGen(analysisCache, descriptor);
    	if (methodGen == null) {
    		throw new MethodUnprofitableException(descriptor);
    	}

    	CFG cfg = getCFG(analysisCache, descriptor);

    	ValueNumberDataflow vnd = getValueNumberDataflow(analysisCache, descriptor);

    	UnconditionalValueDerefAnalysis analysis = new UnconditionalValueDerefAnalysis(
    			getReverseDepthFirstSearch(analysisCache, descriptor),
    			getDepthFirstSearch(analysisCache, descriptor),
    			cfg,
    			getMethod(analysisCache, descriptor),
    			methodGen,
    			vnd,
    			getAssertionMethods(analysisCache, descriptor.getClassDescriptor())
    	);

    	IsNullValueDataflow inv = getIsNullValueDataflow(analysisCache, descriptor);
    	// XXX: hack to clear derefs on not-null branches
    	analysis.clearDerefsOnNonNullBranches(inv);

    	TypeDataflow typeDataflow = getTypeDataflow(analysisCache, descriptor);
    	// XXX: type analysis is needed to resolve method calls for
    	// checking whether call targets unconditionally dereference parameters
    	analysis.setTypeDataflow(typeDataflow);

    	UnconditionalValueDerefDataflow dataflow =
    		new UnconditionalValueDerefDataflow(cfg, analysis);
    	dataflow.execute();
    	if (UnconditionalValueDerefAnalysis.DEBUG) {
    		ClassContext.dumpDataflowInformation(getMethod(analysisCache, descriptor), cfg, vnd, inv, dataflow, typeDataflow);
    	}

    	return dataflow;
    }

//    /* (non-Javadoc)
//     * @see edu.umd.cs.findbugs.ba.ClassContext.AnalysisFactory#analyze(org.apache.bcel.classfile.Method)
//     */
//    @Override
//    protected UnconditionalValueDerefDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    
//    	CFG cfg = getCFG(jclass, method);
//    
//    	ValueNumberDataflow vnd = getValueNumberDataflow(jclass, method);
//    
//    	UnconditionalValueDerefAnalysis analysis = new UnconditionalValueDerefAnalysis(
//    			getReverseDepthFirstSearch(jclass, method),
//    			getDepthFirstSearch(jclass, method),
//    			cfg,
//    			method,
//    			getMethodGen(jclass, method), vnd, getAssertionMethods(jclass)
//    			);
//    
//    	IsNullValueDataflow inv = getIsNullValueDataflow(jclass, method);
//    	// XXX: hack to clear derefs on not-null branches
//    	analysis.clearDerefsOnNonNullBranches(inv);
//    
//    	TypeDataflow typeDataflow = getTypeDataflow(jclass, method);
//    	// XXX: type analysis is needed to resolve method calls for
//    	// checking whether call targets unconditionally dereference parameters
//    	analysis.setTypeDataflow(typeDataflow);
//    
//    	UnconditionalValueDerefDataflow dataflow =
//    		new UnconditionalValueDerefDataflow(getCFG(jclass, method), analysis);
//    	dataflow.execute();
//    	 if (UnconditionalValueDerefAnalysis.DEBUG) {
//    			ClassContext.dumpDataflowInformation(method, cfg, vnd, inv, dataflow, typeDataflow);
//    		}
//    
//    	return dataflow;
//    }
}
