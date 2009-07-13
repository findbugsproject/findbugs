/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003-2008, University of Maryland
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

import java.io.File;
import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.SourceFinder;
import edu.umd.cs.findbugs.xml.XMLAttributeList;
import edu.umd.cs.findbugs.xml.XMLOutput;

/**
 * A BugAnnotation that records a range of source lines
 * in a class.
 *
 * @author David Hovemeyer
 * @see BugAnnotation
 */
public class SourceLineAnnotation implements ISourceLineAnnotation {
	private static final long serialVersionUID = 1L;

	private String description;
	final private String className;
	private String sourceFile;
	final private int startLine;
	final private int endLine;
	final private int startBytecode;
	final private int endBytecode;
	private boolean synthetic;

	/**
	 * Constructor.
	 *
	 * @param className     the class to which the line number(s) refer
	 * @param sourceFile    the name of the source file
	 * @param startLine     the first line (inclusive)
	 * @param endLine       the ending line (inclusive)
	 * @param startBytecode the first bytecode offset (inclusive)
	 * @param endBytecode   the end bytecode offset (inclusive)
	 */
	public SourceLineAnnotation(@NonNull String className, @NonNull String sourceFile, int startLine, int endLine,
			int startBytecode, int endBytecode) {
		if (className == null) {
			throw new IllegalArgumentException("class name is null");
		}
		if (sourceFile == null) {
			throw new IllegalArgumentException("source file is null");
		}
		this.description = DEFAULT_ROLE;
		this.className = className;
		this.sourceFile = sourceFile;
		this.startLine = startLine;
		this.endLine = endLine;
		this.startBytecode = startBytecode;
		this.endBytecode = endBytecode;
	}


	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Factory method to create an unknown source line annotation.
	 *
	 * @param className the class name
	 * @param sourceFile the source file name
	 * @return the SourceLineAnnotation
	 */
	public static ISourceLineAnnotation createUnknown(String className, String sourceFile) {
		return createUnknown(className, sourceFile, -1, -1);
	}

	/**
	 * Factory method to create an unknown source line annotation.
	 * This doesn't use the analysis context.
	 *
	 * @param className the class name
	 * @return the SourceLineAnnotation
	 */
	public static ISourceLineAnnotation createReallyUnknown(String className) {
		return createUnknown(
				className,
				ISourceLineAnnotation.UNKNOWN_SOURCE_FILE,
				-1,
				-1);
	}

	/**
	 * Factory method to create an unknown source line annotation.
	 * This variant is used when bytecode offsets are known,
	 * but not source lines.
	 *
	 * @param className the class name
	 * @param sourceFile the source file name
	 * @return the SourceLineAnnotation
	 */
	public static ISourceLineAnnotation createUnknown(String className, String sourceFile, int startBytecode, int endBytecode) {
		ISourceLineAnnotation result = new SourceLineAnnotation(className, sourceFile, -1, -1, startBytecode, endBytecode);
		// result.setDescription("SOURCE_LINE_UNKNOWN");
		return result;
	}

	/**
	 * Get the class name.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Get the source file name.
	 */
	public String getSourceFile() {
		return sourceFile;
	}

	/**
	 * Is the source file known?
	 */
	public boolean isSourceFileKnown() {
		return !sourceFile.equals(UNKNOWN_SOURCE_FILE);
	}

	/**
	 * Set the source file name.
	 *
	 * @param sourceFile the source file name
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}


	/**
	 * Get the simple class name (the part of the name after the dot)
	 */
	public String getSimpleClassName() {
		int lastDot = className.lastIndexOf('.');
		return className.substring(lastDot+1);
	}

	/**
	 * Get the package name.
	 */
	public String getPackageName() {
		int lastDot = className.lastIndexOf('.');
		if (lastDot < 0) {
			return "";
		}
		return className.substring(0, lastDot);
	}

	/**
	 * Get the start line (inclusive).
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * Get the ending line (inclusive).
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * Get start bytecode (inclusive).
	 */
	public int getStartBytecode() {
		return startBytecode;
	}

