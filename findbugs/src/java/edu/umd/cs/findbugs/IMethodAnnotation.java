/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
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

package edu.umd.cs.findbugs;

import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;

/**
 * @author Andrei
 */
public interface IMethodAnnotation extends IPackageMemberAnnotation {

	String METHOD_DANGEROUS_TARGET_ACTUAL_GUARANTEED_NULL = "METHOD_DANGEROUS_TARGET_ACTUAL_GUARANTEED_NULL";

	String METHOD_DANGEROUS_TARGET = "METHOD_DANGEROUS_TARGET";

	String METHOD_RETURN_VALUE_OF = "METHOD_RETURN_VALUE_OF";

	String METHOD_SAFE_TARGET = "METHOD_SAFE_TARGET";

	String METHOD_EQUALS_USED = "METHOD_EQUALS_USED";

	String METHOD_CALLED = "METHOD_CALLED";

	String METHOD_SUPERCLASS_CONSTRUCTOR = "METHOD_SUPERCLASS_CONSTRUCTOR";

	String METHOD_CONSTRUCTOR = "METHOD_CONSTRUCTOR";

	String METHOD_OVERRIDDEN = "METHOD_OVERRIDDEN";

	String METHOD_DID_YOU_MEAN_TO_OVERRIDE = "METHOD_DID_YOU_MEAN_TO_OVERRIDE";

	/**
	 * Get the method name.
	 */
	String getMethodName();

	String getJavaSourceMethodName();

	/**
	 * Get the method type signature.
	 */
	String getMethodSignature();

	/**
	 * Return whether or not the method is static.
	 * 
	 * @return true if the method is static, false otherwise
	 */
	boolean isStatic();

	/**
	 * Get the "full" method name.
	 * This is a format which looks sort of like a method signature
	 * that would appear in Java source code.
	 * @param primaryClass TODO
	 */
	String getNameInClass(IClassAnnotation primaryClass);

	String getSignatureInClass(IClassAnnotation primaryClass);

	String getNameInClass(boolean shortenPackages, boolean useJVMMethodName, boolean hash);

	/**
	 * Get the "full" method name.
	 * This is a format which looks sort of like a method signature
	 * that would appear in Java source code.
	 * 
	 * note: If shortenPackeges==true, this will return the same
	 * value as getNameInClass(), except that method caches the
	 * result and this one does not. Calling this one may be slow.
	 * 
	 * @param shortenPackages whether to shorten package names
	 * if they are in java or in the same package as this method.
	 * @param useJVMMethodName TODO
	 * @param hash TODO
	 */
	String getNameInClass(boolean shortenPackages, boolean useJVMMethodName, boolean hash, boolean omitMethodName);

	/**
	 * Get the "full" method name.
	 * This is a format which looks sort of like a method signature
	 * that would appear in Java source code.
	 * @param primaryClass TODO
	 */
	String getFullMethod(IClassAnnotation primaryClass);

	String stripJavaLang(@DottedClassName String className);

}
