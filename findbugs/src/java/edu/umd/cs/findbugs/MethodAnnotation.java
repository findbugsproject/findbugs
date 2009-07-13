/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003-2005, University of Maryland
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

import java.io.IOException;

import edu.umd.cs.findbugs.signature.SignatureConverter;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.xml.XMLAttributeList;
import edu.umd.cs.findbugs.xml.XMLOutput;

/**
 * A BugAnnotation specifying a particular method in a particular class.
 * A MethodAnnotation may (optionally) have a SourceLineAnnotation directly
 * embedded inside it to indicate the range of source lines where the
 * method is defined.
 *
 * @author David Hovemeyer
 * @see BugAnnotation
 */
public class MethodAnnotation extends PackageMemberAnnotation implements IMethodAnnotation {
	private static final long serialVersionUID = 1L;

	private static final boolean UGLY_METHODS = SystemProperties.getBoolean("ma.ugly");

	private static final String DEFAULT_ROLE = "METHOD_DEFAULT";

	private final String methodName;
	private final String methodSig;
	private String fullMethod;
	private final boolean isStatic;

	/**
	 * Constructor.
	 *
	 * @param className  the name of the class containing the method
	 * @param methodName the name of the method
	 * @param methodSig  the Java type signature of the method
	 * @param isStatic   true if the method is static, false if not
	 */
	public MethodAnnotation(String className, String methodName, String methodSig, boolean isStatic) {
		super(className, DEFAULT_ROLE);
		this.methodName = methodName;
		if (methodSig.indexOf(".") >= 0) {
			assert false : "signatures should not be dotted: " + methodSig;
		methodSig = methodSig.replace('.','/');
		}
		this.methodSig = methodSig;
		this.isStatic = isStatic;
	}

	/**
	 * Get the method name.
	 */
	public String getMethodName() {
		return methodName;
	}

	public String getJavaSourceMethodName() {
		if (methodName.equals("<clinit>")) {
			return "<static initializer>";
		}
		if (methodName.equals("<init>")) {
			String result = getClassName();
			int pos = Math.max(result.lastIndexOf('$'),result.lastIndexOf('.'));
			return className.substring(pos+1);
		}
		return methodName;
	}
	/**
	 * Get the method type signature.
	 */
	public String getMethodSignature() {
		return methodSig;
	}

	/**
	 * Return whether or not the method is static.
	 * 
	 * @return true if the method is static, false otherwise
	 */
	public boolean isStatic() {
		return isStatic;
	}

	public void accept(BugAnnotationVisitor visitor) {
		visitor.visitMethodAnnotation(this);
	}

	@Override
	protected String formatPackageMember(String key, IClassAnnotation primaryClass) {
		if (key.equals("")) {
			return UGLY_METHODS ? getUglyMethod() : getFullMethod(primaryClass);
		} else if (key.equals("givenClass")) {
			if (methodName.equals("<init>")) {
				return "new " + shorten(primaryClass.getPackageName(), className) + getSignatureInClass(primaryClass);
			}
			if (className.equals(primaryClass.getClassName())) {
				return getNameInClass(primaryClass);
			}
			return shorten(primaryClass.getPackageName(), className) + "." + getNameInClass(primaryClass);
		}
		else if (key.equals("name")) {
			return methodName;
		}
		else if (key.equals("nameAndSignature")) {
			return getNameInClass(primaryClass);
		} else if (key.equals("shortMethod") ) {
			return className + "." + methodName + "(...)";
		} else if (key.equals("hash")){
			String tmp= getNameInClass(false, true, true);

			return className + "." + tmp;
		}
		else if (key.equals("returnType")) {
			int i = methodSig.indexOf(')');
			String returnType = methodSig.substring(i+1);
			String pkgName = primaryClass == null ? "" : primaryClass.getPackageName();
			SignatureConverter converter = new SignatureConverter(returnType);
			return shorten(pkgName, converter.parseNext());
		} else {
			throw new IllegalArgumentException("unknown key " + key);
		}
	}

	/**
	 * Get the "full" method name.
	 * This is a format which looks sort of like a method signature
	 * that would appear in Java source code.
	 * @param primaryClass TODO
	 */
	public String getNameInClass(IClassAnnotation primaryClass) {
		return  getNameInClass(true, false, false, false);
	}
	public String getSignatureInClass(IClassAnnotation primaryClass) {
		return  getNameInClass(true, false, false, true);
	}

