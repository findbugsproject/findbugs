package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.CompactLocationNumbering;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.ba.npe2.DefinitelyNullSetAnalysis;
import edu.umd.cs.findbugs.ba.npe2.DefinitelyNullSetDataflow;
import edu.umd.cs.findbugs.ba.vna.ValueNumberDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class DefinitelyNullSetDataflowFactory extends DataflowAnalysisFactory<DefinitelyNullSetDataflow> {
    public DefinitelyNullSetDataflowFactory() {
	    super("definitely null set dataflow", DefinitelyNullSetDataflow.class);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	CFG cfg = getCFG(analysisCache, descriptor);
    	DepthFirstSearch  dfs = getDepthFirstSearch(analysisCache, descriptor);
    	ValueNumberDataflow vnaDataflow = getValueNumberDataflow(analysisCache, descriptor);
    	CompactLocationNumbering compactLocationNumbering = getCompactLocationNumbering(analysisCache, descriptor);

    	DefinitelyNullSetAnalysis analysis = new DefinitelyNullSetAnalysis(dfs, vnaDataflow, compactLocationNumbering);
    	DefinitelyNullSetDataflow dataflow = new DefinitelyNullSetDataflow(cfg, analysis);

    	dataflow.execute();

    	return dataflow;
    }
    
//    /* (non-Javadoc)
//     * @see edu.umd.cs.findbugs.ba.ClassContext.AnalysisFactory#analyze(org.apache.bcel.classfile.Method)
//     */
//    @Override
//    protected DefinitelyNullSetDataflow analyze(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//    
//    	CFG cfg = getCFG(jclass, method);
//    	DepthFirstSearch  dfs = getDepthFirstSearch(jclass, method);
//    	ValueNumberDataflow vnaDataflow = getValueNumberDataflow(jclass, method);
//    	CompactLocationNumbering compactLocationNumbering = getCompactLocationNumbering(jclass, method);
//    
//    	DefinitelyNullSetAnalysis analysis = new DefinitelyNullSetAnalysis(dfs, vnaDataflow, compactLocationNumbering);
//    	DefinitelyNullSetDataflow dataflow = new DefinitelyNullSetDataflow(cfg, analysis);
//    
//    	dataflow.execute();
//    
//    	return dataflow;
//    }
}
