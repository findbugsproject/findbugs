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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.commons.lang.ArrayUtils;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.ba.vbc.ValueBasedClassIdentifier;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.internalAnnotations.SlashedClassName;
import edu.umd.cs.findbugs.util.ClassName;

public class MonitoringValueBasedClass extends OpcodeStackDetector {

    private final BugReporter bugReporter;

    public MonitoringValueBasedClass(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {
        checkAndReport(isLockOnValueBasedClass(seen), "VBC_MO_LOCK", NORMAL_PRIORITY);
        checkAndReport(isCallToWaitOnValueBasedClass(seen), "VBC_MO_WAIT", NORMAL_PRIORITY);
        checkAndReport(isCallToNotifyOnValueBasedClass(seen), "VBC_MO_NOTIFY", NORMAL_PRIORITY);
    }

    private void checkAndReport(boolean isBug, String type, int priority) {
        if (!isBug) {
            return;
        }

        BugInstance bug = new BugInstance(this, type, priority)
                .addClass(this)
                .addSourceLine(this)
                .addMethod(this);
        bugReporter.reportBug(bug);
    }

    private boolean isLockOnValueBasedClass(int seen) {
        boolean isLock = seen == MONITORENTER;
        if (!isLock) {
            return false;
        }

        // check the argument to 'MONITORENTER', which is the last item on the stack
        Item lastStackItem = getStack().getStackItem(0);
        String classSignature = lastStackItem.getSignature();
        @Nullable
        @SlashedClassName
        String className = ClassName.fromFieldSignature(classSignature);

        return ValueBasedClassIdentifier.isValueBasedClass(className);
    }

    private boolean isCallToWaitOnValueBasedClass(int seen) {
        return isCallToObjectMethodOnValueBasedClass(seen, "wait");
    }

    private boolean isCallToNotifyOnValueBasedClass(int seen) {
        return isCallToObjectMethodOnValueBasedClass(seen, "notify", "notifyAll");
    }

    private boolean isCallToObjectMethodOnValueBasedClass(int seen, String... methodNames) {
        boolean invokesMethod = seen == INVOKEVIRTUAL;
        if (!invokesMethod) {
            return false;
        }

        boolean invokesSpecifiedMethod = "java/lang/Object".equals(getClassConstantOperand())
                && ArrayUtils.contains(methodNames, getNameConstantOperand());
        // all overloads are treated identically
        if (!invokesSpecifiedMethod) {
            return false;
        }

        @Nullable
        @SlashedClassName
        String slashedClassName = getCallTargetName();
        return ValueBasedClassIdentifier.isValueBasedClass(slashedClassName);
    }

    private @SlashedClassName @CheckForNull String getCallTargetName() {
        String calledMethod = getMethodDescriptorOperand().getSignature();
        int nrOfArguments = getNumberArguments(calledMethod);
        Item callTargetStackItem = getStack().getStackItem(nrOfArguments);
        String callTargetSignature = callTargetStackItem.getSignature();
        return ClassName.fromFieldSignature(callTargetSignature);
    }

}
