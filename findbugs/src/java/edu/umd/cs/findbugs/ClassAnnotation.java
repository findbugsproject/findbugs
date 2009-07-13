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

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.SourceInfoMap;
import edu.umd.cs.findbugs.internalAnnotations.DottedClassName;
import edu.umd.cs.findbugs.xml.XMLAttributeList;
import edu.umd.cs.findbugs.xml.XMLOutput;

/**
 * A BugAnnotation object specifying a Java class involved in the bug.
 *
 * @author David Hovemeyer
 * @see BugAnnotation
 * @see BugInstance
 */
public class ClassAnnotation extends PackageMemberAnnotation implements IClassAnnotation {
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ROLE = "CLASS_DEFAULT";
	
	/**
	 * Constructor.
	 *
	 * @param className the name of the class
	 */
	public ClassAnnotation(@DottedClassName String className) {
		super(className, DEFAULT_ROLE);
	}

	@Override
	public boolean isSignificant() {
		return !SUBCLASS_ROLE.equals(description);
	}

	public void accept(BugAnnotationVisitor visitor) {
		visitor.visitClassAnnotation(this);
	}

	@Override
	protected String formatPackageMember(String key, IClassAnnotation primaryClass) {
		if (key.equals("") || key.equals("hash"))
			return className;
		else if (key.equals("givenClass"))
			return shorten(primaryClass.getPackageName(), className);
		else if (key.equals("excludingPackage"))
			return shorten(getPackageName(), className);
		else
			throw new IllegalArgumentException("unknown key " + key);
	}

	@Override
	public int hashCode() {
		return className.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IClassAnnotation))
			return false;
		IClassAnnotation other = (IClassAnnotation) o;
		return className.equals(other.getClassName());
	}

	public boolean contains(IClassAnnotation other) {
			return other.getClassName().startsWith(className);
	}
	public IClassAnnotation getTopLevelClass() {
		int firstDollar = className.indexOf('$');
		if (firstDollar <= 0) return this;
		return new ClassAnnotation(className.substring(0,firstDollar));

	}
	public int compareTo(BugAnnotation o) {
		if (!(o instanceof IClassAnnotation)) // BugAnnotations must be Comparable with any type of BugAnnotation
			return this.getClass().getName().compareTo(o.getClass().getName());
		IClassAnnotation other = (IClassAnnotation) o;
		return className.compareTo(other.getClassName());
	}

	@Override
	public ISourceLineAnnotation getSourceLines() {
		if (sourceLines == null) {
	        this.sourceLines = getSourceLinesForClass(className, sourceFileName);
        }
		return sourceLines;
	}

	/**
	 * @return never null, at least "unknown" source line info
	 */
	static ISourceLineAnnotation getSourceLinesForClass(@DottedClassName String className, String sourceFileName) {
		/*
		 * TODO Andrei: should move out from this class, because 1) it is already too late to get
		 * the info after AnalysisContext is gone 2) this code belongs to other module (factory).  
		 */
		// Create source line annotation for class on demand
		AnalysisContext currentAnalysisContext = AnalysisContext.currentAnalysisContext();

		if (currentAnalysisContext == null) {
	        return new SourceLineAnnotation(className, sourceFileName, -1, -1, -1, -1);
        }

		SourceInfoMap.SourceLineRange classLine = currentAnalysisContext.getSourceInfoMap().getClassLine(className);

		if (classLine == null) {
	        return getSourceAnnotationForClass(className, sourceFileName);
        }
		return new SourceLineAnnotation(
				className, sourceFileName, classLine.getStart().intValue(), classLine.getEnd().intValue(), -1, -1);
	}

	private static ISourceLineAnnotation getSourceAnnotationForClass(String className, String sourceFileName) {
		/*
		 * TODO Andrei: should move out from this class, because 1) it is already too late to get
		 * the info after AnalysisContext is gone 2) this code belongs to other module (factory).  
		 */
		int lastLine = -1;
		int firstLine = Integer.MAX_VALUE;

		try {
			JavaClass targetClass = AnalysisContext.currentAnalysisContext().lookupClass(className);
			for (Method m : targetClass.getMethods()) {
				Code c = m.getCode();
				if (c != null) {
					LineNumberTable table = c.getLineNumberTable();
					if (table != null)
						for (LineNumber line : table.getLineNumberTable()) {
							lastLine = Math.max(lastLine, line.getLineNumber());
							firstLine = Math.min(firstLine, line.getLineNumber());
						}
				}
			}
		} catch (ClassNotFoundException e) {
			AnalysisContext.reportMissingClass(e);
		}
		if (firstLine < Integer.MAX_VALUE)
			return new SourceLineAnnotation(className, sourceFileName,
					firstLine, lastLine, -1, -1);
		return   SourceLineAnnotation.createUnknown(className, sourceFileName);
	}
	
	/*
	 * ----------------------------------------------------------------------
	 * XML Conversion support
	 * ----------------------------------------------------------------------
	 */

	private static final String ELEMENT_NAME = "Class";



	
	public void writeXML(XMLOutput xmlOutput) throws IOException {
		writeXML(xmlOutput, false, false);
	}

	public void writeXML(XMLOutput xmlOutput, boolean addMessages, boolean isPrimary) throws IOException {
		XMLAttributeList attributeList = new XMLAttributeList()
			.addAttribute("classname", getClassName());
		if (isPrimary) attributeList.addAttribute("primary", "true");

		String role = getDescription();
		if (!role.equals(DEFAULT_ROLE))
			attributeList.addAttribute("role", role);

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

