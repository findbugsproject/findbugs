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
public interface IFieldAnnotation extends IPackageMemberAnnotation {

	public static final String DEFAULT_ROLE = "FIELD_DEFAULT";

	public static final String DID_YOU_MEAN_ROLE = "FIELD_DID_YOU_MEAN";

	public static final String VALUE_OF_ROLE = "FIELD_VALUE_OF";

	public static final String LOADED_FROM_ROLE = VALUE_OF_ROLE;

	public static final String STORED_ROLE = "FIELD_STORED";

	public static final String INVOKED_ON_ROLE = "FIELD_INVOKED_ON";

	public static final String ARGUMENT_ROLE = "FIELD_ARGUMENT";

	/**
	 * Get the field name.
	 */
	String getFieldName();

	/**
	 * Get the type signature of the field.
	 */
	String getFieldSignature();

	/**
	 * Return whether or not the field is static.
	 */
	boolean isStatic();

}
