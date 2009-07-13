/*
 * Contributions to FindBugs
 * Copyright (C) 2009, Tom\u00e1s Pollak
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
package edu.umd.cs.findbugs.architecture;

import java.io.File;

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import junit.framework.TestCase;

/**
 * Verifies the package dependencies.
 *
 * @author Tom\u00e1s Pollak
 * @author Andrei Loskutov
 */
public class PackageDependenciesTest extends TestCase {
	private JDepend engine;

	/**
	 * The only packages today which have no cycles...
	 * Should be much more here!
	 */
	public void testNoCycles() throws Exception {
	    assertNoCycles("edu.umd.cs.findbugs.ba.generic");
	    assertNoCycles("edu.umd.cs.findbugs.graph");
	    assertNoCycles("edu.umd.cs.findbugs.io");
	    assertNoCycles("edu.umd.cs.findbugs.signature");
	    assertNoCycles("edu.umd.cs.findbugs.util");
	    assertNoCycles("edu.umd.cs.findbugs.xml");
    }

	public void testAnnotationDependencies() {
	    String testPackage = "edu.umd.cs.findbugs.ann";

	    // This is the only package which should use annotations
	    // assertFirstNotDependOnSecond("edu.umd.cs.findbugs.detect", testPackage);

	    // TODO the root should not depend too, but currently it does...
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs", testPackage);

	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.asm", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.ba", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.bcel", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.classfile", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.cloud", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.graph", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.io", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.log", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.model", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.plan", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.signature", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.util", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.visitclass", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.xml", testPackage);
	    assertFirstNotDependOnSecond("edu.umd.cs.findbugs.xml", testPackage);

	    assertFirstNotDependOnSecond(testPackage, "edu.umd.cs.findbugs.detect");
	}

	public void testGui2Dependencies() {
		String testPackage = "edu.umd.cs.findbugs.gui2";

		assertFirstNotDependOnSecond("edu.umd.cs.findbugs", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.asm", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.ba", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.bcel", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.classfile", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.cloud", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.detect", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.graph", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.io", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.log", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.model", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.plan", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.signature", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.util", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.visitclass", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.xml", testPackage);
	}

	public void testCloudDependencies() {
		String testPackage = "edu.umd.cs.findbugs.cloud";
		// TODO refactor code to made core independent from annotation plugin implementation
//		assertFirstNotDependOnSecond("edu.umd.cs.findbugs", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.asm", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.ba", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.bcel", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.classfile", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.detect", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.graph", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.io", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.log", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.model", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.plan", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.signature", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.util", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.visitclass", testPackage);
		assertFirstNotDependOnSecond("edu.umd.cs.findbugs.xml", testPackage);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Setup the JDepend analysis
		engine = new JDepend();

		// Get the classes root directory
		String rootDirectory = getClass().getResource("/").getFile();

		if(rootDirectory.endsWith("/bin/") || rootDirectory.endsWith("/bin")){
		    // started from eclipse UI
		    engine.addDirectory(new File(rootDirectory + "../../findbugs/classesEclipse").toString());
		    engine.addDirectory(new File(rootDirectory + "../../findbugs-gui/bin").toString());
		} else {
		    // started from ant
		    engine.addDirectory(rootDirectory);
		}
		engine.analyze();
	}

	@Override
	protected void tearDown() throws Exception {
		engine = null;

		super.tearDown();
	}

	private void assertFirstNotDependOnSecond(String first, String second) {
		JavaPackage afferentPackage = engine.getPackage(first);
		JavaPackage efferentPackage = engine.getPackage(second);
		assertFalse(afferentPackage.getName() + " shouldn't depend on " + efferentPackage.getName(), afferentPackage
				.getEfferents().contains(efferentPackage));
	}

	private void assertNoCycles(String afferent) {
	    JavaPackage afferentPackage = engine.getPackage(afferent);
	    assertFalse(afferentPackage.getName() +
	            " has dependency cycle: depends on " + afferentPackage.getEfferents()
	            + " and used by: " + afferentPackage.getAfferents(),
	            afferentPackage.containsCycle());

	}
}
