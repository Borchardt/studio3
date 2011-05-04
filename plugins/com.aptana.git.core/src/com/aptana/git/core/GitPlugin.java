/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.git.core;

import java.io.File;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.history.IFileRevision;
import org.osgi.framework.BundleContext;

import com.aptana.core.util.EclipseUtil;
import com.aptana.core.util.ResourceUtil;
import com.aptana.git.core.model.GitCommit;
import com.aptana.git.core.model.GitRepositoryManager;
import com.aptana.git.core.model.IGitRepositoryManager;
import com.aptana.git.internal.core.storage.CommitFileRevision;

/**
 * The activator class controls the plug-in life cycle
 */
public class GitPlugin extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.git.core"; //$NON-NLS-1$

	// The shared instance
	private static GitPlugin plugin;

	private GitProjectRefresher fRepoListener;
	private IResourceChangeListener fGitResourceListener;

	private GitRepositoryManager fGitRepoManager;

	/**
	 * The constructor
	 */
	public GitPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		// Add a resource listener that triggers git repo index refreshes!
		Job job = new Job("Add Git Index Resource listener") //$NON-NLS-1$
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				fGitResourceListener = new GitResourceListener();
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fGitResourceListener,
						IResourceChangeEvent.POST_CHANGE);
				fRepoListener = new GitProjectRefresher();
				getGitRepositoryManager().addListener(fRepoListener);
				getGitRepositoryManager().addListenerToEachRepository(fRepoListener);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(!EclipseUtil.showSystemJobs());
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fGitResourceListener);
			getGitRepositoryManager().removeListener(fRepoListener);
			getGitRepositoryManager().removeListenerFromEachRepository(fRepoListener);
			// Remove all the GitRepositories from memory!
			if (fGitRepoManager != null)
				fGitRepoManager.cleanup();
		}
		finally
		{
			fGitRepoManager = null;
			plugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static GitPlugin getDefault()
	{
		return plugin;
	}

	public static String getPluginId()
	{
		return PLUGIN_ID;
	}

	public static void logError(String msg, Throwable e)
	{
		getDefault().getLog().log(new Status(IStatus.ERROR, getPluginId(), msg, e));
	}

	protected static void logError(CoreException e)
	{
		getDefault().getLog().log(e.getStatus());
	}

	public static void logWarning(String warning)
	{
		if (getDefault() != null)
			getDefault().getLog().log(new Status(IStatus.WARNING, getPluginId(), warning));
	}

	public static void logError(Exception e)
	{
		if (getDefault() != null)
			getDefault().getLog().log(new Status(IStatus.WARNING, getPluginId(), "", e)); //$NON-NLS-1$
	}

	public static void logInfo(String string)
	{
		if (getDefault() != null && getDefault().isDebugging())
			getDefault().getLog().log(new Status(IStatus.INFO, getPluginId(), string));
	}

	/**
	 * FIXME This doesn't seem like the best place to stick this.
	 * 
	 * @param commit
	 * @param repoRelativePath
	 * @return
	 */
	public static IFileRevision revisionForCommit(GitCommit commit, IPath repoRelativePath)
	{
		return new CommitFileRevision(commit, repoRelativePath);
	}

	public IPath getGIT_SSH()
	{
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			File sshwFile = ResourceUtil.resourcePathToFile(FileLocator.find(getBundle(), Path.fromPortableString("$os$/sshw.exe"), null)); //$NON-NLS-1$
			if (sshwFile.isFile())
			{
				return Path.fromOSString(sshwFile.getAbsolutePath());
			}
		}
		return null;
	}

	public IPath getGIT_ASKPASS()
	{
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			return getGIT_SSH();
		} else if (Platform.OS_LINUX.equals(Platform.getOS())
				|| Platform.OS_MACOSX.equals(Platform.getOS())) {
			File askpassFile = ResourceUtil.resourcePathToFile(FileLocator.find(getBundle(), Path.fromPortableString("$os$/askpass.tcl"), null)); //$NON-NLS-1$
			if (askpassFile.isFile()) {
				return Path.fromOSString(askpassFile.getAbsolutePath());
			}
		}
		return null;
	}

	public IGitRepositoryManager getGitRepositoryManager()
	{
		if (fGitRepoManager == null)
		{
			fGitRepoManager = new GitRepositoryManager();
		}
		return fGitRepoManager;
	}
}
