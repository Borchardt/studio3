package com.aptana.parsing.ast;

import com.aptana.parsing.lexer.ILexeme;

public interface IParseNode extends ILexeme, ILanguageNode, Iterable<IParseNode>
{
	/**
	 * addChild
	 * 
	 * @param child
	 */
	public void addChild(IParseNode child);

	/**
	 * getAttributes
	 * 
	 * @return
	 */
	public IParseNodeAttribute[] getAttributes();

	/**
	 * getChild
	 * 
	 * @param index
	 * @return
	 */
	public IParseNode getChild(int index);

	/**
	 * getChildCount
	 * 
	 * @return
	 */
	public int getChildCount();

	/**
	 * getChildren
	 * 
	 * @return
	 */
	public IParseNode[] getChildren();

	/**
	 * getElementName
	 * 
	 * @return
	 */
	public String getElementName();

	/**
	 * getFirstChild
	 * 
	 * @return
	 */
	public IParseNode getFirstChild();

	/**
	 * getIndex
	 * 
	 * @return
	 */
	public int getIndex();

	/**
	 * getLanguage
	 */
	public String getLanguage();

	/**
	 * getLastChild
	 * 
	 * @return
	 */
	public IParseNode getLastChild();

	/**
	 * getNameNode
	 * 
	 * @return
	 */
	public INameNode getNameNode();

	/**
	 * getNextNode
	 * 
	 * @return
	 */
	public IParseNode getNextNode();

	/**
	 * getNextSibling
	 * 
	 * @return
	 */
	public IParseNode getNextSibling();

	/**
	 * getNodeAt
	 * 
	 * @param offset
	 * @return
	 */
	public IParseNode getNodeAtOffset(int offset);

	/**
	 * getParent
	 * 
	 * @return
	 */
	public IParseNode getParent();

	/**
	 * getPreviousNode
	 * 
	 * @return
	 */
	public IParseNode getPreviousNode();

	/**
	 * getPreviousSibling
	 * 
	 * @return
	 */
	public IParseNode getPreviousSibling();

	/**
	 * getRootNode
	 * 
	 * @return
	 */
	public IParseNode getRootNode();
	
	/**
	 * getNodeType
	 * 
	 * @return
	 */
	public short getNodeType();

	/**
	 * hasChildren
	 * 
	 * @return
	 */
	public boolean hasChildren();
	
	/**
	 * Set a child at a given index, replacing any existing child.
	 * 
	 * @param index
	 * @param child
	 * @throws IndexOutOfBoundsException
	 */
	void replaceChild(int index, IParseNode child) throws IndexOutOfBoundsException;
}
