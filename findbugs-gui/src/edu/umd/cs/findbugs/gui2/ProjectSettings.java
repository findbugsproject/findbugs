/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2006, University of Maryland
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

package edu.umd.cs.findbugs.gui2;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.WillClose;

import edu.umd.cs.findbugs.filter.LastVersionMatcher;

/**
 * This is the .fas file stored when projects are saved
 * All project related information goes here.  Anything that would be shared between multiple projects goes into GUISaveState instead
 */
@Deprecated
public class ProjectSettings implements Serializable
{
	private static final long serialVersionUID = 6505872267795979672L;

	// Singleton
	private ProjectSettings()
	{
		allMatchers = new CompoundMatcher();
		filters = new ArrayList<FilterMatcher>();
	}
	private static ProjectSettings instance;
	public static ProjectSettings newInstance()
	{
		instance = new ProjectSettings();
		LastVersionMatcher dbf= LastVersionMatcher.DEAD_BUG_MATCHER;

		//Important: add the deadbug filter directly to filters and allmatchers, dont go through addFilter, otherwise it causes a
		//tree to rebuild.
		MainFrame.getInstance().getProject().getSuppressionFilter().addChild(dbf);
		PreferencesFrame.getInstance().updateFilterPanel();
		return instance;
	}
	public static synchronized ProjectSettings getInstance()
	{
		if (instance == null) {
			instance= new ProjectSettings();
		}
		return instance;
	}


	/**
	 * The list of all defined filters
	 */
	private final ArrayList<FilterMatcher> filters;

	/**
	 * The CompoundMatcher enveloping all enabled matchers.
	 */
	private final CompoundMatcher allMatchers;

	public static void loadInstance(@WillClose InputStream in)
	{
		try
		{
			instance = (ProjectSettings) new ObjectInputStream(in).readObject();
			PreferencesFrame.getInstance().updateFilterPanel();

		}
		catch (ClassNotFoundException e)
		{
			if (MainFrame.DEBUG) {
				System.err.println("Error in deserializing Settings:");
			}
			Debug.println(e);
		}
		catch (IOException e)
		{
			if (MainFrame.DEBUG) {
				System.err.println("IO error in deserializing Settings:");
			}
			Debug.println(e);
			instance=newInstance();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				assert false;
			}
		}
	}

	public void save(@WillClose OutputStream out)
	{
		try
		{
			new ObjectOutputStream(out).writeObject(this);
		}
		catch (IOException e)
		{
			if (MainFrame.DEBUG) {
				System.err.println("Error serializing Settings:");
			}
			Debug.println(e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// nothing to do
				assert true;
			}
		}
	}

	public void addFilters(FilterMatcher[] newFilters)
	{
		for (FilterMatcher i : newFilters) {
			if (!filters.contains(i))
			{
				filters.add(i);
				allMatchers.add(i);
			}
			else //if filters contains i
			{
				filters.get(filters.indexOf(i)).setActive(true);
				//FIXME Do I need to do this for allMatchers too?  Or are the filters all the same, with both just holding references?
			}
		}
		FilterActivity.notifyListeners(FilterListener.Action.FILTERING, null);
		PreferencesFrame.getInstance().updateFilterPanel();
		MainFrame.getInstance().updateStatusBar();
	}

}
