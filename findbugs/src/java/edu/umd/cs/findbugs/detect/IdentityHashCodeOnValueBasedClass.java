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

import java.util.Collections;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.vbc.ValueBasedClassIdentifier;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.util.ClassName;

public class IdentityHashCodeOnValueBasedClass extends OpcodeStackDetector {

    private static final MethodDescriptor IDENTITY_HASH_CODE =
            new MethodDescriptor("java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I", true);

    private final BugReporter bugReporter;

    public IdentityHashCodeOnValueBasedClass(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        if (hasInterestingMethod(classContext.getJavaClass().getConstantPool(), Collections.singleton(IDENTITY_HASH_CODE))) {
            super.visitClassContext(classContext);
        }
    }

    @Override
    public void sawOpcode(int seen) {
        checkAndReport(isCallToSystemIdentityHashCodeWithValueBasedClass(seen), "VBC_IDENTITY_HASHCODE", NORMAL_PRIORITY);
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

    private boolean isCallToSystemIdentityHashCodeWithValueBasedClass(int seen) {
        boolean callToSystemIdentityHashCode = seen == INVOKESTATIC && getMethodDescriptorOperand().equals(IDENTITY_HASH_CODE);
        if (!callToSystemIdentityHashCode) {
            return false;
        }

        Item callArgumentStackItem = getStack().getStackItem(0);
        String callArgumentSignature = callArgumentStackItem.getSignature();
        String callArgumentClassName = ClassName.fromFieldSignature(callArgumentSignature);

        return ValueBasedClassIdentifier.isValueBasedClass(callArgumentClassName);
    }
}
