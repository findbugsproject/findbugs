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
 * @author pwilliam
 */
public enum UserDesignation {
	NEEDS_STUDY,
	NOT_A_BUG,
	MOSTLY_HARMLESS,
	SHOULD_FIX,
	MUST_FIX,
	I_WILL_FIX,
	BAD_ANALYSIS,
	UNCLASSIFIED,
	OBSOLETE_CODE;

	
	public int score() {
		switch (this) {

		case BAD_ANALYSIS:
			return -3;
		case NOT_A_BUG:
		case OBSOLETE_CODE:
			return  -2;
		case MOSTLY_HARMLESS:
			return -1;
		case SHOULD_FIX:
			return  1;
		case MUST_FIX:
		case I_WILL_FIX:
			return 2;	
		default:
			return 0;
		}
	}
	
	/**
	 * Fail-safe method to get localised user designation name
	 * 
	 * @param ud
	 *            user designation, may be null
	 * @return translated user designation or the name of
	 *         {@link UserDesignation#UNCLASSIFIED} if the given user
	 *         designation is null
	 */
	public static String getLocalisedName(UserDesignation ud){
		String name;
		if(ud == null){
			name = UNCLASSIFIED.name();
		} else {
			name = ud.name();
		}
		return I18N.instance().getUserDesignation(name);
	}
	
	/**
	 * Fail-safe method to get matching user designation instance
	 * 
	 * @param index
	 *            ordinal of user designation, may be out of range
	 * @return matching user designation or {@link UserDesignation#UNCLASSIFIED}
	 *         if the given index is out of range
	 * @see UserDesignation#ordinal()
	 */
	public static UserDesignation getUserDesignation(int index){
		UserDesignation[] values = UserDesignation.values();
		if(index < 0 || index >= values.length){
			return UNCLASSIFIED;
		}
		return values[index];
	}
	
	/**
	 * Fail-safe method to get matching user designation instance
	 * 
	 * @param name
	 *            string with the name of user designation, may be null
	 * @return matching user designation or {@link UserDesignation#UNCLASSIFIED}
	 *         if the given name does not match any known user designation name
	 */
	public static UserDesignation getUserDesignation(String name) {
		if (name == null) {
			return UNCLASSIFIED;
		}
		try {
			return UserDesignation.valueOf(name);
		} catch (IllegalArgumentException e) {
			return UNCLASSIFIED;
		}
	}

	/**
	 * @return all known user designation keys, which can be used to lookup the
	 *         user designations
	 */
	public static String[] getKeys(){
		UserDesignation[] values = UserDesignation.values();
		String [] keys = new String [values.length];
		for (int i = 0; i < keys.length; i++) {
			keys [i] = values[i].name();
        }
		return keys;
	}
}
