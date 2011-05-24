/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.css.formatter;


import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.nodes.AbstractFormatterNodeBuilder;
import com.aptana.formatter.nodes.FormatterBlockWithBeginEndNode;
import com.aptana.formatter.nodes.FormatterBlockWithBeginNode;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseRootNode;
import com.aptana.editor.css.formatter.nodes.FormatterCSSBlockNode;
import com.aptana.editor.css.formatter.nodes.FormatterCSSDeclarationNode;
import com.aptana.editor.css.formatter.nodes.FormatterCSSRootNode;
import com.aptana.editor.css.formatter.nodes.FormatterCSSSelectorNode;
import com.aptana.editor.css.parsing.ast.*;

/**
 * CSS formatter node builder.<br>
 * This builder generates the formatter nodes that will then be processed by the {@link CSSFormatterNodeRewriter} to
 * produce the output for the code formatting process.
 */
public class CSSFormatterNodeBuilder extends AbstractFormatterNodeBuilder
{

	private FormatterDocument document;

	/**
	 * @param parseResult
	 * @param document
	 * @return
	 */
	public IFormatterContainerNode build(IParseNode parseResult, FormatterDocument document)
	{
		this.document = document;
		final IFormatterContainerNode rootNode = new FormatterCSSRootNode(document);
		start(rootNode);
		IParseNode[] children = parseResult.getChildren();
		addNodes(children);
		checkedPop(rootNode, document.getLength());
		return rootNode;
	}

	/**
	 * @param children
	 * @param rootNode
	 */
	private void addNodes(IParseNode[] children)
	{
		if (children == null || children.length == 0)
		{
			return;
		}
		for (IParseNode child : children)
		{
			addNode(child);
		}
	}

	/**
	 * @param node
	 * @param rootNode
	 */
	private void addNode(IParseNode node)
	{
		CSSNode cssNode = (CSSNode) node;

		if (cssNode.getNodeType() == CSSNodeTypes.RULE)
		{
			pushFormatterRuleNode((CSSRuleNode) cssNode);
		}
		else if (cssNode.getNodeType() == CSSNodeTypes.PAGE)
		{
			pushFormatterPageNode((CSSPageNode) cssNode);
		}
		else if (cssNode.getNodeType() == CSSNodeTypes.FONTFACE)
		{
			pushFormatterFontFaceNode((CSSFontFaceNode) cssNode);
		}
		else if (cssNode.getNodeType() == CSSNodeTypes.MEDIA)
		{
			pushFormatterMediaNode((CSSMediaNode) cssNode);
		}

	}

	/**
	 * Accepts a CSSMediaNode and breaks down the node into different formatter nodes which should represent it while
	 * rewriting the doc.<br>
	 * The statements of the media node will also be recursively added as formatter nodes.
	 * 
	 * @param pageNode
	 */
	private void pushFormatterMediaNode(CSSMediaNode mediaNode)
	{

		CSSTextNode[] medias = mediaNode.getMedias();

		int blockStartOffset = getBlockStartOffset(medias[medias.length - 1].getEndingOffset() + 1, document);

		for (int i = 0; i < medias.length; i++)
		{
			CSSTextNode mediaSelectorNode = medias[i];

			// For media nodes that are just syntax, we skip it
			if (mediaSelectorNode.getText().length() == 1 && mediaSelectorNode.getText().matches("[^a-zA-Z]")) //$NON-NLS-1$
			{
				continue;
			}

			FormatterBlockWithBeginNode formatterSelectorNode = new FormatterCSSSelectorNode(document, false);
			formatterSelectorNode.setBegin(createTextNode(document,
					getBeginWithoutWhiteSpaces(mediaSelectorNode.getStartingOffset(), document),
					getSelectorNodeEnd(mediaSelectorNode.getEndingOffset() + 1, document) + 1));
			push(formatterSelectorNode);

			checkedPop(formatterSelectorNode, -1);
		}

		FormatterBlockWithBeginEndNode formatterBlockNode = new FormatterCSSBlockNode(document, false);
		formatterBlockNode.setBegin(createTextNode(document, blockStartOffset, blockStartOffset + 1));
		formatterBlockNode
				.setEnd(createTextNode(document, mediaNode.getEndingOffset(), mediaNode.getEndingOffset() + 1));
		push(formatterBlockNode);

		// Recursively add this node's children
		addNodes(mediaNode.getStatements());

		checkedPop(formatterBlockNode, -1);

	}

