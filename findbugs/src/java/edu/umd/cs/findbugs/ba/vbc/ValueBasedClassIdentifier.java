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

package edu.umd.cs.findbugs.ba.vbc;

import java.util.HashSet;

import edu.umd.cs.findbugs.annotations.ValueBased;
import edu.umd.cs.findbugs.ba.XClass;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.DescriptorFactory;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.analysis.AnnotationValue;
import edu.umd.cs.findbugs.internalAnnotations.SlashedClassName;
import edu.umd.cs.findbugs.internalAnnotations.StaticConstant;

public class ValueBasedClassIdentifier {

    /**
     * <a href="http://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">Value-based classes</a> from the JDK
     * which are identified as such by their documentation.
     */
    @StaticConstant
    private static final HashSet<String> JDK_VALUE_BASED_CLASSES = new HashSet<String>();

    /**
     * Non-JDK classes which are considered to be value-based.
     */
    @StaticConstant
    private static final HashSet<String> OTHER_VALUE_BASED_CLASSES = new HashSet<String>();

    static {
        /* JDK */
        JDK_VALUE_BASED_CLASSES.add("java/util/Optional");
        JDK_VALUE_BASED_CLASSES.add("java/util/OptionalDouble");
        JDK_VALUE_BASED_CLASSES.add("java/util/OptionalLong");
        JDK_VALUE_BASED_CLASSES.add("java/util/OptionalInt");

        JDK_VALUE_BASED_CLASSES.add("java/time/Duration");
        JDK_VALUE_BASED_CLASSES.add("java/time/Instant");
        JDK_VALUE_BASED_CLASSES.add("java/time/LocalDate");
        JDK_VALUE_BASED_CLASSES.add("java/time/LocalDateTime");
        JDK_VALUE_BASED_CLASSES.add("java/time/LocalTime");
        JDK_VALUE_BASED_CLASSES.add("java/time/MonthDay");
        JDK_VALUE_BASED_CLASSES.add("java/time/OffsetDateTime");
        JDK_VALUE_BASED_CLASSES.add("java/time/OffsetTime");
        JDK_VALUE_BASED_CLASSES.add("java/time/Period");
        JDK_VALUE_BASED_CLASSES.add("java/time/Year");
        JDK_VALUE_BASED_CLASSES.add("java/time/YearMonth");
        JDK_VALUE_BASED_CLASSES.add("java/time/ZonedDateTime");
        JDK_VALUE_BASED_CLASSES.add("java/time/ZoneId");
        JDK_VALUE_BASED_CLASSES.add("java/time/ZoneOffset");

        JDK_VALUE_BASED_CLASSES.add("java/time/chrono/HijrahDate");
        JDK_VALUE_BASED_CLASSES.add("java/time/chrono/JapaneseDate");
        JDK_VALUE_BASED_CLASSES.add("java/time/chrono/MinguaDate");
        JDK_VALUE_BASED_CLASSES.add("java/time/chrono/ThaiBuddhistDate");

        /* OTHER */
        OTHER_VALUE_BASED_CLASSES.add("com/google/common/base/Optional");
    }

    @StaticConstant
    private static final ClassDescriptor VALUE_BASED_ANNOTATION_DESCRIPTOR =
    DescriptorFactory.createClassDescriptor(ValueBased.class);

    /**
     * Indicates whether the specified class is <i>value-based</i>.
     * <p>
     * A class is considered value-based for one of three reasons:
     * <ul>
     * <li>the class is part of the JDK and its official documentation identifies it as value-based (e.g.
     * {@link java.util.Optional Optional})
     * <li>the class is part of some other widely used library and can be considered as value-based (e.g. <a
     * href="http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Optional.html">Guava's Optional</a>)
     * <li>it is annotated with the FindBugs annotation {@link ValueBased @ValueBased}
     * </ul>
     *
     * @param className
     *            the fully qualified class name in "slashed form" without descriptor and semicolon, e.g. "java/util/Optional"
     * @return
     */
    public static boolean isValueBasedClass(@SlashedClassName String className) {
        return isJdkValueBasedClass(className) || isOtherValueBasedClass(className) || isAnnotatedAsValueBasedClass(className);
    }

    private static boolean isJdkValueBasedClass(@SlashedClassName String className) {
        return JDK_VALUE_BASED_CLASSES.contains(className);
    }

    private static boolean isOtherValueBasedClass(@SlashedClassName String className) {
        return OTHER_VALUE_BASED_CLASSES.contains(className);
    }

    private static boolean isAnnotatedAsValueBasedClass(@SlashedClassName String className) {
        try {
            ClassDescriptor classDescriptor = DescriptorFactory.createClassDescriptor(className);
            XClass xClass = Global.getAnalysisCache().getClassAnalysis(XClass.class, classDescriptor);
            AnnotationValue annotation = xClass.getAnnotation(VALUE_BASED_ANNOTATION_DESCRIPTOR);
            return annotation != null;
        } catch (CheckedAnalysisException ex) {
            // TODO (nipa@codefx.org) log the exception?
            return false;
        }
    }

}
