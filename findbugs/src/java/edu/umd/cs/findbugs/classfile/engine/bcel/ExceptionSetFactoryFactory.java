package edu.umd.cs.findbugs.classfile.engine.bcel;

import edu.umd.cs.findbugs.ba.type.ExceptionSetFactory;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class ExceptionSetFactoryFactory extends NoExceptionAnalysisFactory<ExceptionSetFactory> {
    public ExceptionSetFactoryFactory() {
	    super("exception set factory", ExceptionSetFactory.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	return new ExceptionSetFactory();
    }

//    @Override
//    protected ExceptionSetFactory analyze(JavaClass jclass, Method method) {
//    	return new ExceptionSetFactory();
//    }
}
