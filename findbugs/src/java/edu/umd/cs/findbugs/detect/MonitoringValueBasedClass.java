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

public class MonitoringValueBasedClass extends OpcodeStackDetector {

    private final BugReporter bugReporter;

    public MonitoringValueBasedClass(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {
        checkLock(seen);
    }

    private void checkLock(int seen) {
        if (seen != MONITORENTER) {
            return;
        }

        Item lastStackItem = getStack().getStackItem(0);
        String dottedClassName = getDottedClassNameFromSignature(lastStackItem.getSignature());
        boolean reportBug = ValueBasedClassIdentifier.isValueBasedClass(dottedClassName);
        if (reportBug) {
            // TODO (nipa@codefx.org) define this pattern
            BugInstance bug = new BugInstance(this, "VBC_MO_LOCK", HIGH_PRIORITY).addClass(this).addSourceLine(this);
            bugReporter.reportBug(bug);
        }
    }

    // TODO is it necessary to implement this here?
    // can I use some helper method, instead?
    private String getDottedClassNameFromSignature(String signature) {
        if (StringUtils.isBlank(signature)) {
            return "";
        }

        String slashedClassName = signature.substring(1, signature.length() - 1);
        return slashedClassName.replace('/', '.');
    }

}
