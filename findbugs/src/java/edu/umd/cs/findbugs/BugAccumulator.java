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

package edu.umd.cs.findbugs;

import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.ba.AnalysisFeatures;
import edu.umd.cs.findbugs.util.MultiMap;

import java.util.TreeSet;

/**
 * Accumulate warnings that may occur at multiple source locations,
 * consolidating them into a single warning.
 * 
 * @author Bill Pugh
 * @author David Hovemeyer
 */
public class BugAccumulator {

	private BugReporter reporter;
	private final boolean performAccumulation;
	private MultiMap<BugInstance, ISourceLineAnnotation> map;
	
	/**
	 * Constructor.
	 * 
	 * @param reporter the BugReporter to which warnings should eventually be reported
	 */
	public BugAccumulator(BugReporter reporter) {
		this.reporter = reporter;
		performAccumulation = AnalysisContext.currentAnalysisContext().getBoolProperty(AnalysisFeatures.MERGE_SIMILAR_WARNINGS);
		this.map = new MultiMap<BugInstance, ISourceLineAnnotation>(TreeSet.class);
	}

	/**
	 * Accumulate a warning at given source location.
	 * 
	 * @param bug        the warning
	 * @param sourceLine the source location
	 */
	public void accumulateBug(BugInstance bug, ISourceLineAnnotation sourceLine) {
		if (performAccumulation) {
	        map.add(bug,sourceLine);
        } else {
	        reporter.reportBug(bug.add(sourceLine));
        }
	}

	public Iterable<? extends BugInstance> uniqueBugs() {
		return map.keySet();
	}
	
	public Iterable<? extends ISourceLineAnnotation> locations(BugInstance bug) {
		return map.get(bug);
	}
	
	/**
	 * Report accumulated warnings to the BugReporter.
	 * Clears all accumulated warnings as a side-effect.
	 */
	public void reportAccumulatedBugs() {
		for(BugInstance bug : map.keySet()) {
			boolean first = true;
			for (ISourceLineAnnotation source  : map.get(bug)) {
				if (source != null) {
					bug.add(source);
					if (first) {
						first = false;
					} else {
						bug.describe(ISourceLineAnnotation.ROLE_ANOTHER_INSTANCE);
					}
				}
			}
			reporter.reportBug(bug);
		}
		clearBugs();
	}

	/**
     * Clear all accumulated bugs without reporting them
     */
    public void clearBugs() {
    	map.clear();
    }

}
