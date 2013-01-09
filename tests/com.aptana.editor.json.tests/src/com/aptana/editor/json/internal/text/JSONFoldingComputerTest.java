/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.json.internal.text;

import java.util.Collection;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.editor.json.JSONPlugin;
import com.aptana.editor.json.preferences.IPreferenceConstants;
import com.aptana.json.core.parsing.JSONParser;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ParseState;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;

@SuppressWarnings("nls")
public class JSONFoldingComputerTest extends TestCase
{

	private IFoldingComputer folder;

	@Override
	protected void tearDown() throws Exception
	{
		folder = null;
		super.tearDown();
	}

	public void testObjectFolding() throws Exception
	{
		String src = "{\n" + "    \"description\": \"event object\", \n" + "    \"name\": \"event\", \n"
				+ "    \"type\": \"object\"\n" + "}";
		Map<ProjectionAnnotation, Position> annotations = emitFoldingRegions(false, src);
		Collection<Position> positions = annotations.values();
		assertEquals(1, positions.size());
		assertTrue(positions.contains(new Position(0, src.length())));
	}

	protected Map<ProjectionAnnotation, Position> emitFoldingRegions(boolean initialReconcile, String src)
			throws BadLocationException
	{
		IParseState parseState = new ParseState(src);
		IParseRootNode ast;
		try
		{
			ast = parse(parseState);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		folder = new JSONFoldingComputer(null, new Document(src));
		return folder.emitFoldingRegions(initialReconcile, new NullProgressMonitor(), ast);
	}

	public void testArrayFolding() throws Exception
	{
		String src = "{\n" + //
				"    \"description\": [\n" + //
				"        \"event object\",\n" + //
				"        \"name\",\n" + //
				"        \"event\"\n" + //
				"    ]\n" + //
				"}"; //
		folder = new JSONFoldingComputer(null, new Document(src))
		{
			protected IParseNode getAST()
			{
				IParseState parseState = new ParseState(getDocument().get());
				try
				{
					return parse(parseState);
				}
				catch (Exception e)
				{
					fail(e.getMessage());
				}
				return null;
			};
		};
		Map<ProjectionAnnotation, Position> annotations = emitFoldingRegions(false, src);
		Collection<Position> positions = annotations.values();
		assertEquals(2, positions.size());
		assertTrue(positions.contains(new Position(0, src.length())));
		assertTrue(positions.contains(new Position(21, 64)));
	}

	public void testObjectInitiallyFolded() throws Exception
	{
		String src = "{\n" + "    \"description\": \"event object\", \n" + "    \"name\": \"event\", \n"
				+ "    \"type\": \"object\"\n" + "}";
		folder = new JSONFoldingComputer(null, new Document(src))
		{
			protected IParseNode getAST()
			{
				IParseState parseState = new ParseState(getDocument().get());
				try
				{
					return parse(parseState);
				}
				catch (Exception e)
				{
					fail(e.getMessage());
				}
				return null;
			};
		};

		// Turn on initially folding objects
		EclipseUtil.instanceScope().getNode(JSONPlugin.PLUGIN_ID)
				.putBoolean(IPreferenceConstants.INITIALLY_FOLD_OBJECTS, true);

		Map<ProjectionAnnotation, Position> annotations = emitFoldingRegions(true, src);
		assertTrue(annotations.keySet().iterator().next().isCollapsed());

		// After initial reconcile, don't mark any collapsed
		annotations = emitFoldingRegions(false, src);
		assertFalse(annotations.keySet().iterator().next().isCollapsed());
	}

	public void testArrayInitiallyFolded() throws Exception
	{
		String src = "{\n" + //
				"    \"description\": [\n" + //
				"        \"event object\",\n" + //
				"        \"name\",\n" + //
				"        \"event\"\n" + //
				"    ]\n" + //
				"}"; //
		folder = new JSONFoldingComputer(null, new Document(src))
		{
			protected IParseNode getAST()
			{
				IParseState parseState = new ParseState(getDocument().get());
				try
				{
					return parse(parseState);
				}
				catch (Exception e)
				{
					fail(e.getMessage());
				}
				return null;
			};
		};

		// Turn on initially folding arrays
		EclipseUtil.instanceScope().getNode(JSONPlugin.PLUGIN_ID)
				.putBoolean(IPreferenceConstants.INITIALLY_FOLD_ARRAYS, true);

		Map<ProjectionAnnotation, Position> annotations = emitFoldingRegions(true, src);
		ProjectionAnnotation annotation = getByPosition(annotations, new Position(21, 64));
		assertTrue(annotation.isCollapsed());

		// After initial reconcile, don't mark any collapsed
		annotations = emitFoldingRegions(false, src);
		annotation = getByPosition(annotations, new Position(21, 64));
		assertFalse(annotation.isCollapsed());
	}

	private ProjectionAnnotation getByPosition(Map<ProjectionAnnotation, Position> annotations, Position position)
	{
		for (Map.Entry<ProjectionAnnotation, Position> entry : annotations.entrySet())
		{
			if (entry.getValue().equals(position))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	private IParseRootNode parse(IParseState parseState) throws Exception
	{
		return new JSONParser().parse(parseState).getRootNode();
	}
}
