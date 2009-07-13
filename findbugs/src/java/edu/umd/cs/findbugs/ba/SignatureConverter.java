/*
 * Bytecode Analysis Framework
 * Copyright (C) 2003,2004 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.ba;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;

/**
 * Convert part or all of a Java type signature into something
 * closer to what types look like in the source code.
 *
 * @author David Hovemeyer
 */
public class SignatureConverter extends edu.umd.cs.findbugs.signature.SignatureConverter {

	/**
	 * Never call me, I'm utility class!
	 */
	private SignatureConverter() {
		super("");
	}

	/**
	 * Convenience method for generating a method signature in
	 * human readable form.
	 *
	 * @param javaClass the class
	 * @param method    the method
	 */
	public static String convertMethodSignature(JavaClass javaClass, Method method) {
		return convertMethodSignature(javaClass.getClassName(), method.getName(), method.getSignature());
	}

	/**
	 * Convenience method for generating a method signature in
	 * human readable form.
	 *
	 * @param methodGen the method to produce a method signature for
	 */
	public static String convertMethodSignature(MethodGen methodGen) {
		return convertMethodSignature(methodGen.getClassName(), methodGen.getName(), methodGen.getSignature());
	}

	/**
	 * Convenience method for generating a method signature in
	 * human readable form.
	 *
	 * @param inv an InvokeInstruction
	 * @param cpg the ConstantPoolGen for the class the instruction belongs to
	 */
	public static String convertMethodSignature(InvokeInstruction inv, ConstantPoolGen cpg) {
		return convertMethodSignature(inv.getClassName(cpg), inv.getName(cpg), inv.getSignature(cpg));
	}

	/**
	 * Convenience method for generating a method signature in
	 * human readable form.
	 * 
	 * @param xmethod an XMethod
	 * @return the formatted version of that signature
	 */
	public static String convertMethodSignature(XMethod xmethod) {
		@DottedClassName String className = xmethod.getClassName();
		assert className.indexOf('/') == -1;
		return convertMethodSignature(className, xmethod.getName(), xmethod.getSignature());
	}

	/**
	 * Convenience method for generating a method signature in
	 * human readable form.
	 * 
     * @param methodDescriptor a MethodDescriptor
     * @return the formatted version of that signature
     */
    public static String convertMethodSignature(MethodDescriptor methodDescriptor) {
    	return convertMethodSignature(
    			methodDescriptor.getClassDescriptor().toDottedClassName(),
    			methodDescriptor.getName(),
    			methodDescriptor.getSignature());
    }

}

