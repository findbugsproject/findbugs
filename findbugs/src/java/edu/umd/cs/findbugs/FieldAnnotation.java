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

import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.SourceInfoMap;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.signature.SignatureConverter;
import edu.umd.cs.findbugs.xml.XMLAttributeList;
import edu.umd.cs.findbugs.xml.XMLOutput;

/**
 * A BugAnnotation specifying a particular field in particular class.
 *
 * @author David Hovemeyer
 * @see BugAnnotation
 */
public class FieldAnnotation extends PackageMemberAnnotation implements IFieldAnnotation {
	private static final long serialVersionUID = 1L;

	private final String fieldName;
	private final String fieldSig;
	private String fieldSourceSig;
	private final boolean isStatic;

	/**
	 * Constructor.
	 *
	 * @param className the name of the class containing the field
	 * @param fieldName the name of the field
	 * @param fieldSig  the type signature of the field
	 */
	public FieldAnnotation(@DottedClassName String className, String fieldName, String fieldSig, boolean isStatic) {
		super(className, DEFAULT_ROLE);
		if (fieldSig.indexOf(".") >= 0) {
			assert false : "signatures should not be dotted: " + fieldSig;
		fieldSig = fieldSig.replace('.','/');
		}
		this.fieldName = fieldName;
		this.fieldSig = fieldSig;
		this.isStatic = isStatic;
	}

	public FieldAnnotation(@DottedClassName String className, String fieldName, String fieldSig, String fieldSourceSig, boolean isStatic) {
		this(className, fieldName, fieldSig, isStatic);
		this.fieldSourceSig = fieldSourceSig;
	}

	/**
	 * Get the field name.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Get the type signature of the field.
	 */
	public String getFieldSignature() {
		return fieldSig;
	}

	/**
	 * Return whether or not the field is static.
	 */
	public boolean isStatic() {
		return isStatic;
	}

	public void accept(BugAnnotationVisitor visitor) {
		visitor.visitFieldAnnotation(this);
	}

	@Override
	protected String formatPackageMember(String key, IClassAnnotation primaryClass) {
		if (key.equals("") || key.equals("hash")) {
			return className + "." + fieldName;
		} else if (key.equals("givenClass")) {
			String primaryClassName = primaryClass.getClassName();
			if (className.equals(primaryClassName)) {
				return getNameInClass(primaryClass);
			}
			return shorten(primaryClass.getPackageName(), className) + "." + fieldName;
		}
		else if (key.equals("name")) {
			return fieldName;
		} else if (key.equals("fullField")) {
			SignatureConverter converter = new SignatureConverter(fieldSig);
			StringBuilder result = new StringBuilder();
			if (isStatic) {
				result.append("static ");
			}
			result.append(converter.parseNext());
			result.append(' ');
			result.append(className);
			result.append('.');
			result.append(fieldName);
			return result.toString();
		} else {
			throw new IllegalArgumentException("unknown key " + key);
		}
	}

	/**
	 * @param primaryClass
	 * @return
	 */
	private String getNameInClass(IClassAnnotation primaryClass) {
		if (primaryClass == null) {
			return   className + "." + fieldName;
		}
		String givenPackageName = primaryClass.getPackageName();
		String thisPackageName = this.getPackageName();
		if (thisPackageName.equals(givenPackageName)) {
			if (thisPackageName.length() == 0) {
				return fieldName;
			}
			return className.substring(thisPackageName.length() + 1) +"." + fieldName;
		}
		return   className + "." + fieldName;
	}

	@Override
	public int hashCode() {
		return className.hashCode() + fieldName.hashCode() + fieldSig.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IFieldAnnotation)) {
			return false;
		}
		IFieldAnnotation other = (IFieldAnnotation) o;
		return className.equals(other.getClassName())
		&& fieldName.equals(other.getFieldName())
		&& fieldSig.equals(other.getFieldSignature())
		&& isStatic == other.isStatic();
	}

	public int compareTo(BugAnnotation o) {
		if (!(o instanceof IFieldAnnotation)) {
			return this.getClass().getName().compareTo(o.getClass().getName());
		}
		IFieldAnnotation other = (IFieldAnnotation) o;
		int cmp;
		cmp = className.compareTo(other.getClassName());
		if (cmp != 0) {
			return cmp;
		}
		cmp = fieldName.compareTo(other.getFieldName());
		if (cmp != 0) {
			return cmp;
		}
		return fieldSig.compareTo(other.getFieldSignature());
	}

	/**
	 * @return never null, at least "unknown" source line info
	 */
	@Override
	public ISourceLineAnnotation getSourceLines() {
		if (sourceLines == null) {
			/*
			 * TODO Andrei: should move out from this class, because 1) it is already too late to get
			 * the info after AnalysisContext is gone 2) this code belongs to other module (factory).
			 */
			// Create source line annotation for field on demand
			AnalysisContext currentAnalysisContext = AnalysisContext.currentAnalysisContext();
			if (currentAnalysisContext == null) {
				sourceLines = new SourceLineAnnotation(className, sourceFileName, -1, -1, -1, -1);
			} else {
				SourceInfoMap.SourceLineRange fieldLine = currentAnalysisContext
				.getSourceInfoMap().getFieldLine(className, fieldName);
				if (fieldLine == null) {
					sourceLines = new SourceLineAnnotation(className, sourceFileName, -1, -1, -1, -1);
				} else {
					sourceLines = new SourceLineAnnotation(className, sourceFileName,
							fieldLine.getStart().intValue(), fieldLine.getEnd().intValue(), -1, -1);
				}
			}
		}
		return sourceLines;
	}

	/* ----------------------------------------------------------------------
	 * XML Conversion support
	 * ---------------------------------------------------------------------- */

	private static final String ELEMENT_NAME = "Field";

	public void writeXML(XMLOutput xmlOutput) throws IOException {
		writeXML(xmlOutput, false, false);
	}

	public void writeXML(XMLOutput xmlOutput, boolean addMessages, boolean isPrimary) throws IOException {
		XMLAttributeList attributeList = new XMLAttributeList()
		.addAttribute("classname", getClassName())
		.addAttribute("name", getFieldName())
		.addAttribute("signature", getFieldSignature());
		if (fieldSourceSig != null) {
			attributeList.addAttribute("sourceSignature", fieldSourceSig);
		}
		attributeList.addAttribute("isStatic", String.valueOf(isStatic()));
		if (isPrimary) {
			attributeList.addAttribute("primary", "true");
		}


		String role = getDescription();
		if (!role.equals(DEFAULT_ROLE)) {
			attributeList.addAttribute("role", role);
		}

		xmlOutput.openTag(ELEMENT_NAME, attributeList);
		getSourceLines().writeXML(xmlOutput, addMessages, false);
		if (addMessages) {
			xmlOutput.openTag(BugAnnotation.MESSAGE_TAG);
			xmlOutput.writeText(this.toString());
			xmlOutput.closeTag(BugAnnotation.MESSAGE_TAG);
		}
		xmlOutput.closeTag(ELEMENT_NAME);
	}
}
