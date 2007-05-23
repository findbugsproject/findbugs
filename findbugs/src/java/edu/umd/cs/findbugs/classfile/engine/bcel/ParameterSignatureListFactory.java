package edu.umd.cs.findbugs.classfile.engine.bcel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.SignatureParser;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class ParameterSignatureListFactory extends NoExceptionAnalysisFactory<String[]> {
	public ParameterSignatureListFactory() {
		super("parameter signature list factory", (Class<String[]>) new String[0].getClass());// XXX: should have a proper analysis class
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
	 */
	public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
		SignatureParser parser = new SignatureParser(descriptor.getSignature());
		ArrayList<String> resultList = new ArrayList<String>();
		for (Iterator<String> i = parser.parameterSignatureIterator(); i.hasNext();) {
			resultList.add(i.next());
		}
		return resultList.toArray(new String[resultList.size()]);
	}

//	@Override
//	protected String[] analyze(JavaClass jclass, Method method) {
//		SignatureParser parser = new SignatureParser(method.getSignature());
//		ArrayList<String> resultList = new ArrayList<String>();
//		for (Iterator<String> i = parser.parameterSignatureIterator(); i.hasNext();) {
//			resultList.add(i.next());
//		}
//		return resultList.toArray(new String[resultList.size()]);
//	}
}
