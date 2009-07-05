/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003,2004 University of Maryland
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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Singleton responsible for returning localized strings for information
 * returned to the user.
 *
 * @author David Hovemeyer
 */
public class I18N {
	private static final boolean DEBUG = SystemProperties.getBoolean("i18n.debug");
	public static final Locale defaultLocale = Locale.getDefault();

	//private final ResourceBundle bugCategoryDescriptionBundle;
	private final HashMap<String, BugCategory> categoryDescriptionMap;
	private final HashMap<String, BugPattern> bugPatternMap;
	private final HashMap<String, BugCode> bugCodeMap;
	private FailSafeResourceBundle bundle;

	private I18N() {
		categoryDescriptionMap = new HashMap<String, BugCategory>();
		bugPatternMap = new HashMap<String, BugPattern>();
		bugCodeMap = new HashMap<String, BugCode>();
		bundle = new FailSafeResourceBundle(I18N.class.getName(), Locale.ENGLISH);
	}

	private static final I18N theInstance = new I18N();

	/**
	 * Get the single object instance.
	 */
	public static I18N instance() {
		return theInstance;
	}

	/**
	 * Register a BugPattern.
	 *
	 * @param bugPattern the BugPattern
	 */
	public void registerBugPattern(BugPattern bugPattern) {
		bugPatternMap.put(bugPattern.getType(), bugPattern);
	}

	/**
	 * Look up bug pattern.
	 *
	 * @param bugType the bug type for the bug pattern
	 * @return the BugPattern, or null if it can't be found
	 */
	public @CheckForNull BugPattern lookupBugPattern(String bugType) {
		DetectorFactoryCollection.instance(); // ensure detectors loaded
		return bugPatternMap.get(bugType);
	}

	/**
	 * Get an Iterator over all registered bug patterns.
	 */
	public Iterator<BugPattern> bugPatternIterator() {
		DetectorFactoryCollection.instance(); // ensure detectors loaded

		return bugPatternMap.values().iterator();
	}

	/**
	 * Get an Iterator over all registered bug codes.
	 */
	public Iterator<BugCode> bugCodeIterator() {
		DetectorFactoryCollection.instance(); // ensure detectors loaded

		return bugCodeMap.values().iterator();
	}

	/**
	 * Register a BugCode.
	 *
	 * @param bugCode the BugCode
	 */
	public void registerBugCode(BugCode bugCode) {
		bugCodeMap.put(bugCode.getAbbrev(), bugCode);
	}

	/**
	 * Get a short message string.
	 * This is a concrete string (not a format pattern) which briefly describes
	 * the type of bug, without mentioning particular a particular class/method/field.
	 *
	 * @param key which short message to retrieve
	 */
	public @NonNull String getShortMessage(String key) {
		BugPattern bugPattern = bugPatternMap.get(key);
		if (bugPattern == null)
			return bundle.getString("err.missing_pattern") + " " + key;
		return bugPattern.getAbbrev() + ": " + bugPattern.getShortDescription();
	}
	
	public @NonNull String getShortMessageWithoutCode(String key) {
		BugPattern bugPattern = bugPatternMap.get(key);
		if (bugPattern == null)
			return bundle.getString("err.missing_pattern") + " " + key;
		return  bugPattern.getShortDescription();
	}

	/**
	 * Get an HTML document describing the bug pattern for given key in detail.
	 *
	 * @param key which HTML details for retrieve
	 */
	public @NonNull String getDetailHTML(String key) {
		BugPattern bugPattern = bugPatternMap.get(key);
		if (bugPattern == null)
			return bundle.getString("err.missing_pattern") + " " + key;
		return bugPattern.getDetailHTML();
	}

	/**
	 * Get an annotation description string.
	 * This is a format pattern which will describe a BugAnnotation in the
	 * context of a particular bug instance.  Its single format argument
	 * is the BugAnnotation.
	 *
	 * @param key the annotation description to retrieve
	 */
	public String getAnnotationDescription(String key) {
		return bundle.getString(key);
	}

