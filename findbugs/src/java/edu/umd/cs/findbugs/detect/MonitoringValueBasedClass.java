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

package edu.umd.cs.findbugs.detect;

import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.ba.vbc.ValueBasedClassIdentifier;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.util.ClassName;

public class MonitoringValueBasedClass extends OpcodeStackDetector {

    private final BugReporter bugReporter;

    public MonitoringValueBasedClass(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {
        // TODO (nipa@codefx.org) define these patterns and determine priority
        checkAndReport(isLockOnValueBasedClass(seen), "VBC_MO_LOCK", HIGH_PRIORITY);
        checkAndReport(isCallToWaitOnValueBasedClass(seen), "VBC_MO_WAIT", HIGH_PRIORITY);
    }

    private void checkAndReport(boolean isBug, String type, int priority) {
        if (!isBug) {
            return;
        }

        BugInstance bug = new BugInstance(this, type, priority).addClass(this).addSourceLine(this);
        bugReporter.reportBug(bug);
    }

    private boolean isLockOnValueBasedClass(int seen) {
        boolean isLock = seen == MONITORENTER;
        if (!isLock) {
            return false;
        }

        // check the argument to 'MONITORENTER', which is the last item on the stack
        Item lastStackItem = getStack().getStackItem(0);
        String dottedClassName = getDottedClassNameFromSignature(lastStackItem.getSignature());
        boolean onValueBasedClass = ValueBasedClassIdentifier.isValueBasedClass(dottedClassName);
        if (!onValueBasedClass) {
            return false;
        }

        return true;
    }

    private boolean isCallToWaitOnValueBasedClass(int seen) {
        boolean invokesMethod = seen == INVOKEVIRTUAL;
        if (!invokesMethod) {
            return false;
        }

        boolean invokesObjectDotWait = "java/lang/Object".equals(getClassConstantOperand())
                && "wait".equals(getNameConstantOperand());
        // we can ignore the signature because all wait-methods on object are considered
        if (!invokesObjectDotWait) {
            return false;
        }

        // TODO (nipa@codefx.org) identify the class of the instance on which the method is called
        boolean invokesOnValueBasedClass = true;
        if (!invokesOnValueBasedClass) {
            return false;
        }

        return true;
    }

    /**
     * Turns the specified signature into an 'L'-descriptor-free, dotted class name.
     * <p>
     * Is similar to {@link ClassName#toDottedClassName(String)} but not the same as it removes the descriptor if it is "L".
     *
     * @param signature
     *            the signature of an OpCodeStack {@link Item}
     * @return the "dotted" name of the class without the descriptor 'L'
     */
    private String getDottedClassNameFromSignature(String signature) {
        if (StringUtils.isBlank(signature)) {
            return "";
        }

        String slashedClassName = signature.startsWith("L") ? signature.substring(1, signature.length() - 1) : signature;
        return slashedClassName.replace('/', '.');
    }

}
