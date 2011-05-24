/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.scope;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ScopeSelectorTests extends TestCase
{
	/**
	 * testParseSimpleName
	 */
	public void testParseSimpleName()
	{
		String scope = "source.ruby";
		ScopeSelector selector = new ScopeSelector(scope);

		// make sure we parsed successfully
		assertNotNull(selector);

		// convert selector back to source and compare
		assertEquals(scope, selector.toString());

		// make sure we have the right selector type
		ISelectorNode root = selector.getRoot();
		assertTrue(root instanceof NameSelector);
	}

	/**
	 * testParseSimpleAndSelector
	 */
	public void testParseSimpleAndSelector()
	{
		String scope = "text.html source.ruby";
		ScopeSelector selector = new ScopeSelector(scope);

		// make sure we parsed successfully
		assertNotNull(selector);

		// convert selector back to source and compare
		assertEquals(scope, selector.toString());

		// make sure we have the right selector type
		ISelectorNode root = selector.getRoot();
		assertTrue(root instanceof AndSelector);

		// check children
		AndSelector andSelector = (AndSelector) root;
		assertTrue(andSelector.getLeftChild() instanceof NameSelector);
		assertTrue(andSelector.getRightChild() instanceof NameSelector);
	}

	/**
	 * testParseSimpleOrSelector
	 */
	public void testParseSimpleOrSelector()
	{
		String scope = "text.html, source.ruby";
		ScopeSelector selector = new ScopeSelector(scope);

		// make sure we parsed successfully
		assertNotNull(selector);

		// convert selector back to source and compare
		assertEquals(scope, selector.toString());

		// make sure we have the right selector type
		ISelectorNode root = selector.getRoot();
		assertTrue(root instanceof OrSelector);

		// check children
		OrSelector orSelector = (OrSelector) root;
		assertTrue(orSelector.getLeftChild() instanceof NameSelector);
		assertTrue(orSelector.getRightChild() instanceof NameSelector);
	}

	/**
	 * testParseMultiAndSelector
	 */
	public void testParseMultiAndSelector()
	{
		String scope = "text.html source.ruby string.ruby";
		ScopeSelector selector = new ScopeSelector(scope);

		// make sure we parsed successfully
		assertNotNull(selector);

		// convert selector back to source and compare
		assertEquals(scope, selector.toString());

		// make sure we have the right selector type
		ISelectorNode root = selector.getRoot();
		assertTrue(root instanceof AndSelector);

		// check children
		AndSelector andSelector = (AndSelector) root;
		assertTrue(andSelector.getLeftChild() instanceof AndSelector);
		assertTrue(andSelector.getRightChild() instanceof NameSelector);
	}

	/**
	 * testParseMultiOrSelector
	 */
	public void testParseMultiOrSelector()
	{
		String scope = "text.html, source.ruby, string.ruby";
		ScopeSelector selector = new ScopeSelector(scope);

		// make sure we parsed successfully
		assertNotNull(selector);

		// convert selector back to source and compare
		assertEquals(scope, selector.toString());

		// make sure we have the right selector type
		ISelectorNode root = selector.getRoot();
		assertTrue(root instanceof OrSelector);

		// check children
		OrSelector orSelector = (OrSelector) root;
		assertTrue(orSelector.getLeftChild() instanceof OrSelector);
		assertTrue(orSelector.getRightChild() instanceof NameSelector);
	}

	/**
	 * testParseMultiMixedSelector
	 */
	public void testParseMultiMixedSelector()
	{
		String scope = "text.html source.ruby, text.erb source.ruby, source.ruby string.ruby";
		ScopeSelector selector = new ScopeSelector(scope);

		// make sure we parsed successfully
		assertNotNull(selector);

		// convert selector back to source and compare
		assertEquals(scope, selector.toString());

		// make sure we have the right selector type
		ISelectorNode root = selector.getRoot();
		assertTrue(root instanceof OrSelector);

		// check children
		OrSelector orSelector = (OrSelector) root;
		assertTrue(orSelector.getLeftChild() instanceof OrSelector);
		assertTrue(orSelector.getRightChild() instanceof AndSelector);
	}

	// Match the element deepest down in the scope e.g. string wins over source.php when the
	// scope is source.php string.quoted.
	//
	// Match most of the deepest element e.g. string.quoted wins over string.
	//
	// Rules 1 and 2 applied again to the scope selector when removing the deepest element
	// (in the case of a tie), e.g. text source string wins over source string.
	//
	// In the case of tab triggers, key equivalents and dropped files (drag commands), a menu is
	// presented for the best matches when these are identical in rank (which would mean the scope
	// selector in that case was identical).
	//
	// For themes and preference items, the winner is undefined when multiple items use the same scope
	// selector, though this is on a per-property basis. So for example if one theme item sets the
	// background to blue for string.quoted and another theme item sets the foreground to white,
	// again for string.quoted, the result would be that the foreground was taken from the latter
	// item and background from the former.
	public void testBestMatch()
	{
		ScopeSelector entity = new ScopeSelector("entity");
		ScopeSelector metaTagEntity = new ScopeSelector("meta.tag entity");
		List<IScopeSelector> selectors = new ArrayList<IScopeSelector>();
		selectors.add(entity);
		selectors.add(metaTagEntity);
		assertEquals(metaTagEntity, ScopeSelector.bestMatch(selectors,
				"text.html.markdown meta.disable-markdown meta.tag.block.any.html entity.name.tag.block.any.html"));
	}

	public void testBestMatchExample()
	{
		ScopeSelector string = new ScopeSelector(
				"string - string.unquoted.old-plist - string.unquoted.heredoc, string.unquoted.heredoc string");
		ScopeSelector metaTag = new ScopeSelector("meta.tag");
		List<IScopeSelector> selectors = new ArrayList<IScopeSelector>();
		selectors.add(string);
		selectors.add(metaTag);
		assertEquals(
				string,
				ScopeSelector
						.bestMatch(
								selectors,
								"text.html.markdown meta.disable-markdown meta.tag.block.any.html meta.attribute-with-value.id.html string.quoted.double.html meta.toc-list.id.html"));
		assertEquals(string, ScopeSelector.bestMatch(selectors,
				"text.html.markdown meta.disable-markdown meta.tag.block.any.html string.quoted.double.html"));
	}

	public void testBestMatchDeepestElementWins()
	{
		ScopeSelector string = new ScopeSelector("string");
		ScopeSelector source = new ScopeSelector("source.php");
		List<IScopeSelector> selectors = new ArrayList<IScopeSelector>();
		selectors.add(string);
		selectors.add(source);
		assertEquals(string, ScopeSelector.bestMatch(selectors, "source.php string.quoted"));
	}

	public void testBestMatchLengthOfDeepestElementWins()
	{
		ScopeSelector string = new ScopeSelector("string");
		ScopeSelector quoted = new ScopeSelector("string.quoted");
		List<IScopeSelector> selectors = new ArrayList<IScopeSelector>();
		selectors.add(string);
		selectors.add(quoted);
		assertEquals(quoted, ScopeSelector.bestMatch(selectors, "source.php string.quoted"));
	}

	public void test2357()
	{
		ScopeSelector jsStorage = new ScopeSelector(
				"source.js storage - storage.type.function - source.php, source.js constant - source.php, source.js keyword - source.php, source.js variable.language, source.js meta.brace, source.js punctuation.definition.parameters.begin, source.js punctuation.definition.parameters.end");
		ScopeSelector constantNumeric = new ScopeSelector("constant.numeric");
		ScopeSelector jsString = new ScopeSelector("source.js string - source.php, source.js keyword.operator");

		assertTrue(new ScopeSelector("source.css string").matches("source.css string.quoted.single.css"));
		assertTrue(new ScopeSelector("string").matches("source.css string.quoted.single.css"));
		assertTrue(constantNumeric.matches("source.js constant.numeric.js"));
		assertTrue(jsStorage.matches("source.js constant.numeric.js"));
		assertTrue(jsString.matches("source.js string.quoted.single.js"));

		List<IScopeSelector> selectors = new ArrayList<IScopeSelector>();
		selectors.add(jsStorage);
		selectors.add(constantNumeric);
		selectors.add(jsString);
		assertEquals(constantNumeric, ScopeSelector.bestMatch(selectors, "source.js constant.numeric.js"));
		assertEquals(jsString, ScopeSelector.bestMatch(selectors, "source.js string.quoted.single.js"));
	}

	/**
	 * Test that ensures we move up the scope chain when deepest level match is of same length.
	 */
	public void testBestMatchAdvanced()
	{
		ScopeSelector metaTagBlockEntity = new ScopeSelector("meta.tag.block entity");
		ScopeSelector metaTagEntity = new ScopeSelector("meta.tag entity");
		// Order is important in inserting into list here!
		List<IScopeSelector> selectors = new ArrayList<IScopeSelector>();
		selectors.add(metaTagEntity);
		selectors.add(metaTagBlockEntity);
		assertEquals(metaTagBlockEntity, ScopeSelector.bestMatch(selectors,
				"text.html.markdown meta.disable-markdown meta.tag.block.any.html entity.name.tag.block.any.html"));
	}

}
