/*
 * Bytecode Analysis Framework
 * Copyright (C) 2003-2007 University of Maryland
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
package edu.umd.cs.findbugs.classfile.engine.bcel;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.AssertionMethods;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CompactLocationNumbering;
import edu.umd.cs.findbugs.ba.DepthFirstSearch;
import edu.umd.cs.findbugs.ba.ReverseDepthFirstSearch;
import edu.umd.cs.findbugs.ba.npe.IsNullValueDataflow;
import edu.umd.cs.findbugs.ba.type.ExceptionSetFactory;
import edu.umd.cs.findbugs.ba.type.TypeDataflow;
import edu.umd.cs.findbugs.ba.vna.LoadedFieldSet;
import edu.umd.cs.findbugs.ba.vna.ValueNumberDataflow;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IMethodAnalysisEngine;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * Abstract factory class for creating analysis objects.
 * Handles caching of analysis results for a method.
 */
public abstract class AnalysisFactory <Analysis> implements IMethodAnalysisEngine {
	private String analysisName;
	private Class<Analysis> analysisClass;
	
//	private HashMap<Method, AnalysisResult<Analysis>> map =
//		new HashMap<Method, AnalysisResult<Analysis>>();

	/**
	 * Constructor.
	 * 
	 * @param analysisName name of the analysis factory: for diagnostics/debugging
	 */
	public AnalysisFactory(String analysisName, Class<Analysis> analysisClass) {
		this.analysisName = analysisName;
		this.analysisClass= analysisClass;

		//analysisFactoryList.add(this);
	}

//	/**
//	 * Get the Analysis for given method.
//	 * If Analysis has already been performed, the cached result is
//	 * returned.
//	 * 
//	 * @param jclass the class containing the method to analyze
//	 * @param method the method to analyze
//	 * 
//	 * @return the Analysis object representing the result of analyzing the method
//	 * @throws CFGBuilderException       if the CFG can't be constructed for the method
//	 * @throws DataflowAnalysisException if dataflow analysis fails on the method
//	 */
//	@CheckForNull public Analysis getAnalysis(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
////		AnalysisResult<Analysis> result = map.get(method);
////		if (result == null) {
//			if (ClassContext.TIME_ANALYSES) {
//				++ClassContext.depth;
//				ClassContext.indent();
//				System.out.println("CC: Starting " + analysisName + " for " +
//						SignatureConverter.convertMethodSignature(jclass, method) + ":");
//			}
//
//			long begin = System.currentTimeMillis();
//
////			// Create a new AnalysisResult
////			result = new AnalysisResult<Analysis>();
//
//			// Attempt to create the Analysis and store it in the AnalysisResult.
//			// If an exception occurs, record it in the AnalysisResult.
//			Analysis analysis;
////			try {
//				analysis = analyze(jclass, method);
////				result.setAnalysis(analysis);
////			} catch (CFGBuilderException e) {
////				result.setCFGBuilderException(e);
////			} catch (DataflowAnalysisException e) {
////				result.setDataflowAnalysisException(e);
////			} catch (AnalysisException e) {
////				result.setAnalysisException(e);
////			}
//
//			if (ClassContext.TIME_ANALYSES) {
//				long end = System.currentTimeMillis();
//				ClassContext.indent();
//				System.out.println("CC: finished " + analysisName + " in " + (end - begin) + " millis");
//				--ClassContext.depth;
//			}
//			
//			return analysis;
//
////			// Cache the outcome of this analysis attempt.
////			map.put(method, result);
////		}
//
////		return result.getAnalysis();
//	}
//
//	/**
//	 * Analyze the given method, returning a reference to the Analysis object.
//	 * 
//	 * @param jclass class containing the method to analyze
//	 * @param method method to analyze
//	 * @return the Analysis object resulting from the analysis
//	 * @throws CFGBuilderException
//	 * @throws DataflowAnalysisException
//	 */
//	@CheckForNull protected abstract Analysis analyze(JavaClass jclass, Method method)
//	throws DataflowAnalysisException, CFGBuilderException;

//	/**
//	 * @return true if this analysis factory is a dataflow analysis,
//	 *          false if not
//	 */
//	public abstract boolean isDataflow();

//	/**
//	 * Purge result for given method.
//	 * 
//	 * @param method the method whose analysis result should purged 
//	 */
//	public void purge(Method method) {
//		map.remove(method);
//	}