	/**
	 * Accepts a CSSFontFaceNode and breaks down the node into different formatter nodes which should represent it while
	 * rewriting the doc.<br>
	 * The CSSFontFaceNode will be broken down into several nodes of type FormatterCSSSelectorNode,
	 * FormatterCSSBlockNode, and FormatterCSSDeclarationNode (if declarations are present).
	 * 
	 * @param pageNode
	 */
	private void pushFormatterFontFaceNode(CSSFontFaceNode faceFontNode)
	{

		CSSDeclarationNode[] declarations = faceFontNode.getDeclarations();
		int blockStartOffset = getBlockStartOffset(faceFontNode.getStartingOffset() + 9, document);

		// create a FormatterCSSSelectorNode for @font-face
		FormatterBlockWithBeginNode formatterSelectorNode = new FormatterCSSSelectorNode(document, true);
		formatterSelectorNode.setBegin(createTextNode(document,
				getBeginWithoutWhiteSpaces(faceFontNode.getStartingOffset(), document),
				getSelectorNodeEnd(faceFontNode.getStartingOffset() + 9, document) + 1));
		push(formatterSelectorNode);
		checkedPop(formatterSelectorNode, -1);

		FormatterBlockWithBeginEndNode formatterBlockNode = new FormatterCSSBlockNode(document, false);
		formatterBlockNode.setBegin(createTextNode(document, blockStartOffset, blockStartOffset + 1));
		formatterBlockNode.setEnd(createTextNode(document, faceFontNode.getEndingOffset(),
				faceFontNode.getEndingOffset() + 1));
		push(formatterBlockNode);

		// Don't create text nodes when there are no declarations, or only white space
		if (declarations != null && declarations.length != 0
				&& getBeginWithoutWhiteSpaces(blockStartOffset + 1, document) < declarations[0].getStartingOffset())
		{
			formatterBlockNode.addChild(createTextNode(document, blockStartOffset + 1,
					declarations[0].getStartingOffset()));

		}

		pushFormatterDeclarationNodes(faceFontNode.getEndingOffset(), declarations, formatterBlockNode);

		checkedPop(formatterBlockNode, -1);

	}

	/**
	 * Accepts a CSSPageNode and breaks down the node into different formatter nodes which should represent it while
	 * rewriting the doc.<br>
	 * The CSSPageNode will be broken down into several nodes of type FormatterCSSSelectorNode, FormatterCSSBlockNode,
	 * and FormatterCSSDeclarationNode (if declarations are present).
	 * 
	 * @param pageNode
	 */
	private void pushFormatterPageNode(CSSPageNode pageNode)
	{

		CSSPageSelectorNode selector = pageNode.getSelector();
		CSSDeclarationNode[] declarations = pageNode.getDeclarations();
		int blockStartOffset = getBlockStartOffset(selector.getEndingOffset() + 1, document);

		FormatterBlockWithBeginNode formatterSelectorNode = new FormatterCSSSelectorNode(document, true);
		formatterSelectorNode.setBegin(createTextNode(document,
				getBeginWithoutWhiteSpaces(pageNode.getStartingOffset(), document),
				getSelectorNodeEnd(pageNode.getStartingOffset() + 5, document) + 1));
		push(formatterSelectorNode);
		checkedPop(formatterSelectorNode, -1);

		formatterSelectorNode = new FormatterCSSSelectorNode(document, false);
		// we do startingOffset - 1 to account for the ':'
		formatterSelectorNode.setBegin(createTextNode(document,
				getBeginWithoutWhiteSpaces(selector.getStartingOffset() - 1, document),
				getSelectorNodeEnd(selector.getEndingOffset() + 1, document) + 1));

		push(formatterSelectorNode);
		checkedPop(formatterSelectorNode, -1);

		FormatterBlockWithBeginEndNode formatterBlockNode = new FormatterCSSBlockNode(document, false);
		formatterBlockNode.setBegin(createTextNode(document, blockStartOffset, blockStartOffset + 1));
		formatterBlockNode.setEnd(createTextNode(document, pageNode.getEndingOffset(), pageNode.getEndingOffset() + 1));
		push(formatterBlockNode);

		// Don't create text nodes when there are no declarations, or only white space
		if (declarations != null && declarations.length != 0
				&& getBeginWithoutWhiteSpaces(blockStartOffset + 1, document) < declarations[0].getStartingOffset())
		{
			formatterBlockNode.addChild(createTextNode(document, blockStartOffset + 1,
					declarations[0].getStartingOffset()));

		}

		pushFormatterDeclarationNodes(pageNode.getEndingOffset(), declarations, formatterBlockNode);

		checkedPop(formatterBlockNode, -1);

	}