	/**
	 * Get end bytecode (inclusive).
	 */
	public int getEndBytecode() {
		return endBytecode;
	}

	/**
	 * Is this an unknown source line annotation?
	 */
	public boolean isUnknown() {
		return startLine < 0 || endLine < 0;
	}

	public void accept(BugAnnotationVisitor visitor) {
		visitor.visitSourceLineAnnotation(this);
	}

	public String format(String key, IClassAnnotation primaryClass) {
		if (key.equals("hash")) {
			return "";
		}
		if (key.equals("")) {
			StringBuilder buf = new StringBuilder();
			buf.append(sourceFile);
			appendLines(buf);
			return buf.toString();
		} else if (key.equals("lineNumber")) {
			StringBuilder buf = new StringBuilder();
			appendLinesRaw(buf);
			return buf.toString();
		} else if (key.equals("full")) {
			StringBuilder buf = new StringBuilder();
			String pkgName = getPackageName();
			if (!pkgName.equals("")) {
				buf.append(pkgName.replace('.', CANONICAL_PACKAGE_SEPARATOR));
				buf.append(CANONICAL_PACKAGE_SEPARATOR);
			}
			buf.append(sourceFile);
			appendLines(buf);
			return buf.toString();
		} else {
			throw new IllegalArgumentException("Unknown format key " + key);
		}
	}

	private void appendLines(StringBuilder buf) {
		if (isUnknown()) {
			return;
		}
		buf.append(":[");
		appendLinesRaw(buf);
		buf.append(']');
	}

	private void appendLinesRaw(StringBuilder buf) {
		if (isUnknown()) {
			return;
		}
		if (startLine == endLine) {
			buf.append("line ");
			buf.append(startLine);
		} else {
			buf.append("lines ");
			buf.append(startLine);
			buf.append('-');
			buf.append(endLine);
		}

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description.intern();
	}

	@Override
	public String toString() {
		String desc = description;
		if (desc.equals(DEFAULT_ROLE) && isUnknown()) {
			desc = DEFAULT_ROLE_UNKNOWN_LINE;
		}
		String pattern = I18N.instance().getAnnotationDescription(desc);
		FindBugsMessageFormat format = new FindBugsMessageFormat(pattern);
		return format.format(new BugAnnotation[]{this}, null);
	}

	public int compareTo(BugAnnotation o) {
		if (!(o instanceof ISourceLineAnnotation)) {
			return this.getClass().getName().compareTo(o.getClass().getName());
		}

		ISourceLineAnnotation other = (ISourceLineAnnotation) o;

		int cmp = className.compareTo(other.getClassName());
		if (cmp != 0) {
			return cmp;
		}
		cmp = startLine - other.getStartLine();
		if (cmp != 0) {
			return cmp;
		}
		cmp = endLine - other.getEndLine();
		if (startLine != -1) {
			return 0;
		}
		if (cmp != 0) {
			return cmp;
		}
		cmp = startBytecode - other.getStartBytecode();
		if (cmp != 0) {
			return cmp;
		}
		return endBytecode - other.getEndBytecode();
	}

