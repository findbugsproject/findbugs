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
public interface ILocalVariableAnnotation extends BugAnnotation {

	String DEFAULT_ROLE = "LOCAL_VARIABLE_DEFAULT";

	String NAMED_ROLE = "LOCAL_VARIABLE_NAMED";

	String UNKNOWN_ROLE = "LOCAL_VARIABLE_UNKNOWN";

	String PARAMETER_ROLE = "LOCAL_VARIABLE_PARAMETER";

	String PARAMETER_NAMED_ROLE = "LOCAL_VARIABLE_PARAMETER_NAMED";

	String PARAMETER_VALUE_SOURCE_ROLE = "LOCAL_VARIABLE_PARAMETER_VALUE_SOURCE";

	String PARAMETER_VALUE_SOURCE_NAMED_ROLE = "LOCAL_VARIABLE_PARAMETER_VALUE_SOURCE_NAMED";

	String VALUE_DOOMED_ROLE = "LOCAL_VARIABLE_VALUE_DOOMED";

	String VALUE_DOOMED_NAMED_ROLE = "LOCAL_VARIABLE_VALUE_DOOMED_NAMED";

	String DID_YOU_MEAN_ROLE = "LOCAL_VARIABLE_DID_YOU_MEAN";

	String INVOKED_ON_ROLE = "LOCAL_VARIABLE_INVOKED_ON";

	String ARGUMENT_ROLE = "LOCAL_VARIABLE_ARGUMENT";

	String VALUE_OF_ROLE = "LOCAL_VARIABLE_VALUE_OF";

	boolean isNamed();

	/**
	 * @return name of local variable
	 */
	String getName();

	int getPC();

	int getRegister();

}
