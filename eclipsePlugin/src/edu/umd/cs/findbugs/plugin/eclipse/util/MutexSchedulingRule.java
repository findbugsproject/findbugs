/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2006 University of Maryland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307, USA
 */
package edu.umd.cs.findbugs.plugin.eclipse.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A complicated scheduling rule for mutually exclusivity, derived from:
 * http://help.eclipse.org/help30/topic/org.eclipse.platform.doc.isv/guide/
 * runtime_jobs_rules.htm
 *
 * This rule takes the available cores into account and also allows to run
 * FB independently on different resources (if there are enough cores available)
 */
public class MutexSchedulingRule implements ISchedulingRule {

    /**
     * Guesses optimal number of concurrently executing jobs on current system /
     * JVM. Since analysis jobs are CPU, memory and sometimes IO intensive, we
     * must take into consideration more than just number of cores.
     *
     * @return optimal number of concurrently executing jobs on current system /
     *         JVM.
     */
    private static int guessBestConcurrentJobsLimit() {
        int cores = Runtime.getRuntime().availableProcessors();
        if (cores == 1) {
            /*
             * Given single processor, it could make sense to run more jobs if
             * they were just IO bound, but they aren't.
             */
            return cores;
        }
        if (cores == 2) {
            /*
             * Given multi-core, we want to return more than 1, so that
             * MULTICORE logic stays OK.
             */
            return cores;
        }

        /*
         * Given more cores, we don't want to eat them all up, just in case user
         * wants to do something else in the mean time (within Eclipse or not)
         * and needs CPU for that.
         *
         * For small projects under analysis, it would make more sense to just
         * do the job using all cores, but for those small projects, we will be
         * done quickly anyway. For bigger projects, analysis will take longer
         * time, so we mustn't make IDE unusable during that.
         */
        int limit = cores - 1;

        /*
         * Guesstimate amount of memory each analysis consumes. Imposes limit on
         * concurrently running analysis. Of course, this depends on active
         * detectors, analysis effort, code under analysis and perhaps other
         * factors -- but those cannot be taken into consideration here.
         */
        final long memoryUsagePerAnalysis = 756 * 1024 * 1024;

        long memoryMax = Runtime.getRuntime().maxMemory();
        long memoryLimit = memoryMax / memoryUsagePerAnalysis;
        if (memoryLimit > 0 && memoryLimit < Integer.MAX_VALUE) {
            limit = Math.min(limit, (int) memoryLimit);
        }

        return limit;
    }

    // enable multicore
    public static final int MAX_JOBS = guessBestConcurrentJobsLimit();
    public static final boolean MULTICORE = MAX_JOBS > 1;

    private final IResource resource;

    public MutexSchedulingRule(IResource resource) {
        super();
        this.resource = resource;
    }

    public boolean isConflicting(ISchedulingRule rule) {
        if (!(rule instanceof MutexSchedulingRule)) {
            return false;
        }
        MutexSchedulingRule mRule = (MutexSchedulingRule) rule;
        if (resource == null || mRule.resource == null) {
            // we don't know the resource, so better to say we have conflict
            return true;
        }
        if (MULTICORE) {
            return resource.contains(mRule.resource);
        }
        return true;
    }

    public boolean contains(ISchedulingRule rule) {
        return isConflicting(rule);
    }

    @Override
    public String toString() {
        return "MutexSchedulingRule, resource: " + resource;
    }

}
