/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003-2005, University of Maryland
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

import java.io.IOException;

import edu.umd.cs.findbugs.xml.XMLAttributeList;
import edu.umd.cs.findbugs.xml.XMLOutput;

/**
 * Bug annotation class for local variable names
 *
 * @author William Pugh
 * @see BugAnnotation
 */
public class LocalVariableAnnotation implements ILocalVariableAnnotation {
	private static final long serialVersionUID = 1L;

	final private String value;
	final int register, pc;
	final int line;
	private String description;

	/**
	 * Constructor.
	 *
	 * @param name     the name of the local variable
	 * @param register the local variable index
	 * @param pc       the bytecode offset of the instruction that mentions
	 *                 this local variable
	 */
	public LocalVariableAnnotation(String name, int register, int pc) {
		this.value = name;
		this.register = register;
		this.pc = pc;
		this.line = -1;
		this.description = DEFAULT_ROLE;
		this.setDescription(name.equals("?") ? "LOCAL_VARIABLE_UNKNOWN" : "LOCAL_VARIABLE_NAMED");
	}
	/**
	 * Constructor.
	 *
	 * @param name     the name of the local variable
	 * @param register the local variable index
	 * @param pc       the bytecode offset of the instruction that mentions
	 *                 this local variable
	 */
	public LocalVariableAnnotation(String name, int register, int pc, int line) {
		this.value = name;
		this.register = register;
		this.pc = pc;
		this.line = line;
		this.description = DEFAULT_ROLE;
		this.setDescription(name.equals("?") ? "LOCAL_VARIABLE_UNKNOWN" : "LOCAL_VARIABLE_NAMED");
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}


	public void accept(BugAnnotationVisitor visitor) {
		visitor.visitLocalVariableAnnotation(this);
	}

	public String format(String key, IClassAnnotation primaryClass) {
		// System.out.println("format: " + key + " reg: " + register + " name: " + value);
		if (key.equals("hash")) {
			if (register < 0) {
				return "??";
			}
			return value;
		}
		if (register < 0) {
			return "?";
		}
		if (key.equals("register")) {
			return String.valueOf(register);
		} else if (key.equals("pc")) {
			return String.valueOf(pc);
		} else if (key.equals("name") || key.equals("givenClass")) {
			return value;
		} else if (!value.equals("?")) {
			return value;
		}
		return "$L"+register;
	}

	public void setDescription(String description) {
		this.description = description.intern();
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ILocalVariableAnnotation)) {
			return false;
		}
		return value.equals(((ILocalVariableAnnotation) o).getName());
	}

	public int compareTo(BugAnnotation o) {
		if (!(o instanceof ILocalVariableAnnotation)) {
			return this.getClass().getName().compareTo(o.getClass().getName());
		}
		return value.compareTo(((ILocalVariableAnnotation) o).getName());
	}

	@Override
	public String toString() {
		String pattern = I18N.instance().getAnnotationDescription(description);
		FindBugsMessageFormat format = new FindBugsMessageFormat(pattern);
		return format.format(new BugAnnotation[]{this}, null);
	}

	/* ----------------------------------------------------------------------
	 * XML Conversion support
	 * ---------------------------------------------------------------------- */

	private static final String ELEMENT_NAME = "LocalVariable";

	public void writeXML(XMLOutput xmlOutput) throws IOException {
		writeXML(xmlOutput, false, false);
	}

	public void writeXML(XMLOutput xmlOutput, boolean addMessages, boolean isPrimary) throws IOException {
		XMLAttributeList attributeList = new XMLAttributeList()
		.addAttribute("name", value)
		.addAttribute("register", String.valueOf(register))
		.addAttribute("pc", String.valueOf(pc));

		String role = getDescription();
		if (!role.equals(DEFAULT_ROLE)) {
			attributeList.addAttribute("role", role);
		}

		BugAnnotationUtil.writeXML(xmlOutput, ELEMENT_NAME, this, attributeList, addMessages);
	}

	public boolean isNamed() {
		return register >= 0 && !value.equals("?");
	}

	/**
	 * @return name of local variable
	 */
	public String getName() {
		return value;
	}

	public int getPC() {
		return pc;
	}
	public int getRegister() {
		return register;
	}

	public boolean isSignificant() {
		return !value.equals("?");
	}

	public String toString(IClassAnnotation primaryClass) {
		return toString();
	}
}