	/* ----------------------------------------------------------------------
	 * Helper methods to get required analysis objects.
	 * ---------------------------------------------------------------------- */

//	private<A> A getMethodAnalysis(JavaClass jclass, Method method, Class<A> analysisClass)
//			throws DataflowAnalysisException, CFGBuilderException {
//		MethodDescriptor methodDescriptor = BCELUtil.getMethodDescriptor(jclass, method);
//
//		try {
//			return Global.getAnalysisCache().getMethodAnalysis(analysisClass, methodDescriptor);
//		} catch (DataflowAnalysisException e) {
//			throw e;
//		} catch (CFGBuilderException e) {
//			throw e;
//		} catch (CheckedAnalysisException e) {
//			// For backwards compatibility, 
//			// analyses in the edu.umd.cs.findbugs.ba package
//			// should only throw DataflowAnalysisException or
//			// CFGBuilderException.
//
//			IllegalStateException ise = new IllegalStateException("Should not happen");
//			ise.initCause(e);
//			throw ise;
//		}
//	}
//
//	private<A> A getClassAnalysis(JavaClass jclass, Class<A> analysisClass)
//			throws DataflowAnalysisException, CFGBuilderException {
//		try {
//			ClassDescriptor classDescriptor = BCELUtil.getClassDescriptor(jclass);
//			return Global.getAnalysisCache().getClassAnalysis(analysisClass, classDescriptor);
//		} catch (DataflowAnalysisException e) {
//			throw e;
//		} catch (CFGBuilderException e) {
//			throw e;
//		} catch (CheckedAnalysisException e) {
//			// For backwards compatibility, 
//			// analyses in the edu.umd.cs.findbugs.ba package
//			// should only throw DataflowAnalysisException or
//			// CFGBuilderException.
//
//			IllegalStateException ise = new IllegalStateException("Should not happen");
//			ise.initCause(e);
//			throw ise;
//		}
//	}
//	
//	protected ClassContext getClassContext(JavaClass jclass) throws DataflowAnalysisException, CFGBuilderException {
//		return getClassAnalysis(jclass, ClassContext.class);
//	}
//
//	protected ConstantPoolGen getConstantPoolGen(JavaClass jclass) throws DataflowAnalysisException, CFGBuilderException {
//		return getClassAnalysis(jclass, ConstantPoolGen.class);
//	}
//
//	protected MethodGen getMethodGen(JavaClass jclass, Method method) {
//		try {
//			return getMethodAnalysis(jclass, method, MethodGen.class);
//		} catch (CheckedAnalysisException e) {
//			IllegalStateException ise = new IllegalStateException("Should not happen");
//			ise.initCause(e);
//			throw ise;
//		}
//	}
//
//	protected CFG getCFG(JavaClass jclass, Method method) throws CFGBuilderException {
//		try {
//			return getMethodAnalysis(jclass, method, CFG.class);
//		} catch (CFGBuilderException e) {
//			throw e;
//		} catch (CheckedAnalysisException e) {
//			IllegalStateException ise = new IllegalStateException("should not happen");
//			ise.initCause(e);
//			throw ise;
//		}
//	}
//
//	protected DepthFirstSearch getDepthFirstSearch(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, DepthFirstSearch.class);
//	}
//	
//	protected ReverseDepthFirstSearch getReverseDepthFirstSearch(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, ReverseDepthFirstSearch.class);
//	}
//
//	protected ValueNumberDataflow getValueNumberDataflow(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, ValueNumberDataflow.class);
//	}
//
//	protected AssertionMethods getAssertionMethods(JavaClass jclass) throws DataflowAnalysisException, CFGBuilderException {
//		return getClassAnalysis(jclass, AssertionMethods.class);
//	}
//
//	protected LoadedFieldSet getLoadedFieldSet(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, LoadedFieldSet.class);
//	}
//
////	protected CFG getRawCFG(JavaClass jclass, Method method) throws CFGBuilderException  {
////		throw new IllegalStateException();
////	}
//
//	protected ExceptionSetFactory getExceptionSetFactory(JavaClass jclass, Method method) {
//		try {
//			return getMethodAnalysis(jclass, method, ExceptionSetFactory.class);
//		} catch (CheckedAnalysisException e) {
//			IllegalStateException ise = new IllegalStateException("should not happen");
//			ise.initCause(e);
//			throw ise;
//		}
//	}
//	
//	protected CompactLocationNumbering getCompactLocationNumbering(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, CompactLocationNumbering.class);
//	}
//	
//	protected IsNullValueDataflow getIsNullValueDataflow(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, IsNullValueDataflow.class);
//	}
//	
//	protected TypeDataflow getTypeDataflow(JavaClass jclass, Method method) throws DataflowAnalysisException, CFGBuilderException {
//		return getMethodAnalysis(jclass, method, TypeDataflow.class);
//	}
	
