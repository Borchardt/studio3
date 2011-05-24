/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.html.contentassist;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.html.HTMLMetadataLoader;
import com.aptana.editor.html.HTMLTestUtil;
import com.aptana.editor.html.contentassist.HTMLContentAssistProcessor.LocationType;
import com.aptana.editor.html.parsing.lexer.HTMLTokenType;

public class HTMLContentAssistProcessorTest extends LocationTestCase
{

	private static final int ELEMENT_PROPOSALS_COUNT = 132;
	private static final int DOCTYPE_PROPOSALS_COUNT = 11;
	private static final int CLOSE_TAG_PROPOSALS_COUNT = 119;
	private static final int ENTITY_PROPOSAL_COUNT = 252;

	private HTMLContentAssistProcessor fProcessor;
	private IDocument fDocument;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		HTMLMetadataLoader loader = new HTMLMetadataLoader();
		loader.schedule();
		loader.join();

		fProcessor = new HTMLContentAssistProcessor(null);
	}

	@Override
	protected void tearDown() throws Exception
	{
		fProcessor = null;
		fDocument = null;
		super.tearDown();
	}

	public void testLinkProposal()
	{
		assertCompletionCorrect("<|", '\t', ELEMENT_PROPOSALS_COUNT, "a", "<a></a>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testDOCTYPEProposal()
	{
		Point p = new Point(10, 0);
		assertCompletionCorrect("<!|", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE >", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<!D|", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE >", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<!D| html>", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE html>", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<|>", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE >", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<!|>", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE >", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<!D|OCTYP >", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE >", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<!D|OCTYPE >", '\t', ELEMENT_PROPOSALS_COUNT, "!DOCTYPE", "<!DOCTYPE >", p); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testDOCTYPEValueReplacement()
	{
		assertCompletionCorrect("<!DOCTYPE |html>", '\t', DOCTYPE_PROPOSALS_COUNT, "HTML 5", "<!DOCTYPE HTML>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect(
				"<!DOCTYPE |html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n	\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">", //$NON-NLS-1$
				'\t', DOCTYPE_PROPOSALS_COUNT, "HTML 5", "<!DOCTYPE HTML>", null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testCloseTagProposal()
	{
		String document = "<a>Test <b>Item</b>"; //$NON-NLS-1$
		IDocument fDocument = createDocument(document, false);

		char trigger = '\t';
		ITextViewer viewer = createTextViewer(fDocument);

		// Should be no unclosed tags at this point
		fProcessor.doComputeCompletionProposals(viewer, 0, trigger, false);
		assertEquals(0, fProcessor.getUnclosedTagNames(0).size());

		fProcessor.doComputeCompletionProposals(viewer, 1, trigger, false);
		assertEquals(0, fProcessor.getUnclosedTagNames(1).size());

		fProcessor.doComputeCompletionProposals(viewer, 2, trigger, false);
		assertEquals(0, fProcessor.getUnclosedTagNames(2).size());

		fProcessor.doComputeCompletionProposals(viewer, 3, trigger, false);
		assertEquals(1, fProcessor.getUnclosedTagNames(3).size());

		// show unclosed tag once we get past the '>'
		fProcessor.doComputeCompletionProposals(viewer, 4, trigger, false);
		assertEquals(1, fProcessor.getUnclosedTagNames(4).size());
	}

	public void testABBRProposal()
	{
		assertCompletionCorrect("<a|>", '\t', ELEMENT_PROPOSALS_COUNT, "abbr", "<abbr></abbr>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<A|>", '\t', ELEMENT_PROPOSALS_COUNT, "abbr", "<abbr></abbr>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testElementWhichIsClosedProposal()
	{
		assertCompletionCorrect("<|></a>", '\t', ELEMENT_PROPOSALS_COUNT, "a", "<a></a>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testElementWhichIsClosedProposal2()
	{
		assertCompletionCorrect("<|></a>", '\t', ELEMENT_PROPOSALS_COUNT, "abbr", "<abbr></abbr></a>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testElementWhichIsClosedProposal3()
	{
		assertCompletionCorrect("<|</a>", '\t', ELEMENT_PROPOSALS_COUNT, "abbr", "<abbr></abbr></a>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testElementWhichIsClosedProposal4()
	{
		assertCompletionCorrect("<b><a><|</b>", '\t', ELEMENT_PROPOSALS_COUNT + 1, "/a", "<b><a></a></b>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testElementWhichIsClosedProposal5()
	{
		assertCompletionCorrect("<b><a><|></b>", '\t', ELEMENT_PROPOSALS_COUNT + 1, "/a", "<b><a></a></b>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testElementWhichIsClosedProposal6()
	{
		assertCompletionCorrect("<b><a></|</b>", '\t', ELEMENT_PROPOSALS_COUNT + 1, "/a", "<b><a></a></b>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSuggestOnlyUnclosedTagForCloseTagWithNoElementName()
	{
		assertCompletionCorrect("<b><a></|></b>", '\t', 1, "/a", "<b><a></a></b>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSuggestOnlyUnclosedTagForCloseTagWithElementName()
	{
		assertCompletionCorrect("<b><a></a|</b>", '\t', 1, "/a", "<b><a></a></b>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testSuggestAllPossibleCloseTagsOnExistingCloseTagRegardlessOfPrefix()
	{
		assertCompletionCorrect("<b><a></a|></b>", '\t', CLOSE_TAG_PROPOSALS_COUNT, "/a", "<b><a></a></b>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testCloseTagWithNoUnclosedTagsProposal()
	{
		assertCompletionCorrect("</|>", '\t', CLOSE_TAG_PROPOSALS_COUNT, "/ul", "</ul>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testNoSuggestionsInTextAreaBetweenTags()
	{
		assertCompletionCorrect("<p>|</p>", '\t', 0, null, "<p></p>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testNoSuggestionsInTextAreaWithWhitespaceBetweenTags()
	{
		assertCompletionCorrect("<p>\n  |\n</p>", '\t', 0, null, "<p>\n  \n</p>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testOnlySuggestEntityIfPrecededByAmpersand()
	{
		assertCompletionCorrect("<p>\n  &|\n</p>", '\t', ENTITY_PROPOSAL_COUNT, "&amp;", "<p>\n  &amp;\n</p>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testEntitySuggestionWithNoSurroundingWhitespace()
	{
		assertCompletionCorrect("<div>&|</div>", '\t', ENTITY_PROPOSAL_COUNT, "&amp;", "<div>&amp;</div>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testExistingEntityGetsFullyReplaced()
	{
		assertCompletionCorrect(
				"<body>\n  &a|acute;\n</body>", '\t', ENTITY_PROPOSAL_COUNT, "&acirc;", "<body>\n  &acirc;\n</body>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<div>&a|acute;</div>", '\t', ENTITY_PROPOSAL_COUNT, "&amp;", "<div>&amp;</div>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testIMGProposal()
	{
		assertCompletionCorrect("<|", '\t', ELEMENT_PROPOSALS_COUNT, "img", "<img />", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testHTML5DoctypeProposal()
	{
		assertCompletionCorrect("<!doctype |>", '\t', DOCTYPE_PROPOSALS_COUNT, "HTML 5", "<!doctype HTML>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testHTML401StrictDoctypeProposal()
	{
		assertCompletionCorrect("<!DOCTYPE |", '\t', DOCTYPE_PROPOSALS_COUNT, "HTML 4.01 Strict", //$NON-NLS-1$ //$NON-NLS-2$
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n\"http://www.w3.org/TR/html4/strict.dtd\"", null); //$NON-NLS-1$
	}

	public void testStyleAttributeProposalWithNoPrefix()
	{
		assertCompletionCorrect("<div |></div>", '\t', 64, "style", "<div style=\"\"></div>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testStyleAttributeProposalOnSelfClosingTag()
	{
		assertCompletionCorrect("<br |/>", '\t', 8, "style", "<br style=\"\"/>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testStyleAttributeProposalOnSelfClosingTagWithTrailingSpaceAfterCursor()
	{
		assertCompletionCorrect("<br | />", '\t', 8, "style", "<br style=\"\" />", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testStyleAttributeProposalWithPrefix()
	{
		assertCompletionCorrect("<div sty|></div>", '\t', 64, "style", "<div style=\"\"></div>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testStyleAttributeProposalWithPrefixAndTrailingEquals()
	{
		assertCompletionCorrect("<div sty|=\"\"></div>", '\t', 64, "style", "<div style=\"\"></div>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void testDontOverwriteTagEnd()
	{
		assertCompletionCorrect("<div dir=\"|></div>", '\t', 2, "ltr", "<div dir=\"ltr></div>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertCompletionCorrect("<br dir=\"|/>", '\t', 2, "ltr", "<br dir=\"ltr/>", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@SuppressWarnings("rawtypes")
	public void testStyleAttributeProposalHasExitTabstopAfterQuotes()
	{
		assertCompletionCorrect("<div |>", '\t', 64, "style", "<div style=\"\">", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Now test that we have two tabstops. One inside the quotes, one just after
		LinkedModeModel model = LinkedModeModel.getModel(fDocument, 12);
		assertNotNull(model);
		List list = model.getTabStopSequence();
		assertNotNull(list);
		assertEquals(2, list.size());
		Position pos = (Position) list.get(0);
		assertEquals(12, pos.getOffset());
		assertEquals(0, pos.getLength());

		pos = (Position) list.get(1);
		assertEquals(13, pos.getOffset());
		assertEquals(0, pos.getLength());
	}

	@SuppressWarnings("rawtypes")
	public void testEventProposalHasExitTabstopAfterQuotes()
	{
		assertCompletionCorrect("<div |>", '\t', 64, "onmouseenter", "<div onmouseenter=\"\">", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Now test that we have two tabstops. One inside the quotes, one just after
		LinkedModeModel model = LinkedModeModel.getModel(fDocument, 19);
		assertNotNull(model);
		List list = model.getTabStopSequence();
		assertNotNull(list);
		assertEquals(2, list.size());
		Position pos = (Position) list.get(0);
		assertEquals(19, pos.getOffset());
		assertEquals(0, pos.getLength());

		pos = (Position) list.get(1);
		assertEquals(20, pos.getOffset());
		assertEquals(0, pos.getLength());
	}

	// https://aptana.lighthouseapp.com/projects/35272/tickets/1719-html-code-completion-for-tag-attributes-goes-wrong
	public void testIMGSrcContentAssistDoesntAutoInsertCloseTag()
	{
		String document = "<div><img src='|' />";
		int offset = HTMLTestUtil.findCursorOffset(document);
		fDocument = HTMLTestUtil.createDocument(document, true);
		ITextViewer viewer = createTextViewer(fDocument);

		ICompletionProposal[] proposals = fProcessor.doComputeCompletionProposals(viewer, offset, '\t', false);
		assertEquals(0, proposals.length);
	}

	public void testAttributeAfterElementName()
	{
		String document = "<body s|></body>";
		int offset = HTMLTestUtil.findCursorOffset(document);
		fDocument = HTMLTestUtil.createDocument(document, true);
		ITextViewer viewer = createTextViewer(fDocument);

		ICompletionProposal[] proposals = fProcessor.doComputeCompletionProposals(viewer, offset, '\t', false);
		assertTrue(proposals.length > 0);

		boolean foundScroll = false;
		for (ICompletionProposal proposal : proposals)
		{
			if ("scroll".equals(proposal.getDisplayString()))
			{
				foundScroll = true;
				break;
			}
		}
		assertTrue(foundScroll);
	}

	private ICompletionProposal findProposal(String string, ICompletionProposal[] proposals)
	{
		for (ICompletionProposal proposal : proposals)
		{
			if (proposal.getDisplayString().equals(string))
			{
				return proposal;
			}
		}
		return null;
	}

	protected ITextViewer createTextViewer(IDocument fDocument)
	{
		ITextViewer viewer = new TextViewer(new Shell(), SWT.NONE);
		viewer.setDocument(fDocument);
		return viewer;
	}

	protected void assertLocation(String document, LocationType location)
	{
		int offset = HTMLTestUtil.findCursorOffset(document);
		fDocument = HTMLTestUtil.createDocument(document, true);

		LexemeProvider<HTMLTokenType> lexemeProvider = HTMLTestUtil.createLexemeProvider(fDocument, offset);
		LocationType l = fProcessor.getOpenTagLocationType(lexemeProvider, offset);

		assertEquals(location, l);
	}

	protected void assertCompletionCorrect(String document, char trigger, int proposalCount, String proposalToSelect,
			String postCompletion, Point point)
	{
		int offset = HTMLTestUtil.findCursorOffset(document);
		fDocument = HTMLTestUtil.createDocument(document, true);
		ITextViewer viewer = createTextViewer(fDocument);

		ICompletionProposal[] proposals = fProcessor.doComputeCompletionProposals(viewer, offset, trigger, false);
		assertEquals(proposalCount, proposals.length);

		if (proposalToSelect != null)
		{
			ICompletionProposal closeProposal = findProposal(proposalToSelect, proposals);
			assertNotNull("Unable to find proposal you wanted to select: " + proposalToSelect, closeProposal);
			assertTrue("Selected proposal doesn't validate against document",
					((ICompletionProposalExtension2) closeProposal).validate(fDocument, offset, null));
			((ICompletionProposalExtension2) closeProposal).apply(viewer, trigger, SWT.NONE, offset);
		}
		assertEquals(postCompletion, fDocument.get());

		if (point != null)
		{
			Point p = viewer.getSelectedRange();
			assertEquals(point.x, p.x);
			assertEquals(point.y, p.y);
		}
	}
}
