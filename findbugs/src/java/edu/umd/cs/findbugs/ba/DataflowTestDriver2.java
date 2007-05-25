/*
 * FindBugs - Find Bugs in Java programs
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

package edu.umd.cs.findbugs.ba;

import java.io.IOException;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IClassFactory;
import edu.umd.cs.findbugs.classfile.IClassPath;
import edu.umd.cs.findbugs.classfile.IClassPathBuilder;
import edu.umd.cs.findbugs.classfile.IClassPathBuilderProgress;
import edu.umd.cs.findbugs.classfile.ICodeBase;
import edu.umd.cs.findbugs.classfile.IErrorLogger;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.classfile.analysis.ClassInfo;
import edu.umd.cs.findbugs.classfile.impl.ClassFactory;

/**
 * Generic dataflow test driver which uses IAnalysisCache.
 * 
 * @author David Hovemeyer
 */
public abstract class DataflowTestDriver2<FactType, AnalysisType extends AbstractDataflowAnalysis<FactType>> {
	
	public DataflowTestDriver2() {
	}
	
	protected abstract AnalysisType createAnalysis(
			IAnalysisCache analysisCache, MethodDescriptor methodDescriptor)
			throws CheckedAnalysisException;
	
	public void execute(String classFileName) throws CheckedAnalysisException, IOException, InterruptedException {
		IClassFactory factory = ClassFactory.instance();

		IClassPath classPath = factory.createClassPath();
		IErrorLogger errorLogger = createErrorLogger();
		
		IAnalysisCache analysisCache = factory.createAnalysisCache(classPath, errorLogger);
		
		IClassPathBuilder builder = factory.createClassPathBuilder(errorLogger);
		builder.addCodeBase(factory.createFilesystemCodeBaseLocator(classFileName), true);
		builder.addCodeBase(factory.createFilesystemCodeBaseLocator("."), false);
		
		builder.build(classPath, new IClassPathBuilderProgress() {
			public void finishArchive() {
				// do nothing.
            }
		});

		List<ClassDescriptor> appClassList = builder.getAppClassList();
		for (ClassDescriptor classDescriptor : appClassList) {
			ClassInfo classInfo = analysisCache.getClassAnalysis(ClassInfo.class, classDescriptor);
			
			for (MethodDescriptor methodDescriptor : classInfo.getMethodDescriptorList()) {
				Method method = analysisCache.getMethodAnalysis(Method.class, methodDescriptor);
				if (method.getCode() == null) {
					continue;
				}
				
				MethodGen methodGen = analysisCache.getMethodAnalysis(MethodGen.class, methodDescriptor);
				if (methodGen == null) {
					continue;
				}
				
				AnalysisType analysis = createAnalysis(analysisCache, methodDescriptor);
				CFG cfg = analysisCache.getMethodAnalysis(CFG.class, methodDescriptor);
				Dataflow<FactType, AnalysisType> dataflow =
					new Dataflow<FactType, AnalysisType>(cfg, analysis);
				dataflow.execute();
				
				// TODO: print CFG
			}
		}
	}

	/**
	 * Create an IErrorLogger that logs to System.err.
     */
    private IErrorLogger createErrorLogger() {
	    return new IErrorLogger() {
			public void logError(String message) {
				System.err.println("Error: " + message);
            }

			public void logError(String message, Throwable e) {
				System.err.println("Error: " + message + ": " + e.toString());
            }

			public void reportMissingClass(ClassNotFoundException ex) {
				System.err.println("Missing class: " + ClassNotFoundExceptionParser.getMissingClassName(ex));
            }

			public void reportMissingClass(ClassDescriptor classDescriptor) {
				System.err.println("Missing class: " + classDescriptor.toDottedClassName());
            }

			public void reportSkippedAnalysis(MethodDescriptor method) {
				System.err.println("Skipped analysis of method: " + method.toString());
            }
		};
    }
}
