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

/**
 * @author Andrei
 */
public interface ISourceLineAnnotation extends BugAnnotation {

	String DEFAULT_ROLE = "SOURCE_LINE_DEFAULT";

	String DEFAULT_ROLE_UNKNOWN_LINE = "SOURCE_LINE_DEFAULT_UNKNOWN_LINE";

	String ROLE_ANOTHER_INSTANCE = "SOURCE_LINE_ANOTHER_INSTANCE";

	String ROLE_CALLED_FROM_SUPERCLASS_AT = "SOURCE_LINE_CALLED_FROM_SUPERCLASS_AT";

	String ROLE_FIELD_SET_TOO_LATE_AT = "SOURCE_LINE_FIELD_SET_TOO_LATE_AT";

	String ROLE_GENERATED_AT = "SOURCE_LINE_GENERATED_AT";

	String ROLE_OBLIGATION_CREATED = "SOURCE_LINE_OBLIGATION_CREATED";

	String ROLE_OBLIGATION_CREATED_BY_WILLCLOSE_PARAMETER = "SOURCE_LINE_OBLIGATION_CREATED_BY_WILLCLOSE_PARAMETER";

	String ROLE_PATH_CONTINUES = "SOURCE_LINE_PATH_CONTINUES";

	/**
	 * String returned if the source file is unknown.
	 * This must match what BCEL uses when the source file is unknown.
	 */
	String UNKNOWN_SOURCE_FILE = "<Unknown>";

	char CANONICAL_PACKAGE_SEPARATOR = '/';

	String DESCRIPTION_LAST_CHANGE = "SOURCE_LINE_LAST_CHANGE";

	String DESCRIPTION_LOOP_BOTTOM = "SOURCE_LINE_LOOP_BOTTOM";

	/**
	 * Get the class name.
	 */
	String getClassName();

	/**
	 * Get the source file name.
	 */
	String getSourceFile();

	/**
	 * Is the source file known?
	 */
	boolean isSourceFileKnown();

	/**
	 * Set the source file name.
	 *
	 * @param sourceFile the source file name
	 */
	void setSourceFile(String sourceFile);

	/**
	 * Get the simple class name (the part of the name after the dot)
	 */
	String getSimpleClassName();

	/**
	 * Get the package name.
	 */
	String getPackageName();

	/**
	 * Get the start line (inclusive).
	 */
	int getStartLine();

	/**
	 * Get the ending line (inclusive).
	 */
	int getEndLine();

	/**
	 * Get start bytecode (inclusive).
	 */
	int getStartBytecode();

	/**
	 * Get end bytecode (inclusive).
	 */
	int getEndBytecode();

	/**
	 * Is this an unknown source line annotation?
	 */
	boolean isUnknown();

	String getSourcePath();

	/**
	 * @param synthetic The synthetic to set.
	 */
	void setSynthetic(boolean synthetic);

	/**
	 * @return Returns the synthetic.
	 */
	boolean isSynthetic();

}
