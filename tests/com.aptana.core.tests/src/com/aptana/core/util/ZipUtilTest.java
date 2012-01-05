/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

@SuppressWarnings("nls")
public class ZipUtilTest extends TestCase
{

	private static final String BUNDLE_ID = "com.aptana.core.tests";

	private static final String STREAM_TEST_ZIP = "resources/streamtest.zip";

	private static final String TEST_ZIP = "resources/test.zip";
	private static final String TEST_OVERWRITE_ZIP = "resources/test_overwrite.zip";
	private static final String TEST_OVERWRITE_DIR_ZIP = "resources/test_overwrite_dir.zip";
	private static final String TEST_ZIP_SYMLINKS = "resources/test_symlinks.zip";
	private static final HashSet<String> TOP_ENTRIES = new HashSet<String>(Arrays.asList("folder", "file.txt"));
	private static final HashSet<String> TOP_OVERWRITE_ENTRIES = new HashSet<String>(Arrays.asList("folder",
			"newFolder", "file.txt"));
	private static final HashSet<String> TOP_ENTRIES_SYMLINK = new HashSet<String>(Arrays.asList("folder", "file.txt",
			"filesym.txt", "othersym"));

	public void testUnzipFile() throws IOException
	{
		URL resourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_ZIP);
		assertNotNull(resourceURL);
		File resourceFile = ResourceUtil.resourcePathToFile(resourceURL);
		assertNotNull(resourceFile);

		File destinationDir = File.createTempFile(getClass().getSimpleName(), null);
		assertTrue(destinationDir.delete());
		assertTrue(destinationDir.mkdirs());