	/**
	 * Accepts a CSSRuleNode and breaks down the node into different formatter nodes which should represent it while
	 * rewriting the doc.<br>
	 * The CSSRuleNode will be broken down into several nodes of type FormatterCSSSelectorNode, FormatterCSSBlockNode,
	 * and FormatterCSSDeclarationNode (if declarations are present).
	 * 
	 * @param ruleNode
	 */
	private void pushFormatterRuleNode(CSSRuleNode ruleNode)
	{

		CSSSelectorNode[] selectors = ruleNode.getSelectors();
		CSSDeclarationNode[] declarations = ruleNode.getDeclarations();
		int blockStartOffset = getBlockStartOffset(selectors[selectors.length - 1].getEndingOffset() + 1, document);

		pushFormatterSelectorNodes(selectors);

		// Check to see whether the rule node is a declaration of a media node
		boolean isDeclaration = (ruleNode.getParent() == null) || !(ruleNode.getParent() instanceof ParseRootNode);

		FormatterBlockWithBeginEndNode formatterBlockNode = new FormatterCSSBlockNode(document, isDeclaration);
		formatterBlockNode.setBegin(createTextNode(document, blockStartOffset, blockStartOffset + 1));
		formatterBlockNode.setEnd(createTextNode(document, ruleNode.getEndingOffset(), ruleNode.getEndingOffset() + 1));
		push(formatterBlockNode);

		// Don't create text nodes when there are no declarations, or only white space
		if (declarations != null && declarations.length != 0
				&& getBeginWithoutWhiteSpaces(blockStartOffset + 1, document) < declarations[0].getStartingOffset())
		{
			formatterBlockNode.addChild(createTextNode(document, blockStartOffset + 1,
					declarations[0].getStartingOffset()));

		}

		pushFormatterDeclarationNodes(ruleNode.getEndingOffset(), declarations, formatterBlockNode);

		checkedPop(formatterBlockNode, -1);

	}

	private void pushFormatterDeclarationNodes(int parentEndOffset, CSSDeclarationNode[] declarations,
			FormatterBlockWithBeginEndNode formatterBlockNode)
	{
		for (int i = 0; i < declarations.length; ++i)
		{

			CSSDeclarationNode declarationNode = declarations[i];
			FormatterBlockWithBeginNode formatterDeclarationNode = new FormatterCSSDeclarationNode(document);

			formatterDeclarationNode.setBegin(createTextNode(document, declarationNode.getStartingOffset(),
					declarationNode.getEndingOffset() + 1));
			push(formatterDeclarationNode);

			checkedPop(formatterDeclarationNode, -1);

			if (i + 1 < declarations.length)
			{
				if (getBeginWithoutWhiteSpaces(declarations[i].getEndingOffset() + 1, document) < declarations[i + 1]
						.getStartingOffset())
				{
					formatterBlockNode.addChild(createTextNode(document, declarations[i].getEndingOffset() + 1,
							declarations[i + 1].getStartingOffset()));
				}
			}
			else if (getBeginWithoutWhiteSpaces(declarations[i].getEndingOffset() + 1, document) < parentEndOffset)
			{
				formatterBlockNode.addChild(createTextNode(document, declarations[i].getEndingOffset() + 1,
						parentEndOffset));
			}

		}
	}

	private void pushFormatterSelectorNodes(CSSSelectorNode[] selectors)
	{
		for (int i = 0; i < selectors.length; i++)
		{
			CSSSelectorNode selectorNode = selectors[i];
			FormatterBlockWithBeginNode formatterSelectorNode = new FormatterCSSSelectorNode(document, i == 0);
			formatterSelectorNode.setBegin(createTextNode(document,
					getBeginWithoutWhiteSpaces(selectorNode.getStartingOffset(), document),
					getSelectorNodeEnd(selectorNode.getEndingOffset() + 1, document) + 1));
			push(formatterSelectorNode);

			checkedPop(formatterSelectorNode, -1);
		}
	}

	private int getBlockStartOffset(int offset, FormatterDocument document)
	{
		int length = document.getLength();
		while (offset < length)
		{
			if (document.charAt(offset) == '{')
			{
				break;
			}
			offset++;
		}
		return offset;
	}

	/**
	 * @param i
	 * @param document2
	 * @return
	 */
	private int getBeginWithoutWhiteSpaces(int offset, FormatterDocument document)
	{
		int length = document.getLength();
		while (offset < length)
		{
			if (!Character.isWhitespace(document.charAt(offset)) && (document.charAt(offset) != '\n'))
			{
				break;
			}
			offset++;
		}
		return offset;
	}

	/**
	 * @param startingOffset
	 * @param document2
	 * @return
	 */
	private int getSelectorNodeEnd(int offset, FormatterDocument document)
	{
		int original = offset;

		while (Character.isWhitespace(document.charAt(offset)))
		{
			offset++;
		}

		// We also account for ':' (mainly for media nodes)
		if (document.charAt(offset) == ',' || document.charAt(offset) == ':')
		{
			return offset;
		}

		offset = original;
		while (offset > 0)
		{
			if (!Character.isWhitespace(document.charAt(offset)) && (document.charAt(offset) != '\n')
					&& (document.charAt(offset) != '{'))
			{
				break;
			}
			offset--;
		}
		return offset;
	}

}
