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

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.IFieldAnnotation;
import edu.umd.cs.findbugs.ILocalVariableAnnotation;
import edu.umd.cs.findbugs.IMethodAnnotation;
import edu.umd.cs.findbugs.ITypeAnnotation;
import edu.umd.cs.findbugs.TypeAnnotation;
import edu.umd.cs.findbugs.ann.AnnotationFactory;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.ClassContext;
import edu.umd.cs.findbugs.ba.Dataflow;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;
import edu.umd.cs.findbugs.ba.LiveLocalStoreAnalysis;
import edu.umd.cs.findbugs.ba.XFactory;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.signature.SignatureParser;
import edu.umd.cs.findbugs.util.ClassName;
import edu.umd.cs.findbugs.util.EditDistance;
import edu.umd.cs.findbugs.visitclass.PreorderVisitor;

/**
 * Helper class with some complicated methods used by different detectors. Should be not used
 * from FB core, as only detectors need the code below.
 * <p>
 * Implementation notes: most of the methods here are copied from different places around
 * *Annotations classes or from BugInstance.add*Annotation*.
 * 
 * @see AnnotationFactory
 * 
 * @author Andrei
 */
public class DetectorUtil {

	public static BugInstance addEqualsMethodUsed(BugInstance bug, @CheckForNull Collection<XMethod> equalsMethods) {
		if (equalsMethods == null) {
			return bug;
		}
		if (equalsMethods.size() < 5) {
			for (XMethod m : equalsMethods) {
				bug.add(AnnotationFactory.createMethod(m)).describe(IMethodAnnotation.METHOD_EQUALS_USED);
			}
		} else {
			bug.add(AnnotationFactory.createMethod(equalsMethods.iterator().next())).describe(IMethodAnnotation.METHOD_EQUALS_USED);
		}
		return bug;
	}

	public static void lowerPriorityIfDeprecated(BugInstance bug) {
		IMethodAnnotation m = bug.getPrimaryMethod();
		if (m != null && XFactory.createXMethod(m).isDeprecated()) {
			bug.lowerPriority();
		}
		IFieldAnnotation f = bug.getPrimaryField();
		if (f != null && XFactory.createXField(f).isDeprecated()) {
			bug.lowerPriority();
		}
	}

	public static void addFoundAndExpectedType(BugInstance bug, Type foundType, Type expectedType) {
		bug.add( new TypeAnnotation(foundType, ITypeAnnotation.FOUND_ROLE));
		bug.add( new TypeAnnotation(expectedType, ITypeAnnotation.EXPECTED_ROLE));
	}

	/**
	 * Add class and method annotations for given bug.
	 *
	 * @param methodGen  the method
	 * @param sourceFile source file the method is defined in
	 * @return given bug
	 */
	public static BugInstance addClassAndMethod(BugInstance bugInstance, MethodGen methodGen, String sourceFile) {
		IMethodAnnotation annotation = AnnotationFactory.createMethod(methodGen, sourceFile);
		bugInstance.add(AnnotationFactory.createClass(annotation.getClassName()));
		bugInstance.add(annotation);
		return bugInstance;
	}

	/**
	 * Add class and method annotations for given bug.
	 * 
	 * @param javaClass the class
	 * @param method    the method
	 * @return given bug
	 */
	public static BugInstance addClassAndMethod(BugInstance bugInstance, JavaClass javaClass, Method method) {
		bugInstance.add(AnnotationFactory.createClass(javaClass.getClassName()));
		bugInstance.add(AnnotationFactory.createMethod(javaClass, method));
		return bugInstance;
	}

	/**
	 * Add class and method annotations for given bug.
	 * 
	 * @param methodDescriptor   the method
	 * @return given bug
	 */
	public static BugInstance addClassAndMethod(BugInstance bugInstance, MethodDescriptor methodDescriptor) {
		bugInstance.add(AnnotationFactory.createClass(ClassName.toDottedClassName(methodDescriptor.getSlashedClassName())));
		bugInstance.add(AnnotationFactory.createMethod(
				methodDescriptor.getSlashedClassName(),
				methodDescriptor.getName(),
				methodDescriptor.getSignature(),
				methodDescriptor.isStatic()));
		return bugInstance;
	}