	public String getNameInClass(boolean shortenPackages, boolean useJVMMethodName, boolean hash) {
		return getNameInClass(shortenPackages, useJVMMethodName, hash, false);
	}
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
	public String getNameInClass(boolean shortenPackages, boolean useJVMMethodName, boolean hash, boolean omitMethodName) {
		// Convert to "nice" representation
		StringBuilder result = new StringBuilder();
		if (!omitMethodName) {
			if (useJVMMethodName) {
				result.append(getMethodName());
			} else {
				result.append(getJavaSourceMethodName());
			}
		}
		result.append('(');

		// append args
		SignatureConverter converter = new SignatureConverter(methodSig);

		if (converter.getFirst() != '(') {
			throw new IllegalStateException("bad method signature " + methodSig);
		}
		converter.skip();

		boolean needsComma = false;
		while (converter.getFirst() != ')') {
			if (needsComma) {
				if (hash) {
					result.append(",");
				} else {
					result.append(", ");
				}
			}
			if (shortenPackages) {
				result.append(removePackageName(converter.parseNext()));
			} else {
				result.append(converter.parseNext());
			}
			needsComma = true;
		}
		converter.skip();

		result.append(')');
		return result.toString();
	}


	/**
	 * Get the "full" method name.
	 * This is a format which looks sort of like a method signature
	 * that would appear in Java source code.
	 * @param primaryClass TODO
	 */
	public String getFullMethod(IClassAnnotation primaryClass) {
		if (fullMethod == null) {
			if (methodName.equals("<init>")) {
				fullMethod = "new " + stripJavaLang(className) + getSignatureInClass(primaryClass);
			} else {
				fullMethod = stripJavaLang(className) + "." + getNameInClass(primaryClass);
			}
		}

		return fullMethod;
	}

	public String stripJavaLang(@DottedClassName String dottedClassName) {
		if (dottedClassName.startsWith("java.lang.")) {
			return dottedClassName.substring(10);
		}
		return dottedClassName;
	}

	private String getUglyMethod() {
		return className + "." + methodName + " : " + methodSig.replace('/', '.');
	}

	@Override
	public int hashCode() {
		return className.hashCode() + methodName.hashCode() + methodSig.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IMethodAnnotation)) {
			return false;
		}
		IMethodAnnotation other = (IMethodAnnotation) o;
		return className.equals(other.getClassName())
		&& methodName.equals(other.getMethodName())
		&& methodSig.equals(other.getMethodSignature());
	}

	public int compareTo(BugAnnotation o) {
		if (!(o instanceof IMethodAnnotation)) {
			return this.getClass().getName().compareTo(o.getClass().getName());
		}
		IMethodAnnotation other = (IMethodAnnotation) o;
		int cmp;
		cmp = className.compareTo(other.getClassName());
		if (cmp != 0) {
			return cmp;
		}
		cmp = methodName.compareTo(other.getMethodName());
		if (cmp != 0) {
			return cmp;
		}
		return methodSig.compareTo(other.getMethodSignature());
	}

	/* ----------------------------------------------------------------------
	 * XML Conversion support
	 * ---------------------------------------------------------------------- */

	private static final String ELEMENT_NAME = "Method";



	public void writeXML(XMLOutput xmlOutput) throws IOException {
	}

	public void writeXML(XMLOutput xmlOutput, boolean addMessages, boolean isPrimary) throws IOException {
		XMLAttributeList attributeList = new XMLAttributeList()
		.addAttribute("classname", getClassName())
		.addAttribute("name", getMethodName())
		.addAttribute("signature", getMethodSignature())
		.addAttribute("isStatic", String.valueOf(isStatic()));
		if (isPrimary) {
			attributeList.addAttribute("primary", "true");
		}

		String role = getDescription();
		if (!role.equals(DEFAULT_ROLE)) {
			attributeList.addAttribute("role", role);
		}

		if (sourceLines == null && !addMessages) {
			xmlOutput.openCloseTag(ELEMENT_NAME, attributeList);
		} else {
			xmlOutput.openTag(ELEMENT_NAME, attributeList);
			if (sourceLines != null) {
				sourceLines.writeXML(xmlOutput);
			}
			if (addMessages) {
				xmlOutput.openTag(MESSAGE_TAG);
				xmlOutput.writeText(this.toString());
				xmlOutput.closeTag(MESSAGE_TAG);
			}
			xmlOutput.closeTag(ELEMENT_NAME);
		}
	}

	@Override
	public boolean isSignificant() {
		String role = getDescription();
		if (METHOD_DANGEROUS_TARGET.equals(role)
				|| METHOD_DANGEROUS_TARGET_ACTUAL_GUARANTEED_NULL.equals(role)
				|| METHOD_SAFE_TARGET.equals(role)
				|| METHOD_EQUALS_USED.equals(role)) {
			return false;
		}
		return true;
	}
}