	@Override
	public int hashCode() {
		if (startLine != -1) {
			return className.hashCode() + startLine + 3 * endLine;
		}
		return className.hashCode() + startBytecode + 3 * endBytecode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ISourceLineAnnotation)) {
			return false;
		}
		ISourceLineAnnotation other = (ISourceLineAnnotation) o;
		if (startLine != -1) {
			return className.equals(other.getClassName())
			&& startLine == other.getStartLine()
			&& endLine == other.getEndLine();
		}
		return className.equals(other.getClassName())
		&& startBytecode == other.getStartBytecode()
		&& endBytecode == other.getEndBytecode();

	}

	/* ----------------------------------------------------------------------
	 * XML Conversion support
	 * ---------------------------------------------------------------------- */

	private static final String ELEMENT_NAME = "SourceLine";


	public void writeXML(XMLOutput xmlOutput) throws IOException {
		writeXML(xmlOutput, false, false);
	}

	static final ThreadLocal<SourceFinder> sourceFinder = new ThreadLocal<SourceFinder>();
	static final ThreadLocal<String> relativeSourceBase = new ThreadLocal<String>();

	public static void generateRelativeSource(File relativeSourceBase1, Project project) {
		try {
			SourceLineAnnotation.relativeSourceBase.set(relativeSourceBase1.getCanonicalPath());
			SourceFinder mySourceFinder  = new SourceFinder(project);
			sourceFinder.set(mySourceFinder);
		} catch (IOException e) {
			AnalysisContext.logError("Error resolving relative source base " + relativeSourceBase1, e);
		}
	}

	public static void clearGenerateRelativeSource() {
		sourceFinder.remove();
		relativeSourceBase.remove();
	}

	public void writeXML(XMLOutput xmlOutput, boolean addMessages, boolean isPrimary) throws IOException {
		String classname = getClassName();
		String sourcePath = getSourcePath();

		XMLAttributeList attributeList = new XMLAttributeList()
		.addAttribute("classname", classname);
		if (isPrimary) {
			attributeList.addAttribute("primary", "true");
		}

		int n = getStartLine(); // start/end are now optional (were too many "-1"s in the xml)
		if (n >= 0) {
			attributeList.addAttribute("start", String.valueOf(n));
		}
		n = getEndLine();
		if (n >= 0) {
			attributeList.addAttribute("end", String.valueOf(n));
		}
		n = getStartBytecode(); // startBytecode/endBytecode haven't been set for a while now
		if (n >= 0) {
			attributeList.addAttribute("startBytecode", String.valueOf(n));
		}
		n = getEndBytecode();
		if (n >= 0) {
			attributeList.addAttribute("endBytecode", String.valueOf(n));
		}

		if (isSourceFileKnown()) {
			attributeList.addAttribute("sourcefile", sourceFile);
			attributeList.addAttribute("sourcepath", sourcePath);
			SourceFinder mySourceFinder = sourceFinder.get();
			if (mySourceFinder != null) {
				try {
					String fullPath = new File(mySourceFinder.findSourceFile(this).getFullFileName()).getCanonicalPath();
					String myRelativeSourceBase = relativeSourceBase.get();
					if (fullPath.startsWith(myRelativeSourceBase)) {
						attributeList.addAttribute("relSourcepath", fullPath.substring(myRelativeSourceBase.length()+1));
					}
				} catch (IOException e) {
					assert true;
				}

			}
		}

		String role = getDescription();
		if (!role.equals(DEFAULT_ROLE)) {
			attributeList.addAttribute("role", getDescription());
		}
		if (synthetic) {
			attributeList.addAttribute("synthetic", "true");
		}
		if (addMessages) {
			xmlOutput.openTag(ELEMENT_NAME, attributeList);
			xmlOutput.openTag("Message");
			xmlOutput.writeText(this.toString());
			xmlOutput.closeTag("Message");
			xmlOutput.closeTag(ELEMENT_NAME);
		} else {
			xmlOutput.openCloseTag(ELEMENT_NAME, attributeList);
		}
	}


	public String getSourcePath() {
		String classname = getClassName();
		String packageName = "";
		if (classname.indexOf('.') > 0) {
			packageName = classname.substring(0,1+classname.lastIndexOf('.'));
		}
		String sourcePath = packageName.replace('.', CANONICAL_PACKAGE_SEPARATOR)+sourceFile;
		return sourcePath;
	}

	/**
	 * @param synthetic The synthetic to set.
	 */
	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	/**
	 * @return Returns the synthetic.
	 */
	public boolean isSynthetic() {
		return synthetic;
	}


	public boolean isSignificant() {
		return false;
	}

	public String toString(IClassAnnotation primaryClass) {
		return toString();
	}
}
