/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003-2005 University of Maryland
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
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.cloud.Cloud;
import edu.umd.cs.findbugs.util.Util;
import edu.umd.cs.findbugs.xml.XMLAttributeList;
import edu.umd.cs.findbugs.xml.XMLOutput;

/**
 * An instance of a bug pattern.
 * A BugInstance consists of several parts:
 * <p/>
 * <ul>
 * <li> the type, which is a string indicating what kind of bug it is;
 * used as a key for the FindBugsMessages resource bundle
 * <li> the priority; how likely this instance is to actually be a bug
 * <li> a list of <em>annotations</em>
 * </ul>
 * <p/>
 * The annotations describe classes, methods, fields, source locations,
 * and other relevant context information about the bug instance.
 * Every BugInstance must have at least one ClassAnnotation, which
 * describes the class in which the instance was found.  This is the
 * "primary class annotation".
 * <p/>
 * <p> BugInstance objects are built up by calling a string of <code>add</code>
 * methods.  (These methods all "return this", so they can be chained).
 * Some of the add methods are specialized to get information automatically from
 * a BetterVisitor or DismantleBytecode object.
 *
 * @author David Hovemeyer
 * @see BugAnnotation
 */
public class BugInstance implements Comparable<BugInstance>, XMLWriteableWithMessages, Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private static final String ELEMENT_NAME = "BugInstance";

	private final String type;
	private int priority;
	private final List<BugAnnotation> annotationList;
	private int cachedHashCode;
	private @CheckForNull  BugDesignation userDesignation;
	private BugProperty propertyListHead, propertyListTail;
	private String oldInstanceHash;
	private String instanceHash;
	private int instanceOccurrenceNum;
	private int instanceOccurrenceMax;
	private DetectorFactory detectorFactory;


	/*
	 * The following fields are used for tracking Bug instances across multiple versions of software.
	 * They are meaningless in a BugCollection for just one version of software.
	 */
	private long firstVersion = 0;
	private long lastVersion = -1;
	private boolean introducedByChangeOfExistingClass;
	private boolean removedByChangeOfPersistingClass;

	/**
	 * This value is used to indicate that the cached hashcode
	 * is invalid, and should be recomputed.
	 */
	private static final int INVALID_HASH_CODE = 0;

	/**
	 * This value is used to indicate whether BugInstances should be reprioritized very low,
	 * when the BugPattern is marked as experimental
	 */
	private static boolean adjustExperimental = false;

	private static Set<String> missingBugTypes = Collections.synchronizedSet(new HashSet<String>());

	/**
	 * Constructor.
	 *
	 * @param type     the bug type
	 * @param priority the bug priority
	 */
	public BugInstance(String type, int priority) {
		this.type = type.intern();
		this.priority = priority;
		annotationList = new ArrayList<BugAnnotation>(4);
		cachedHashCode = INVALID_HASH_CODE;

		BugPattern p = I18N.instance().lookupBugPattern(type);
		if (p == null) {
			if (missingBugTypes.add(type)) {
				String msg = "Can't find definition of bug type " + type;
				AnalysisContext.logError(msg, new IllegalArgumentException(msg));
			}
		} else {
			this.priority += p.getPriorityAdjustment();
		}
		if (adjustExperimental && isExperimental()) {
			this.priority = Detector.EXP_PRIORITY;
		}
		boundPriority();
	}

	private void boundPriority() {
		priority = boundedPriority(priority);
	}

	@Override
	public Object clone() {
		BugInstance dup;

		try {
			dup = (BugInstance) super.clone();

			// Do deep copying of mutable objects
			for (int i = 0; i < dup.annotationList.size(); ++i) {
				dup.annotationList.set(i, (BugAnnotation) dup.annotationList.get(i).clone());
			}
			dup.propertyListHead = dup.propertyListTail = null;
			for (Iterator<BugProperty> i = propertyIterator(); i.hasNext(); ) {
				dup.addProperty((BugProperty) i.next().clone());
			}

			return dup;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Create a new BugInstance.
	 * This is the constructor that should be used by Detectors.
	 * 
	 * @param detector the Detector that is reporting the BugInstance
	 * @param type     the bug type
	 * @param priority the bug priority
	 */
	public BugInstance(Detector detector, String type, int priority) {
		this(type, priority);
		if (detector != null) {
			// Adjust priority if required
			detectorFactory =
				DetectorFactoryCollection.instance().getFactoryByClassName(detector.getClass().getName());
			if (detectorFactory != null) {
				this.priority += detectorFactory.getPriorityAdjustment();
				boundPriority();
			}
		}

	}

	/**
	 * Create a new BugInstance.
	 * This is the constructor that should be used by Detectors.
	 * 
	 * @param detector the Detector2 that is reporting the BugInstance
	 * @param type     the bug type
	 * @param priority the bug priority
	 */
	public BugInstance(Detector2 detector, String type, int priority) {
		this(type, priority);

		if (detector != null) {
			// Adjust priority if required
			detectorFactory =
				DetectorFactoryCollection.instance().getFactoryByClassName(detector.getDetectorClassName());
			if (detectorFactory != null) {
				this.priority += detectorFactory.getPriorityAdjustment();
				boundPriority();
			}
		}

	}
	public static void setAdjustExperimental(boolean adjust) {
		adjustExperimental = adjust;
	}

	/* ----------------------------------------------------------------------
	 * Accessors
	 * ---------------------------------------------------------------------- */

	/**
	 * Get the bug type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the BugPattern.
	 */
	public @NonNull BugPattern getBugPattern() {
		BugPattern result =  I18N.instance().lookupBugPattern(getType());
		if (result != null) {
			return result;
		}
		AnalysisContext.logError("Unable to find description of bug pattern " + getType());
		result = I18N.instance().lookupBugPattern("UNKNOWN");
		if (result != null) {
			return result;
		}
		return BugPattern.REALLY_UNKNOWN;
	}

	/**
	 * Get the bug priority.
	 */
	public int getPriority() {
		return priority;
	}


	public int getBugRank() {
		return BugRanker.findRank(this);
	}
	/**
	 * Get a string describing the bug priority and type.
	 * e.g. "High Priority Correctness"
	 * @return a string describing the bug priority and type
	 */
	public String getPriorityTypeString()
	{
		String priorityString = getPriorityString();
		BugPattern bugPattern = this.getBugPattern();
		//then get the category and put everything together
		String categoryString;
		if (bugPattern == null) {
			categoryString = "Unknown category for " + getType();
		} else {
			categoryString = I18N.instance().getBugCategoryDescription(bugPattern.getCategory());
		}
		return priorityString + " Priority " + categoryString;
		//TODO: internationalize the word "Priority"
	}

	public String getCategoryAbbrev() {
		BugPattern bugPattern = getBugPattern();
		if (bugPattern == null) {
			return "?";
		}
		return bugPattern.getCategoryAbbrev();
	}

	public String getPriorityString() {
		return I18N.instance().getPriorityString(this);
	}

	public String getPriorityAbbreviation() {
		return getPriorityString().substring(0,1);
	}
	/**
	 * Set the bug priority.
	 */
	public void setPriority(int p) {
		priority = boundedPriority(p);
	}

	private int boundedPriority(int p) {
		return Math.max(Detector.HIGH_PRIORITY, Math.min(Detector.IGNORE_PRIORITY, p));
	}
	public void raisePriority() {
		priority = boundedPriority(priority-1);

	}
	public void lowerPriority() {
		priority = boundedPriority(priority+1);
	}

	public void lowerPriorityALot() {
		priority = boundedPriority(priority+2);
	}

	/**
	 * Is this bug instance the result of an experimental detector?
	 */
	public boolean isExperimental() {
		BugPattern pattern = getBugPattern();
		return (pattern != null) && pattern.isExperimental();
	}

	/**
	 * Get the primary class annotation, which indicates where the bug occurs.
	 */
	public IClassAnnotation getPrimaryClass() {
		IClassAnnotation result =   findPrimaryAnnotationOfType(IClassAnnotation.class);
		if (result == null) {
			System.out.println("huh");
			result =   findPrimaryAnnotationOfType(IClassAnnotation.class);
		}
		return result;
	}

	/**
	 * Get the primary method annotation, which indicates where the bug occurs.
	 */
	public IMethodAnnotation getPrimaryMethod() {
		return  findPrimaryAnnotationOfType(IMethodAnnotation.class);
	}
	/**
	 * Get the primary method annotation, which indicates where the bug occurs.
	 */
	public IFieldAnnotation getPrimaryField() {
		return findPrimaryAnnotationOfType(IFieldAnnotation.class);
	}

	/**
	 * Find the first BugAnnotation in the list of annotations
	 * that is the same type or a subtype as the given Class parameter.
	 * 
	 * @param cls the Class parameter
	 * @return the first matching BugAnnotation of the given type,
	 *         or null if there is no such BugAnnotation
	 */
	private <T extends BugAnnotation> T findPrimaryAnnotationOfType(Class<T>  cls) {
		for (Iterator<BugAnnotation> i = annotationIterator(); i.hasNext();) {
			BugAnnotation annotation = i.next();
			if (annotation.getDescription().endsWith("DEFAULT") && cls.isAssignableFrom(annotation.getClass())) {
				return cls.cast(annotation);
			}
		}
		return null;
	}

	public ILocalVariableAnnotation getPrimaryLocalVariableAnnotation() {
		for (BugAnnotation annotation : annotationList) {
			if (annotation instanceof ILocalVariableAnnotation) {
				return (ILocalVariableAnnotation) annotation;
			}
		}
		return null;
	}
	/**
	 * Get the primary source line annotation.
	 * There is guaranteed to be one (unless some Detector constructed
	 * an invalid BugInstance).
	 *
	 * @return the source line annotation
	 */
	public ISourceLineAnnotation getPrimarySourceLineAnnotation() {
		// Highest priority: return the first top level source line annotation
		for (BugAnnotation annotation : annotationList) {
			if (annotation instanceof ISourceLineAnnotation
					&& annotation.getDescription().equals(ISourceLineAnnotation.DEFAULT_ROLE)) {
				return (ISourceLineAnnotation) annotation;
			}
		}
		for (BugAnnotation annotation : annotationList) {
			if (annotation instanceof ISourceLineAnnotation) {
				return (ISourceLineAnnotation) annotation;
			}
		}

		// Next: Try primary method, primary field, primary class
		ISourceLineAnnotation srcLine;
		if ((srcLine = inspectPackageMemberSourceLines(getPrimaryMethod())) != null) {
			return srcLine;
		}
		if ((srcLine = inspectPackageMemberSourceLines(getPrimaryField())) != null) {
			return srcLine;
		}
		if ((srcLine = inspectPackageMemberSourceLines(getPrimaryClass())) != null) {
			return srcLine;
		}

		// Last resort: throw exception
		throw new IllegalStateException("BugInstance must contain at least one class, method, or field annotation");
	}

	private static boolean CHECK = false;
	private static int keyCount = 0;
	static int keyDifferentCount = 0;
	static {
		if (CHECK && SystemProperties.getBoolean("findbugs.shutdownLogging")) {
			Util.runLogAtShutdown(new Runnable() {
				@SuppressWarnings("boxing")
				public void run() {
					System.out.printf("%d/%d instance keys changed\n", keyDifferentCount, keyCount);
				}
			});
		}
	}

	public String getInstanceKey() {
		String newValue = getInstanceKeyNew();
		if (!CHECK) {
			return newValue;
		}
		String oldValue = getInstanceKeyOld();

		keyCount++;
		if (!oldValue.equals(newValue)) {
			keyDifferentCount++;
			System.out.println(keyDifferentCount);
			System.out.println(getMessageWithoutPrefix());
			System.out.println(oldValue);
			System.out.println(newValue);
		}
		return newValue;
	}
	private String getInstanceKeyOld() {
		StringBuilder buf = new StringBuilder(type);
		for (BugAnnotation annotation : annotationList) {
			if (annotation instanceof ISourceLineAnnotation || annotation instanceof IMethodAnnotation && !annotation.isSignificant()) {
				// do nothing
			} else {
				buf.append(":");
				buf.append(annotation.format("hash", null));
			}
		}
		return buf.toString();
	}
	private String getInstanceKeyNew() {
		StringBuilder buf = new StringBuilder(type);
		for (BugAnnotation annotation : annotationList) {
			if (annotation.isSignificant() || annotation instanceof IntAnnotation || annotation instanceof ILocalVariableAnnotation) {
				buf.append(":");
				buf.append(annotation.format("hash", null));
			}
		}

		return buf.toString();
	}
	/**
	 * If given PackageMemberAnnotation is non-null, return its
	 * SourceLineAnnotation.
	 * 
	 * @param packageMember
	 *            a PackageMemberAnnotation
	 * @return the PackageMemberAnnotation's SourceLineAnnotation, or null if
	 *         there is no SourceLineAnnotation
	 */
	private ISourceLineAnnotation inspectPackageMemberSourceLines(IPackageMemberAnnotation packageMember) {
		return (packageMember != null) ? packageMember.getSourceLines() : null;
	}

	/**
	 * Get an Iterator over all bug annotations.
	 */
	public Iterator<BugAnnotation> annotationIterator() {
		return annotationList.iterator();
	}

	/**
	 * Get an Iterator over all bug annotations.
	 */
	public List<? extends BugAnnotation> getAnnotations() {
		return annotationList;
	}

	/**
	 * Get the abbreviation of this bug instance's BugPattern.
	 * This is the same abbreviation used by the BugCode which
	 * the BugPattern is a particular species of.
	 */
	public String getAbbrev() {
		BugPattern pattern = getBugPattern();
		return pattern != null ? pattern.getAbbrev() : "<unknown bug pattern>";
	}

	/** set the user designation object. This will clobber any
	 *  existing annotationText (or any other BugDesignation field). */
	@Deprecated
	public void setUserDesignation(BugDesignation bd) {
		userDesignation = bd;
	}

	/** return the user designation object, which may be null.
	 * 
	 *  A previous calls to getSafeUserDesignation(), setAnnotationText(),
	 *  or setUserDesignation() will ensure it will be non-null
	 *  [barring an intervening setUserDesignation(null)].
	 *  @see #getNonnullUserDesignation() */
	@Deprecated
	@Nullable public BugDesignation getUserDesignation() {
		return userDesignation;
	}

	/** return the user designation object, creating one if
	 *  necessary. So calling
	 *  <code>getSafeUserDesignation().setDesignation("HARMLESS")</code>
	 *  will always work without the possibility of a NullPointerException.
	 *  @see #getUserDesignation() */
	@Deprecated
	@NonNull public BugDesignation getNonnullUserDesignation() {
		if (userDesignation == null) {
			userDesignation = new BugDesignation();
		}
		return userDesignation;
	}


	/** Get the user designation key.
	 *  E.g., "MOSTLY_HARMLESS", "CRITICAL", "NOT_A_BUG", etc.
	 *
	 *  If the user designation object is null,returns UNCLASSIFIED.
	 *
	 *  To set the user designation key, call
	 *  <code>getSafeUserDesignation().setDesignation("HARMLESS")</code>.
	 * 
	 *  @see I18N#getUserDesignation(String key)
	 *  @return the user designation key
	 */
	@NonNull public String getUserDesignationKey() {
		if (userDesignation == null) {
			return BugDesignation.UNCLASSIFIED;
		}
		return userDesignation.getDesignationKey();
	}

	public @CheckForNull String getUserName() {
		if (userDesignation == null) {
			return null;
		}
		return userDesignation.getUser();
	}
	public  long getUserTimestamp() {
		if (userDesignation == null) {
			return Long.MAX_VALUE;
		}
		return userDesignation.getTimestamp();
	}

	/**
	 * @param key
	 * @param bugCollection TODO
	 */
	public void setUserDesignationKey(String key, @CheckForNull BugCollection bugCollection) {
		BugDesignation userDesignation1 = getNonnullUserDesignation();
		if (userDesignation1.getDesignationKey().equals(key)) {
			return;
		}
		userDesignation1.setDesignationKey(key);
		Cloud plugin = bugCollection != null? bugCollection.getCloud() : null;
		if (plugin != null) {
			plugin.storeUserAnnotation(this);
		}
	}

	/**s
	 * @param index
	 * @param bugCollection TODO
	 */
	public void setUserDesignationKeyIndex(int index, @CheckForNull BugCollection bugCollection) {
		setUserDesignationKey(
				UserDesignation.getUserDesignation(index).name(), bugCollection);
	}

	/**
	 * Set the user annotation text.
	 *
	 * @param annotationText the user annotation text
	 * @param bugCollection TODO
	 */
	public void setAnnotationText(String annotationText, @CheckForNull BugCollection bugCollection) {
		final BugDesignation u = getNonnullUserDesignation();
		String existingText = u.getAnnotationText();
		if (existingText != null && existingText.equals(annotationText)) {
			return;
		}
		u.setAnnotationText(annotationText);
		Cloud plugin = bugCollection != null? bugCollection.getCloud() : null;
		if (plugin != null) {
			plugin.storeUserAnnotation(this);
		}
	}

	/**
	 * Get the user annotation text.
	 *
	 * @return the user annotation text
	 */
	@NonNull public String getAnnotationText() {
		if (userDesignation == null) {
			return "";
		}
		String s = userDesignation.getAnnotationText();
		if (s == null) {
			return "";
		}
		return s;
	}

	public void setUser(String user) {
		getNonnullUserDesignation().setUser(user);
	}
	public void setUserAnnotationTimestamp(long timestamp) {
		getNonnullUserDesignation().setTimestamp(timestamp);
	}
	/**
	 * Determine whether or not the annotation text contains
	 * the given word.
	 *
	 * @param word the word
	 * @return true if the annotation text contains the word, false otherwise
	 */
	public boolean annotationTextContainsWord(String word) {
		return getTextAnnotationWords().contains(word);
	}

	/**
	 * Get set of words in the text annotation.
	 */
	public Set<String> getTextAnnotationWords() {
		HashSet<String> result = new HashSet<String>();

		StringTokenizer tok = new StringTokenizer(getAnnotationText(), " \t\r\n\f.,:;-");
		while (tok.hasMoreTokens()) {
			result.add(tok.nextToken());
		}
		return result;
	}





	/* ----------------------------------------------------------------------
	 * Property accessors
	 * ---------------------------------------------------------------------- */

	private class BugPropertyIterator implements Iterator<BugProperty> {
		private BugProperty prev, cur;
		private boolean removed;

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return findNext() != null;
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public BugProperty next() {
			BugProperty next = findNext();
			if (next == null) {
				throw new NoSuchElementException();
			}
			prev = cur;
			cur = next;
			removed = false;
			return cur;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			if (cur == null || removed) {
				throw new IllegalStateException();
			}
			if (prev == null) {
				propertyListHead = cur.getNext();
			} else {
				prev.setNext(cur.getNext());
			}
			if (cur == propertyListTail) {
				propertyListTail = prev;
			}
			removed = true;
		}

		private BugProperty findNext() {
			return cur == null ? propertyListHead : cur.getNext();
		}

	}

	/**
	 * Get value of given property.
	 * 
	 * @param name name of the property to get
	 * @return the value of the named property, or null if
	 *         the property has not been set
	 */
	public String getProperty(String name) {
		BugProperty prop = lookupProperty(name);
		return prop != null ? prop.getValue() : null;
	}

	/**
	 * Get value of given property, returning given default
	 * value if the property has not been set.
	 * 
	 * @param name         name of the property to get
	 * @param defaultValue default value to return if propery is not set
	 * @return the value of the named property, or the default
	 *         value if the property has not been set
	 */
	public String getProperty(String name, String defaultValue) {
		String value = getProperty(name);
		return value != null ? value : defaultValue;
	}

	/**
	 * Get an Iterator over the properties defined in this BugInstance.
	 * 
	 * @return Iterator over properties
	 */
	public Iterator<BugProperty> propertyIterator() {
		return new BugPropertyIterator();
	}

	/**
	 * Set value of given property.
	 * 
	 * @param name  name of the property to set
	 * @param value the value of the property
	 * @return this object, so calls can be chained
	 */
	public BugInstance setProperty(String name, String value) {
		BugProperty prop = lookupProperty(name);
		if (prop != null) {
			prop.setValue(value);
		} else {
			prop = new BugProperty(name, value);
			addProperty(prop);
		}
		return this;
	}

	/**
	 * Look up a property by name.
	 * 
	 * @param name name of the property to look for
	 * @return the BugProperty with the given name,
	 *         or null if the property has not been set
	 */
	public BugProperty lookupProperty(String name) {
		BugProperty prop = propertyListHead;

		while (prop != null) {
			if (prop.getName().equals(name)) {
				break;
			}
			prop = prop.getNext();
		}

		return prop;
	}

	/**
	 * Delete property with given name.
	 * 
	 * @param name name of the property to delete
	 * @return true if a property with that name was deleted,
	 *         or false if there is no such property
	 */
	public boolean deleteProperty(String name) {
		BugProperty prev = null;
		BugProperty prop = propertyListHead;

		while (prop != null) {
			if (prop.getName().equals(name)) {
				break;
			}
			prev = prop;
			prop = prop.getNext();
		}

		if (prop != null) {
			if (prev != null) {
				// Deleted node in interior or at tail of list
				prev.setNext(prop.getNext());
			} else {
				// Deleted node at head of list
				propertyListHead = prop.getNext();
			}

			if (prop.getNext() == null) {
				// Deleted node at end of list
				propertyListTail = prev;
			}

			return true;
		}
		// No such property
		return false;
	}

	private void addProperty(BugProperty prop) {
		if (propertyListTail != null) {
			propertyListTail.setNext(prop);
			propertyListTail = prop;
		} else {
			propertyListHead = propertyListTail = prop;
		}
		prop.setNext(null);
	}

	/**
	 * Add an integer annotation.
	 *
	 * @param value the integer value
	 * @return this object
	 */
	public BugInstance addInt(int value) {
		add(new IntAnnotation(value));
		return this;
	}

	/**
	 * Add a String annotation.
	 *
	 * @param value the String value
	 * @return this object
	 */
	public BugInstance addString(String value) {
		add(new StringAnnotation(value));
		return this;
	}

	/**
	 * Format a string describing this bug instance.
	 *
	 * @return the description
	 */
	public String getMessageWithoutPrefix() {
		BugPattern bugPattern = getBugPattern();
		String pattern, shortPattern;

		pattern = getLongDescription();
		shortPattern = bugPattern.getShortDescription();
		try {
			FindBugsMessageFormat format = new FindBugsMessageFormat(pattern);
			return format.format(annotationList.toArray(new BugAnnotation[annotationList.size()]), getPrimaryClass());
		} catch (RuntimeException e) {
			AnalysisContext.logError("Error generating bug msg ", e);
			return shortPattern + " [Error generating customized description]";
		}
	}
	String getLongDescription() {
		return getBugPattern().getLongDescription().replaceAll("BUG_PATTERN", type);
	}
	public String getAbridgedMessage() {
		BugPattern bugPattern = getBugPattern();
		String pattern, shortPattern;
		if (bugPattern == null) {
			shortPattern = pattern = "Error2: missing bug pattern for key " + type;
		} else {
			pattern = getLongDescription().replaceAll(" in \\{1\\}", "");
			shortPattern = bugPattern.getShortDescription();
		}
		try {
			FindBugsMessageFormat format = new FindBugsMessageFormat(pattern);
			return format.format(annotationList.toArray(new BugAnnotation[annotationList.size()]), getPrimaryClass());
		} catch (RuntimeException e) {
			AnalysisContext.logError("Error generating bug msg ", e);
			return shortPattern + " [Error3 generating customized description]";
		}
	}
	/**
	 * Format a string describing this bug instance.
	 *
	 * @return the description
	 */
	public String getMessage() {
		BugPattern bugPattern = getBugPattern();
		String pattern = bugPattern.getAbbrev() + ": " + getLongDescription();
		FindBugsMessageFormat format = new FindBugsMessageFormat(pattern);
		try {
			return format.format(annotationList.toArray(new BugAnnotation[annotationList.size()]), getPrimaryClass());
		} catch (RuntimeException e) {
			AnalysisContext.logError("Error generating bug msg ", e);
			return bugPattern.getShortDescription() + " [Error generating customized description]";
		}
	}

	/**
	 * Add a description to the most recently added bug annotation.
	 *
	 * @param description the description to add
	 * @return this object
	 */
	public BugInstance describe(String description) {
		annotationList.get(annotationList.size() - 1).setDescription(description);
		return this;
	}

	/**
	 * Convert to String.
	 * This method returns the "short" message describing the bug,
	 * as opposed to the longer format returned by getMessage().
	 * The short format is appropriate for the tree view in a GUI,
	 * where the annotations are listed separately as part of the overall
	 * bug instance.
	 */
	@Override
	public String toString() {
		return I18N.instance().getShortMessage(type);
	}

	/* ----------------------------------------------------------------------
	 * XML Conversion support
	 * ---------------------------------------------------------------------- */

	public void writeXML(XMLOutput xmlOutput) throws IOException {
		writeXML(xmlOutput, false, false);
	}

	public void writeXML(XMLOutput xmlOutput, boolean addMessages, boolean isPrimary) throws IOException {
		XMLAttributeList attributeList = new XMLAttributeList()
		.addAttribute("type", type)
		.addAttribute("priority", String.valueOf(priority));

		BugPattern pattern = getBugPattern();
		if (pattern != null) {
			// The bug abbreviation and pattern category are
			// emitted into the XML for informational purposes only.
			// (The information is redundant, but might be useful
			// for processing tools that want to make sense of
			// bug instances without looking at the plugin descriptor.)
			attributeList.addAttribute("abbrev", pattern.getAbbrev());
			attributeList.addAttribute("category", pattern.getCategory());
		}


		if (addMessages) {
			//		Add a uid attribute, if we have a unique id.

			attributeList.addAttribute("instanceHash", getInstanceHash());
			attributeList.addAttribute("instanceOccurrenceNum", Integer.toString(getInstanceOccurrenceNum()));
			attributeList.addAttribute("instanceOccurrenceMax", Integer.toString(getInstanceOccurrenceMax()));

		}
		if (firstVersion > 0) {
			attributeList.addAttribute("first", Long.toString(firstVersion));
		}
		if (lastVersion >= 0) {
			attributeList.addAttribute("last", Long.toString(lastVersion));
		}
		if (introducedByChangeOfExistingClass) {
			attributeList.addAttribute("introducedByChange", "true");
		}
		if (removedByChangeOfPersistingClass) {
			attributeList.addAttribute("removedByChange", "true");
		}

		xmlOutput.openTag(ELEMENT_NAME, attributeList);

		if (userDesignation != null) {
			userDesignation.writeXML(xmlOutput);
		}

		if (addMessages) {
			BugPattern bugPattern = getBugPattern();

			xmlOutput.openTag("ShortMessage");
			xmlOutput.writeText(bugPattern != null ? bugPattern.getShortDescription() : this.toString());
			xmlOutput.closeTag("ShortMessage");

			xmlOutput.openTag("LongMessage");
			if (FindBugsDisplayFeatures.isAbridgedMessages()) {
				xmlOutput.writeText(this.getAbridgedMessage());
			} else {
				xmlOutput.writeText(this.getMessageWithoutPrefix());
			}
			xmlOutput.closeTag("LongMessage");
		}

		Map<BugAnnotation,Void> primaryAnnotations;

		if (addMessages) {
			primaryAnnotations = new IdentityHashMap<BugAnnotation,Void>();
			primaryAnnotations.put(getPrimarySourceLineAnnotation(), null);
			primaryAnnotations.put(getPrimaryClass(), null);
			primaryAnnotations.put(getPrimaryField(), null);
			primaryAnnotations.put(getPrimaryMethod(), null);
		} else {
			primaryAnnotations = Collections.<BugAnnotation,Void>emptyMap();
		}

		boolean foundSourceAnnotation = false;
		for (BugAnnotation annotation : annotationList) {
			if (annotation instanceof ISourceLineAnnotation) {
				foundSourceAnnotation = true;
			}
			annotation.writeXML(xmlOutput, addMessages,  primaryAnnotations.containsKey(annotation));
		}
		if (!foundSourceAnnotation && addMessages) {
			ISourceLineAnnotation synth = getPrimarySourceLineAnnotation();
			if (synth != null) {
				synth.setSynthetic(true);
				synth.writeXML(xmlOutput, addMessages, false);
			}
		}

		if (propertyListHead != null) {
			BugProperty prop = propertyListHead;
			while (prop != null) {
				prop.writeXML(xmlOutput);
				prop = prop.getNext();
			}
		}

		xmlOutput.closeTag(ELEMENT_NAME);
	}

	/**
	 * Add an annotation. If this is the first annotation added,
	 * it becomes the primary annotation.
	 *
	 * @param annotation the annotation, must be not null
	 * @return this object
	 */
	public BugInstance add(@Nonnull BugAnnotation annotation) {
		if (annotation == null) {
			return this;
			//			throw new IllegalArgumentException("Missing BugAnnotation!");
		}

		// Add to list
		annotationList.add(annotation);

		// This object is being modified, so the cached hashcode
		// must be invalidated
		cachedHashCode = INVALID_HASH_CODE;
		return this;
	}

	public BugInstance addOptionalUniqueAnnotations(BugAnnotation... annotations) {
		HashSet<BugAnnotation> added = new HashSet<BugAnnotation>();
		for(BugAnnotation a : annotations){
			if (a != null && added.add(a)) {
				add(a);
			}
		}
		return this;
	}

	@Override
	public int hashCode() {
		if (cachedHashCode == INVALID_HASH_CODE) {
			int hashcode = type.hashCode() + priority;
			Iterator<BugAnnotation> i = annotationIterator();
			while (i.hasNext()) {
				hashcode += i.next().hashCode();
			}
			if (hashcode == INVALID_HASH_CODE) {
				hashcode = INVALID_HASH_CODE+1;
			}
			cachedHashCode = hashcode;
		}

		return cachedHashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BugInstance)) {
			return false;
		}
		BugInstance other = (BugInstance) o;
		if (!type.equals(other.type) || priority != other.priority) {
			return false;
		}
		if (annotationList.size() != other.annotationList.size()) {
			return false;
		}
		int numAnnotations = annotationList.size();
		for (int i = 0; i < numAnnotations; ++i) {
			BugAnnotation lhs = annotationList.get(i);
			BugAnnotation rhs = other.annotationList.get(i);
			if (!lhs.equals(rhs)) {
				return false;
			}
		}

		return true;
	}

	public int compareTo(BugInstance other) {
		int cmp;
		cmp = type.compareTo(other.getType());
		if (cmp != 0) {
			return cmp;
		}
		cmp = priority - other.getPriority();
		if (cmp != 0) {
			return cmp;
		}

		// Compare BugAnnotations lexicographically
		List<? extends BugAnnotation> otherAnnotations = other.getAnnotations();
		int pfxLen = Math.min(annotationList.size(), otherAnnotations.size());
		for (int i = 0; i < pfxLen; ++i) {
			BugAnnotation lhs = annotationList.get(i);
			BugAnnotation rhs = otherAnnotations.get(i);
			cmp = lhs.compareTo(rhs);
			if (cmp != 0) {
				return cmp;
			}
		}

		// All elements in prefix were the same,
		// so use number of elements to decide
		return annotationList.size() - otherAnnotations.size();
	}

	/**
	 * @param firstVersion The firstVersion to set.
	 */
	public void setFirstVersion(long firstVersion) {
		this.firstVersion = firstVersion;
		if (lastVersion >= 0 && firstVersion > lastVersion) {
			throw new IllegalArgumentException(
					firstVersion + ".." + lastVersion);
		}
	}

	/**
	 * @return Returns the firstVersion.
	 */
	public long getFirstVersion() {
		return firstVersion;
	}

	/**
	 * @param lastVersion The lastVersion to set.
	 */
	public void setLastVersion(long lastVersion) {
		if (lastVersion >= 0 && firstVersion > lastVersion) {
			throw new IllegalArgumentException(
					firstVersion + ".." + lastVersion);
		}
		this.lastVersion = lastVersion;
	}

	/**
	 * @return Returns the lastVersion.
	 */
	public long getLastVersion() {
		return lastVersion;
	}

	public boolean isDead() {
		return lastVersion != -1;
	}
	/**
	 * @param introducedByChangeOfExistingClass The introducedByChangeOfExistingClass to set.
	 */
	public void setIntroducedByChangeOfExistingClass(boolean introducedByChangeOfExistingClass) {
		this.introducedByChangeOfExistingClass = introducedByChangeOfExistingClass;
	}

	/**
	 * @return Returns the introducedByChangeOfExistingClass.
	 */
	public boolean isIntroducedByChangeOfExistingClass() {
		return introducedByChangeOfExistingClass;
	}

	/**
	 * @param removedByChangeOfPersistingClass The removedByChangeOfPersistingClass to set.
	 */
	public void setRemovedByChangeOfPersistingClass(boolean removedByChangeOfPersistingClass) {
		this.removedByChangeOfPersistingClass = removedByChangeOfPersistingClass;
	}

	/**
	 * @return Returns the removedByChangeOfPersistingClass.
	 */
	public boolean isRemovedByChangeOfPersistingClass() {
		return removedByChangeOfPersistingClass;
	}

	/**
	 * @param instanceHash The instanceHash to set.
	 */
	public void setInstanceHash(String instanceHash) {
		this.instanceHash = instanceHash;
	}
	/**
	 * @param oldInstanceHash The oldInstanceHash to set.
	 */
	public void setOldInstanceHash(String oldInstanceHash) {
		this.oldInstanceHash = oldInstanceHash;
	}
	private static final boolean DONT_HASH =  SystemProperties.getBoolean("findbugs.dontHash");
	/**
	 * @return Returns the instanceHash.
	 */
	public String getInstanceHash() {
		if (instanceHash != null) {
			return instanceHash;
		}
		MessageDigest digest = null;
		try { digest = MessageDigest.getInstance("MD5");
		} catch (Exception e2) {
			// OK, we won't digest
		}
		instanceHash = getInstanceKey();
		if (digest != null && !DONT_HASH) {
			byte [] data = digest.digest(instanceHash.getBytes());
			String tmp = new BigInteger(1,data).toString(16);
			instanceHash = tmp;
		}
		return instanceHash;
	}

	public boolean isInstanceHashConsistent() {
		return oldInstanceHash == null || instanceHash.equals(oldInstanceHash);
	}
	/**
	 * @param instanceOccurrenceNum The instanceOccurrenceNum to set.
	 */
	public void setInstanceOccurrenceNum(int instanceOccurrenceNum) {
		this.instanceOccurrenceNum = instanceOccurrenceNum;
	}

	/**
	 * @return Returns the instanceOccurrenceNum.
	 */
	public int getInstanceOccurrenceNum() {
		return instanceOccurrenceNum;
	}

	/**
	 * @param instanceOccurrenceMax The instanceOccurrenceMax to set.
	 */
	public void setInstanceOccurrenceMax(int instanceOccurrenceMax) {
		this.instanceOccurrenceMax = instanceOccurrenceMax;
	}

	/**
	 * @return Returns the instanceOccurrenceMax.
	 */
	public int getInstanceOccurrenceMax() {
		return instanceOccurrenceMax;
	}

	public DetectorFactory getDetectorFactory() {
		return detectorFactory;
	}
}

// vim:ts=4
