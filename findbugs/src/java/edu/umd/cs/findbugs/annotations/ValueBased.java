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

package edu.umd.cs.findbugs.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is a <i>value-based class</i>.
 * <p>
 * To be value-based, a class must fulfill certain conditions (see below).
 * Some operations on such classes are strongly discouraged (see further below).
 * The conditions and operations marked with <b>[FB]</b> are checked by FindBugs.
 * <h2>Conditions</h2>
 * The <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html">official documentation</a>
 * specifies the limitations for value-based classes. This is an edited version of those limitations.
 * <p>
 * To be considered <i>value-based</i> the class must fulfill the following conditions:
 * <ul>
 *  <li>is final
 *  <li>does not have accessible constructors, but instead instantiates through factory methods
 *      which make no commitment as to the identity of returned instances
 *  <li>has implementations of {@link Object#equals(Object) equals}, {@link Object#hashCode() hashCode},
 *      and {@link Object#toString() toString} which are computed solely from the instance's state and
 *      not from its identity or the state of any other object or variable
 *  <li>makes no use of identity-sensitive operations such as reference equality ({@code ==}) between
 *      instances, identity hash code of instances, or synchronization on an instances's intrinsic lock
 *  <li>instances are final and immutable (though may contain references to mutable objects)
 *  <li>instances are considered equal solely based on {@link Object#equals(Object) equals},
 *      not based on reference equality ({@code ==});
 *  <li>instances are freely substitutable when equal, meaning that interchanging any two instances
 *      {@code x} and {@code y} that are equal according to {@link Object#equals(Object) equals} in any
 *      computation or method invocation should produce no visible change in behavior
 * </ul>
 *
 *<h2>Operations</h2>
 * From the official documentation: "A program may produce unpredictable results if it attempts
 * to distinguish two references to equal values of a value-based class [...]"
 * <p>
 * It lists the following operations as examples of such operations:
 * <ul>
 *  <li>using reference equality <b>[FB]</b>
 *  <li>synchronization
 *  <li>identity hashing
 *  <li>serialization
 *      (because some value-based JDK classes are serializable
 *      [e.g. {@link java.time.LocalTime LocalTime}] <b>[FB]</b> does no special checks; it uses the
 *      <a href="http://findbugs.sourceforge.net/bugDescriptions.html#SE_BAD_FIELD">SE_BAD_FIELD</a>
 *      pattern to detect serialization)
 *  <li>"any other identity-sensitive mechanism"
 * </ul>
 * It goes on: "Use of such identity-sensitive operations on instances of value-based classes
 * may have unpredictable effects and should be avoided."
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ValueBased {

}
