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
public interface IClassAnnotation extends IPackageMemberAnnotation {

	public static final String SUBCLASS_ROLE = "CLASS_SUBCLASS";

	public static final String SUPERCLASS_ROLE = "CLASS_SUPERCLASS";

	public static final String IMPLEMENTED_INTERFACE_ROLE = "CLASS_IMPLEMENTED_INTERFACE";

	public static final String INTERFACE_ROLE = "INTERFACE_TYPE";

	public static final String ANNOTATION_ROLE = "CLASS_ANNOTATION";

	boolean contains(IClassAnnotation other);

	IClassAnnotation getTopLevelClass();

}