	/**
	 * Add a class annotation and a method annotation for the class and method
	 * which the given visitor is currently visiting.
	 *
	 * @param visitor the BetterVisitor
	 * @return given bug
	 */
	public static BugInstance addClassAndMethod(BugInstance bugInstance, PreorderVisitor visitor) {
		bugInstance.add(AnnotationFactory.createClass(visitor.getDottedClassName()));
		bugInstance.add(AnnotationFactory.createMethod(visitor));
		return bugInstance;
	}

	public static @CheckForNull
	ILocalVariableAnnotation findMatchingIgnoredParameter(ClassContext classContext, Method method, String name, String signature) {
		try {
			Dataflow<BitSet, LiveLocalStoreAnalysis> llsaDataflow = classContext.getLiveLocalStoreDataflow(method);
			CFG cfg;

			cfg = classContext.getCFG(method);
			ILocalVariableAnnotation match = null;
			int lowestCost = Integer.MAX_VALUE;
			BitSet liveStoreSetAtEntry = llsaDataflow.getAnalysis().getResultFact(cfg.getEntry());
			int localsThatAreParameters = PreorderVisitor.getNumberArguments(method.getSignature());
			int startIndex = 0;
			if (!method.isStatic()) {
				startIndex = 1;
			}
			SignatureParser parser = new SignatureParser(method.getSignature());
			Iterator<String> signatureIterator = parser.parameterSignatureIterator();
			for(int i = startIndex; i < localsThatAreParameters+startIndex; i++) {
				String sig = signatureIterator.next();
				if (!liveStoreSetAtEntry.get(i) && signature.equals(sig)) {
					// parameter isn't live and signatures match
					ILocalVariableAnnotation potentialMatch = AnnotationFactory.createVariable(method, i, 0, 0);
					potentialMatch.setDescription(ILocalVariableAnnotation.DID_YOU_MEAN_ROLE);
					if (!potentialMatch.isNamed()) {
						return potentialMatch;
					}
					int distance = EditDistance.editDistance(name, potentialMatch.getName());
					if (distance < lowestCost) {
						match = potentialMatch;
						match.setDescription(ILocalVariableAnnotation.DID_YOU_MEAN_ROLE);
						lowestCost = distance;
					} else if (distance == lowestCost) {
						// not unique best match
						match = null;
					}


				}
			}
			return match;
		} catch (DataflowAnalysisException e) {
			AnalysisContext.logError("", e);
		} catch (CFGBuilderException e) {
			AnalysisContext.logError("", e);
		}
		return null;
	}

	public static @CheckForNull
	ILocalVariableAnnotation findUniqueBestMatchingParameter(ClassContext classContext, Method method, String name, String signature) {
		ILocalVariableAnnotation match = null;
		int localsThatAreParameters = PreorderVisitor.getNumberArguments(method.getSignature());
		int startIndex = 0;
		if (!method.isStatic()) {
			startIndex = 1;
		}
		SignatureParser parser = new SignatureParser(method.getSignature());
		Iterator<String> signatureIterator = parser.parameterSignatureIterator();
		int lowestCost = Integer.MAX_VALUE;
		for(int i = startIndex; i < localsThatAreParameters+startIndex; i++) {
			String sig = signatureIterator.next();
			if (signature.equals(sig)) {
				ILocalVariableAnnotation potentialMatch = AnnotationFactory.createVariable(method, i, 0, 0);
				if (!potentialMatch.isNamed()) {
					continue;
				}
				int distance = EditDistance.editDistance(name, potentialMatch.getName());
				if (distance < lowestCost) {
					match = potentialMatch;
					match.setDescription(ILocalVariableAnnotation.DID_YOU_MEAN_ROLE);
					lowestCost = distance;
				} else if (distance == lowestCost) {
					// not unique best match
					match = null;
				}
				// signatures match
			}
		}
		if (lowestCost < 5) {
			return match;
		}
		return null;
	}
}