	protected CFG getCFG(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(CFG.class, methodDescriptor);
	}
	
	protected DepthFirstSearch getDepthFirstSearch(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(DepthFirstSearch.class, methodDescriptor);
	}
	
	protected ConstantPoolGen getConstantPoolGen(IAnalysisCache analysisCache, ClassDescriptor classDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getClassAnalysis(ConstantPoolGen.class, classDescriptor);
	}
	
	protected MethodGen getMethodGen(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(MethodGen.class, methodDescriptor);
	}
	
	protected CompactLocationNumbering getCompactLocationNumbering(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(CompactLocationNumbering.class, methodDescriptor);
	}
	
	protected ValueNumberDataflow getValueNumberDataflow(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor) 
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(ValueNumberDataflow.class, methodDescriptor);
	}
	
	protected AssertionMethods getAssertionMethods(IAnalysisCache analysisCache, ClassDescriptor classDescriptor) 
			throws CheckedAnalysisException {
		return analysisCache.getClassAnalysis(AssertionMethods.class, classDescriptor);
	}
	
	protected JavaClass getJavaClass(IAnalysisCache analysisCache, ClassDescriptor classDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getClassAnalysis(JavaClass.class, classDescriptor);
	}
	
	protected Method getMethod(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(Method.class, methodDescriptor);
	}
	
	protected ReverseDepthFirstSearch getReverseDepthFirstSearch(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(ReverseDepthFirstSearch.class, methodDescriptor);
	}
	
	protected ExceptionSetFactory getExceptionSetFactory(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(ExceptionSetFactory.class, methodDescriptor);
	}
	
	protected IsNullValueDataflow getIsNullValueDataflow(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor) 
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(IsNullValueDataflow.class, methodDescriptor);
	}
	
	protected TypeDataflow getTypeDataflow(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(TypeDataflow.class, methodDescriptor);
	}
	
	protected LoadedFieldSet getLoadedFieldSet(IAnalysisCache analysisCache, MethodDescriptor methodDescriptor) 
			throws CheckedAnalysisException {
		return analysisCache.getMethodAnalysis(LoadedFieldSet.class, methodDescriptor);
	}
	
	/* ----------------------------------------------------------------------
	 * IAnalysisEngine methods
	 * ---------------------------------------------------------------------- */
	
//	/* (non-Javadoc)
//	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#analyze(edu.umd.cs.findbugs.classfile.IAnalysisCache, java.lang.Object)
//	 */
//	public Object analyze(IAnalysisCache analysisCache, MethodDescriptor descriptor) throws CheckedAnalysisException {
//		JavaClass jclass = analysisCache.getClassAnalysis(JavaClass.class, descriptor.getClassDescriptor());
//		Method method = analysisCache.getMethodAnalysis(Method.class, descriptor);
//		
//		return analyze(jclass, method);
//	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#retainAnalysisResults()
	 */
	public boolean retainAnalysisResults() {
	    return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.findbugs.classfile.IAnalysisEngine#registerWith(edu.umd.cs.findbugs.classfile.IAnalysisCache)
	 */
	public void registerWith(IAnalysisCache analysisCache) {
		analysisCache.registerMethodAnalysisEngine(analysisClass, this);
	}
}
