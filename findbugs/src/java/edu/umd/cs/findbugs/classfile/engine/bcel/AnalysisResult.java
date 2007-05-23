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

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.ba.AnalysisException;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.DataflowAnalysisException;

/**
 * An AnalysisResult stores the result of requesting an analysis
 * from an AnalysisFactory.  It can represent a successful outcome
 * (where the Analysis object can be returned), or an unsuccessful
 * outcome (where an exception was thrown trying to create the
 * analysis).  For unsuccessful outcomes, we rethrow the original
 * exception rather than making another attempt to create the analysis
 * (since if it fails once, it will never succeed). 
 * 
 * @author David Hovemeyer
 */
public class AnalysisResult<Analysis> {
	private boolean analysisSetExplicitly;
	private Analysis analysis;
	private AnalysisException analysisException;
	private CFGBuilderException cfgBuilderException;
	private DataflowAnalysisException dataflowAnalysisException;

	/**
	 * Get the analysis object.
	 * Throw an exception if an exception occurred while performing
	 * the analysis.
	 * 
	 * @return the analysis object
	 * @throws CFGBuilderException
	 * @throws DataflowAnalysisException
	 */
	public Analysis getAnalysis() throws CFGBuilderException, DataflowAnalysisException {
		if (analysisSetExplicitly)
			return analysis;
		if (dataflowAnalysisException != null)
			throw dataflowAnalysisException;
		if (analysisException != null)
			throw analysisException;
		if (cfgBuilderException != null)
			throw cfgBuilderException;
		throw new IllegalStateException();
	}

	/**
	 * Record a successful outcome, where the analysis was created.
	 * 
	 * @param analysis the Analysis
	 */
	public void setAnalysis(@Nullable Analysis analysis) {
		this.analysisSetExplicitly = true;
		this.analysis = analysis;
	}

	/**
	 * Record that an AnalysisException occurred while attempting
	 * to create the Analysis.
	 * 
	 * @param analysisException the AnalysisException
	 */
	public void setAnalysisException(AnalysisException analysisException) {
		this.analysisException = analysisException;
	}

	/**
	 * Record that a CFGBuilderException occurred while attempting
	 * to create the Analysis.
	 * 
	 * @param cfgBuilderException the CFGBuilderException
	 */
	public void setCFGBuilderException(CFGBuilderException cfgBuilderException) {
		this.cfgBuilderException = cfgBuilderException;
	}

	/**
	 * Record that a DataflowAnalysisException occurred while attempting
	 * to create the Analysis.
	 *  
	 * @param dataflowException the DataflowAnalysisException
	 */
	public void setDataflowAnalysisException(DataflowAnalysisException dataflowException) {
		this.dataflowAnalysisException = dataflowException;
	}
}