		try
		{
			assertEquals(Status.OK_STATUS, ZipUtil.extract(resourceFile, destinationDir, new NullProgressMonitor()));

			File[] files = destinationDir.listFiles();
			assertEquals("Unzipped contents to not match expected number of files", TOP_ENTRIES.size(), files.length);

			for (File file : files)
			{
				assertTrue("Unexpected zip entry " + file.getName(), TOP_ENTRIES.contains(file.getName()));
			}
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "file.txt").isFile());
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder/other").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "folder/file.txt").isFile());
		}
		finally
		{
			// remove the contents after we are done with the test
			FileUtil.deleteRecursively(destinationDir);
		}
	}

	/**
	 * Test unzipping the test.zip with an overwrite flag (expected to act the same as without the overwrite)
	 * 
	 * @throws Exception
	 */
	public void testUnzipFileWithOverwrite() throws Exception
	{
		URL resourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_ZIP);
		assertNotNull(resourceURL);
		File resourceFile = ResourceUtil.resourcePathToFile(resourceURL);
		assertNotNull(resourceFile);

		File destinationDir = File.createTempFile(getClass().getSimpleName(), null);
		assertTrue(destinationDir.delete());
		assertTrue(destinationDir.mkdirs());

		try
		{
			// Extract once.
			assertEquals(Status.OK_STATUS, ZipUtil.extract(resourceFile, destinationDir, new NullProgressMonitor()));
			// Extract again, with an overwrite mode.
			assertEquals(Status.OK_STATUS,
					ZipUtil.extract(resourceFile, destinationDir, true, new NullProgressMonitor()));

			File[] files = destinationDir.listFiles();
			assertEquals("Unzipped contents to not match expected number of files", TOP_ENTRIES.size(), files.length);

			for (File file : files)
			{
				assertTrue("Unexpected zip entry " + file.getName(), TOP_ENTRIES.contains(file.getName()));
			}
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "file.txt").isFile());
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder/other").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "folder/file.txt").isFile());
		}
		finally
		{
			// remove the contents after we are done with the test
			FileUtil.deleteRecursively(destinationDir);
		}
	}

	/**
	 * Test unzipping the test_overwrite with an overwrite flag.
	 * 
	 * @throws Exception
	 */
	public void testUnzipWithChangedOverwrite() throws Exception
	{
		URL resourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_ZIP);
		URL overwriteResourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_OVERWRITE_ZIP);
		assertNotNull(resourceURL);
		assertNotNull(overwriteResourceURL);

		File resourceFile = ResourceUtil.resourcePathToFile(resourceURL);
		File overwriteResourceFile = ResourceUtil.resourcePathToFile(overwriteResourceURL);

		assertNotNull(resourceFile);
		assertNotNull(overwriteResourceFile);

		File destinationDir = File.createTempFile(getClass().getSimpleName(), null);
		assertTrue(destinationDir.delete());
		assertTrue(destinationDir.mkdirs());

		try
		{
			// Extract once.
			assertEquals(Status.OK_STATUS, ZipUtil.extract(resourceFile, destinationDir, new NullProgressMonitor()));
			// Extract again, with an overwrite mode.
			assertEquals(Status.OK_STATUS,
					ZipUtil.extract(overwriteResourceFile, destinationDir, true, new NullProgressMonitor()));

			File[] files = destinationDir.listFiles();
			assertEquals("Unzipped contents to not match expected number of files", TOP_OVERWRITE_ENTRIES.size(),
					files.length);

			for (File file : files)
			{
				assertTrue("Unexpected zip entry " + file.getName(), TOP_OVERWRITE_ENTRIES.contains(file.getName()));
			}
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder").isDirectory());
			assertTrue("Expected entry is not a directory", new File(destinationDir, "newFolder").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "file.txt").isFile());
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder/other").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "folder/other/hello.txt").isFile());
			assertTrue("Expected entry is not a file", new File(destinationDir, "folder/file.txt").isFile());
			assertTrue("Expected entry is not a file", new File(destinationDir, "newFolder/newFile.txt").isFile());

			// The file.txt should now contain the string "new content"
			assertEquals("Expected 'new content' as the overitten content of 'file.txt'", "new content",
					IOUtil.read(new FileInputStream(new File(destinationDir, "folder/file.txt"))));
		}
		finally
		{
			// remove the contents after we are done with the test
			FileUtil.deleteRecursively(destinationDir);
		}
	}

	/*
	 * Test a case where the overriding Zip has a file that overwrite a folder on the target directory.
	 */
	public void testOverwriteDirWithFile() throws Exception
	{
		URL resourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_ZIP);
		URL overwriteResourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_OVERWRITE_DIR_ZIP);
		assertNotNull(resourceURL);
		assertNotNull(overwriteResourceURL);

		File resourceFile = ResourceUtil.resourcePathToFile(resourceURL);
		File overwriteResourceFile = ResourceUtil.resourcePathToFile(overwriteResourceURL);

		assertNotNull(resourceFile);
		assertNotNull(overwriteResourceFile);

		File destinationDir = File.createTempFile(getClass().getSimpleName(), null);
		assertTrue(destinationDir.delete());
		assertTrue(destinationDir.mkdirs());

		try
		{
			// Extract once.
			assertEquals(Status.OK_STATUS, ZipUtil.extract(resourceFile, destinationDir, new NullProgressMonitor()));
			// Extract again, with an overwrite mode.
			assertEquals(Status.OK_STATUS,
					ZipUtil.extract(overwriteResourceFile, destinationDir, true, new NullProgressMonitor()));

			File[] files = destinationDir.listFiles();
			assertEquals("Unzipped contents to not match expected number of files", 2, files.length);

			// The previous 'folder' directory should now be a file.
			assertTrue("Expected entry is not a file", new File(destinationDir, "folder").isFile());
			assertTrue("Expected entry is not a file", new File(destinationDir, "file.txt").isFile());
		}
		finally
		{
			// remove the contents after we are done with the test
			FileUtil.deleteRecursively(destinationDir);
		}
	}

	public void testUnzipFileWithSymlinks() throws IOException
	{
		URL resourceURL = Platform.getBundle(BUNDLE_ID).getEntry(TEST_ZIP_SYMLINKS);
		assertNotNull(resourceURL);
		File resourceFile = ResourceUtil.resourcePathToFile(resourceURL);
		assertNotNull(resourceFile);

		File destinationDir = File.createTempFile(getClass().getSimpleName(), null);
		assertTrue(destinationDir.delete());
		assertTrue(destinationDir.mkdirs());

		try
		{
			assertEquals(Status.OK_STATUS, ZipUtil.extract(resourceFile, destinationDir, new NullProgressMonitor()));

			File[] files = destinationDir.listFiles();
			assertEquals("Unzipped contents to not match expected number of files", TOP_ENTRIES_SYMLINK.size(),
					files.length);

			for (File file : files)
			{
				assertTrue("Unexpected zip entry " + file.getName(), TOP_ENTRIES_SYMLINK.contains(file.getName()));
			}
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "file.txt").isFile());
			assertTrue("Expected entry is not a directory", new File(destinationDir, "folder/other").isDirectory());
			assertTrue("Expected entry is not a file", new File(destinationDir, "folder/file.txt").isFile());

			assertTrue("Expected entry is not a symlink", FileUtil.isSymlink(new File(destinationDir, "filesym.txt")));
			assertTrue("Expected entry is not a symlink", FileUtil.isSymlink(new File(destinationDir, "othersym")));
		}
		finally
		{
			// remove the contents after we are done with the test
			FileUtil.deleteRecursively(destinationDir);
		}
	}

	public void testOpenEntry() throws IOException
	{
		URL zipURL = Platform.getBundle(BUNDLE_ID).getEntry(STREAM_TEST_ZIP);
		File zipFile = ResourceUtil.resourcePathToFile(zipURL);
		InputStream stream;

		// Open entry should return null when it can't find the specified file
		assertNull(ZipUtil.openEntry(zipFile, Path.fromOSString("test.haml")));

		stream = ZipUtil.openEntry(zipFile, Path.fromOSString("streamtest"));

		String text = IOUtil.read(stream);
		assertEquals("this is a test\n\n", text);
	}

}
