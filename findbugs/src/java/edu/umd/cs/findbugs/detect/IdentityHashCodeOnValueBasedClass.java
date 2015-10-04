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

import java.util.Objects;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack.Item;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.vbc.ValueBasedClassIdentifier;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.internalAnnotations.SlashedClassName;
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
        // to improve performance, only scan the whole class if 'System.identityHashCode' is called
        // anywhere within it; look at the constant pool to find this out
        if (constantPoolReferencesMethod(classContext.getJavaClass().getConstantPool(), IDENTITY_HASH_CODE)) {
            super.visitClassContext(classContext);
        }
    }

    private static boolean constantPoolReferencesMethod(ConstantPool constantPool, MethodDescriptor methodDesc) {
        Constant[] constants = constantPool.getConstantPool();
        for (Constant constant : constants) {
            if (constantDescribesMethod(constant, constants, methodDesc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean constantDescribesMethod(Constant constant, Constant[] constants, MethodDescriptor methodDesc) {
        if (!(constant instanceof ConstantMethodref)) {
            return false;
        }
        ConstantMethodref methodRef = (ConstantMethodref) constant;

        ConstantClass clazz = (ConstantClass) constants[methodRef.getClassIndex()];
        @SlashedClassName
        String clazzName = ((ConstantUtf8) constants[clazz.getNameIndex()]).getBytes();
        if (!Objects.equals(methodDesc.getSlashedClassName(), clazzName)) {
            return false;
        }

        ConstantNameAndType nameAndSignature = (ConstantNameAndType) constants[methodRef.getNameAndTypeIndex()];
        String methodName = ((ConstantUtf8) constants[nameAndSignature.getNameIndex()]).getBytes();
        String methodSignature = ((ConstantUtf8) constants[nameAndSignature.getSignatureIndex()]).getBytes();

        return Objects.equals(methodDesc.getName(), methodName) && Objects.equals(methodDesc.getSignature(), methodSignature);
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
        String callArgumentClassName = ClassName.extractClassName(callArgumentSignature);

        return ValueBasedClassIdentifier.isValueBasedClass(callArgumentClassName);
    }
}
