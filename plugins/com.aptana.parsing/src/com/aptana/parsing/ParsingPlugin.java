/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.parsing;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.aptana.core.logging.IdeLog;

/**
 * The activator class controls the plug-in life cycle
 */
public class ParsingPlugin extends Plugin
{
	public static final String PLUGIN_ID = "com.aptana.parsing"; //$NON-NLS-1$
	private static ParsingPlugin plugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ParsingPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * logError
	 * 
	 * @param e
	 */
	public static void logError(Exception e)
	{
		logError(e.getMessage(), e);
	}

	/**
	 * logError
	 * 
	 * @param msg
	 * @param e
	 */
	public static void logError(String msg, Throwable e)
	{
		IdeLog.logError(getDefault(), msg, e);
	}

	/**
	 * logInfo
	 * 
	 * @param message
	 */
	public static void logInfo(String message)
	{
		IdeLog.logInfo(getDefault(), message);
	}

	/**
	 * logWarning
	 * 
	 * @param message
	 */
	public static void logWarning(String message)
	{
		IdeLog.logWarning(getDefault(), message);
	}

	/**
	 * The constructor
	 */
	public ParsingPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			ParserPoolFactory.getInstance().dispose();
		}
		finally
		{
			plugin = null;
			super.stop(context);
		}
	}
}