	/**
	 * Get a description for given "bug type".
	 * FIXME: this is referred to elsewhere as the "bug code" or "bug abbrev".
	 * Should make the terminology consistent everywhere.
	 * In this case, the bug type refers to the short prefix code prepended to
	 * the long and short bug messages.
	 *
	 * @param shortBugType the short bug type code
	 * @return the description of that short bug type code means
	 */
	public BugCode getBugCode(String shortBugType) {
		BugCode bugCode = bugCodeMap.get(shortBugType);
		if (bugCode == null)
			throw new IllegalArgumentException("Error: missing bug code for key" + shortBugType);
		return bugCode;
	}
	/**
	 * Get a description for given "bug type".
	 * FIXME: this is referred to elsewhere as the "bug code" or "bug abbrev".
	 * Should make the terminology consistent everywhere.
	 * In this case, the bug type refers to the short prefix code prepended to
	 * the long and short bug messages.
	 *
	 * @param shortBugType the short bug type code
	 * @return the description of that short bug type code means
	 */
	public @NonNull String getBugTypeDescription(String shortBugType) {
		BugCode bugCode = bugCodeMap.get(shortBugType);
		if (bugCode == null)
			return bundle.getString("err.missing_code") + " " + shortBugType;
		return bugCode.getDescription();
	}

	/**
	 * Set the metadata for a bug category.
	 * If the category's metadata has already been set, this does nothing.
	 *
	 * @param category the category key
	 * @param bc the BugCategory object holding the metadata for the category
	 * @return false if the category's metadata has already been set, true otherwise
	 */
	public boolean registerBugCategory(String category, BugCategory bc) {
		if (categoryDescriptionMap.get(category) != null) return false;
		categoryDescriptionMap.put(category, bc);
		return true;
	}

	/**
	 * Get the BugCategory object for a category key.
	 * Returns null if no BugCategory object can be found.
	 *
	 * @param category the category key
	 * @return the BugCategory object (may be null)
	 */
	public BugCategory getBugCategory(String category) {
		return categoryDescriptionMap.get(category);
	}

	/**
	 * Get the description of a bug category.
	 * Returns the category if no description can be found.
	 *
	 * @param category the category
	 * @return the description of the category
	 */
	public String getBugCategoryDescription(String category) {
		BugCategory bc = categoryDescriptionMap.get(category);
		return (bc!=null ? bc.getShortDescription() : category);
	}

	/**
	 * Get a Collection containing all known bug category keys.
	 * E.g., "CORRECTNESS", "MT_CORRECTNESS", "PERFORMANCE", etc.
	 *
	 * @return Collection of bug category keys.
	 */
	public Collection<String> getBugCategories() {
		DetectorFactoryCollection.instance(); // ensure detectors loaded

		return categoryDescriptionMap.keySet(); // backed by the Map
	}

	public Collection<BugCategory> getBugCategoryObjects() {
		DetectorFactoryCollection.instance(); // ensure detectors loaded

		return categoryDescriptionMap.values(); // backed by the Map
	}

	public String getPriorityString(BugInstance bug) {
		//first, get the priority
		int value = bug.getPriority();
		String priorityString;
		if (value == Detector.HIGH_PRIORITY)
			priorityString = bundle.getString("sort.priority_high");
		else if (value == Detector.NORMAL_PRIORITY)
			priorityString = bundle.getString("sort.priority_normal");
		else if (value == Detector.LOW_PRIORITY)
			priorityString = bundle.getString("sort.priority_low");
		else if (value == Detector.EXP_PRIORITY)
			priorityString = bundle.getString("sort.priority_experimental");
		else
			priorityString = bundle.getString("sort.priority_ignore"); // This probably shouldn't ever happen, but what the hell, let's be complete
		return priorityString;
	}

	/**
	 * Get the localized user designation string.
	 * Returns the key if no user designation can be found.
	 *
	 * @param key the user designation key
	 * @return the localized designation string
	 */
	public String getUserDesignation(String key) {
		return bundle.getString(key);
	}

	public static class FailSafeResourceBundle  {

        private ResourceBundle local;
		private final ResourceBundle defaultBundle;

		public FailSafeResourceBundle(String baseName, Locale defaultLocale) {
			defaultBundle = ResourceBundle.getBundle(baseName, defaultLocale);
			try {
				local = ResourceBundle.getBundle(baseName);
			} catch (MissingResourceException e) {
				local = defaultBundle;
			}
        }

        public Enumeration<String> getKeys() {
	        return local.getKeys();
        }

		public String getString(String key) {			
			return getString(key, key);
		}
		
		public String getString(String key, String defaultValue) {
			String value;
			try {
				value = local.getString(key);
			} catch (MissingResourceException e) {
				if(DEBUG){
					return "TRANSLATE(key=" + key + ") (param={0}}";
				}
				try {
					value = defaultBundle.getString(key);
				} catch (MissingResourceException e2) {
					value = defaultValue;
				}
			}
			return value;
		}
	}
}
