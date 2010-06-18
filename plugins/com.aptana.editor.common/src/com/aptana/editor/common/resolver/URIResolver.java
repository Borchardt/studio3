package com.aptana.editor.common.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.common.CommonEditorPlugin;

/**
 * Resolves paths that may be relative to a base URI (filesystem, remote, etc)
 * 
 * @author cwilliams
 */
public class URIResolver implements IPathResolver
{

	private URI baseURI;

	public URIResolver(URI baseURI)
	{
		this.baseURI = baseURI;
	}

	/**
	 * Returns null if unable to resolve the path to a URI and grab the contents.
	 */
	@Override
	public String resolveSource(String path, IProgressMonitor monitor) throws Exception
	{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		URI uri = resolveURI(path);
		if (uri == null)
			return null;
		sub.worked(5);
		// get the filesystem that can handle the URI
		IFileStore store = getFileStore(uri);

		int options = EFS.CACHE;
		// If file is local no need to cache
		if (store.getFileSystem().equals(EFS.getLocalFileSystem()))
		{
			options = EFS.NONE;
		}
		// grab down a local copy
		File aFile = store.toLocalFile(options, sub.newChild(90));
		if (aFile == null || !aFile.exists())
		{
			// Need to pass up correct original filename and says that's the one that doesn't exist
			throw new FileNotFoundException(uri.toString());
		}
		// now read in the local copy
		return IOUtil.read(new FileInputStream(aFile));
	}

	protected IFileStore getFileStore(URI uri) throws CoreException
	{
		IFileSystem fileSystem = EFS.getFileSystem(uri.getScheme());
		if (fileSystem == null)
			return EFS.getNullFileSystem().getStore(uri);
		return fileSystem.getStore(uri);
	}

	public URI resolveURI(String path)
	{
		if (path == null)
			return null;
		URI uri;
		try
		{
			// try to parse as a URI
			uri = URI.create(path);
			String scheme = uri.getScheme();
			if (scheme == null)
			{
				// no scheme, means it's relative to base URI, or an absolute file path?
				uri = baseURI.resolve(path);
			}
		}
		catch (IllegalArgumentException e)
		{
			// fails to parse, try resolving against base URI
			uri = baseURI.resolve(path);
			// TODO What if it fails here, then what do we do?
		}

		try
		{
			IFileStore store = getFileStore(uri);
			IFileInfo info = store.fetchInfo();
			if (info.exists())
			{
				return uri;
			}
		}
		catch (CoreException e)
		{
			CommonEditorPlugin.logError(e);
		}

		return null;
	}
}
