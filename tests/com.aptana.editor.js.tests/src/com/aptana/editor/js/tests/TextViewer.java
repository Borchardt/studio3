package com.aptana.editor.js.tests;

import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

@SuppressWarnings("deprecation")
public class TextViewer implements ITextViewer
{
	private IDocument _document;

	public TextViewer(IDocument document)
	{
		this._document = document;
	}

	public void activatePlugins()
	{
	}

	public void addTextInputListener(ITextInputListener listener)
	{
	}

	public void addTextListener(ITextListener listener)
	{
	}

	public void addViewportListener(IViewportListener listener)
	{
	}

	public void changeTextPresentation(TextPresentation presentation, boolean controlRedraw)
	{
	}

	public int getBottomIndex()
	{
		return 0;
	}

	public int getBottomIndexEndOffset()
	{
		return 0;
	}

	public IDocument getDocument()
	{
		return this._document;
	}

	public IFindReplaceTarget getFindReplaceTarget()
	{
		return null;
	}

	public Point getSelectedRange()
	{
		return null;
	}

	public ISelectionProvider getSelectionProvider()
	{
		return null;
	}

	public ITextOperationTarget getTextOperationTarget()
	{
		return null;
	}

	public StyledText getTextWidget()
	{
		return null;
	}

	public int getTopIndex()
	{
		return 0;
	}

	public int getTopIndexStartOffset()
	{
		return 0;
	}

	public int getTopInset()
	{
		return 0;
	}

	public IRegion getVisibleRegion()
	{
		return null;
	}

	public void invalidateTextPresentation()
	{
	}

	public boolean isEditable()
	{
		return false;
	}

	public boolean overlapsWithVisibleRegion(int offset, int length)
	{
		return false;
	}

	public void removeTextInputListener(ITextInputListener listener)
	{
	}

	public void removeTextListener(ITextListener listener)
	{
	}

	public void removeViewportListener(IViewportListener listener)
	{
	}

	public void resetPlugins()
	{
	}

	public void resetVisibleRegion()
	{
	}

	public void revealRange(int offset, int length)
	{
	}

	public void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType)
	{
	}

	public void setDefaultPrefixes(String[] defaultPrefixes, String contentType)
	{
	}

	public void setDocument(IDocument document)
	{
	}

	public void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength)
	{
	}

	public void setEditable(boolean editable)
	{
	}

	public void setEventConsumer(IEventConsumer consumer)
	{
	}

	public void setIndentPrefixes(String[] indentPrefixes, String contentType)
	{
	}

	public void setSelectedRange(int offset, int length)
	{
	}

	public void setTextColor(Color color)
	{
	}

	public void setTextColor(Color color, int offset, int length, boolean controlRedraw)
	{
	}

	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy, String contentType)
	{
	}

	public void setTextHover(ITextHover textViewerHover, String contentType)
	{
	}

	public void setTopIndex(int index)
	{
	}

	public void setUndoManager(IUndoManager undoManager)
	{
	}

	public void setVisibleRegion(int offset, int length)
	{
	}
}
