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

package edu.umd.cs.findbugs.detect;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ILocalVariableAnnotation;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.StatelessDetector;
import edu.umd.cs.findbugs.ann.AnnotationFactory;
import edu.umd.cs.findbugs.ba.XField;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;

public class FindFieldSelfAssignment extends OpcodeStackDetector implements StatelessDetector {
	private BugReporter bugReporter;

	int state;

	public FindFieldSelfAssignment(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	@Override
	public void visit(Code obj) {
		state = 0;
		super.visit(obj);
		initializedFields.clear();
	}

	int register;

	Set<String> initializedFields = new HashSet<String>();

	@Override
	public void sawOpcode(int seen) {

		if (seen == PUTFIELD) {
			OpcodeStack.Item top = stack.getStackItem(0);
			OpcodeStack.Item next = stack.getStackItem(1);
			
			
			XField f = top.getXField();
			int registerNumber = next.getRegisterNumber();
			if (f != null && f.equals(getXFieldOperand()) 
					&& registerNumber >= 0 && registerNumber == top.getFieldLoadedFromRegister()) {
				int priority = NORMAL_PRIORITY;

				ILocalVariableAnnotation possibleMatch = DetectorUtil.findMatchingIgnoredParameter(getClassContext(),
				        getMethod(), getNameConstantOperand(), getSigConstantOperand());
				if (possibleMatch == null)
					possibleMatch = DetectorUtil.findUniqueBestMatchingParameter(getClassContext(), getMethod(),
					        getNameConstantOperand(), getSigConstantOperand());
				if (possibleMatch != null)
					priority--;
				else {
					String signature = stack.getLVValue(registerNumber).getSignature();
					for(int i = 0; i < stack.getNumLocalValues(); i++) if (i != register) {
						if (stack.getLVValue(i).getSignature().equals(signature)) {
							priority--;
							break;
						}
					}
				}
				
				BugInstance bugInstance = DetectorUtil.addClassAndMethod(new BugInstance(this, "SA_FIELD_SELF_ASSIGNMENT", priority), this)
				    .add(AnnotationFactory.createReferencedField(this));
				if(possibleMatch != null){
					bugInstance.add(possibleMatch);
				}
				bugReporter.reportBug(bugInstance.add(AnnotationFactory.createSourceLine(this)));
				
			}
		}
		switch (state) {
		case 0:
			if (seen == DUP)
				state = 6;
			break;
			case 6:
			if (isRegisterStore()) {
				state = 7;
				register = getRegisterOperand();
			} else
				state = 0;
			break;
		case 7:
			if (isRegisterStore() && register == getRegisterOperand()) {
				bugReporter.reportBug(DetectorUtil.addClassAndMethod(new BugInstance(this, "SA_LOCAL_DOUBLE_ASSIGNMENT", NORMAL_PRIORITY), this).add(
				                AnnotationFactory.createVariable(getMethod(), register, getPC(), getPC() - 1))
				        .add(AnnotationFactory.createSourceLine(this)));
			}
			state = 0;
			break;
		}

	}

}
