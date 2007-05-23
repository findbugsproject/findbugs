package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.BytecodeScanner;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * @author David Hovemeyer
 */
public class UnpackedCodeFactory extends NoExceptionAnalysisFactory<UnpackedCode> {
    public UnpackedCodeFactory() {
	    super("unpacked bytecode", UnpackedCode.class);
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
     */
    public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
    	Method method = getMethod(analysisCache, descriptor);
    	Code code = method.getCode();
    	if (code == null)
    		return null;

    	byte[] instructionList = code.getCode();

    	// Create callback
    	UnpackedBytecodeCallback callback = new UnpackedBytecodeCallback(instructionList.length);

    	// Scan the method.
    	BytecodeScanner scanner = new BytecodeScanner();
    	scanner.scan(instructionList, callback);

    	return callback.getUnpackedCode();
    	
    }

//    @Override
//    protected UnpackedCode analyze(JavaClass jclass, Method method) {
//    
//    	Code code = method.getCode();
//    	if (code == null)
//    		return null;
//    
//    	byte[] instructionList = code.getCode();
//    
//    	// Create callback
//    	UnpackedBytecodeCallback callback = new UnpackedBytecodeCallback(instructionList.length);
//    
//    	// Scan the method.
//    	BytecodeScanner scanner = new BytecodeScanner();
//    	scanner.scan(instructionList, callback);
//    
//    	return callback.getUnpackedCode();
//    }
}
