/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.aptana.editor.common.contentassist.ContentAssistant;
import com.aptana.editor.common.hover.CommonAnnotationHover;
import com.aptana.editor.common.hover.ThemedInformationControl;
import com.aptana.editor.common.internal.formatter.CommonMultiPassContentFormatter;
import com.aptana.editor.common.internal.hover.TextHoverDescriptor;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.CommonDoubleClickStrategy;
import com.aptana.editor.common.text.RubyRegexpAutoIndentStrategy;
import com.aptana.editor.common.text.reconciler.CommonCompositeReconcilingStrategy;
import com.aptana.editor.common.text.reconciler.CommonReconciler;
import com.aptana.formatter.ScriptFormatterManager;
import com.aptana.theme.IThemeManager;
import com.aptana.theme.Theme;
import com.aptana.theme.ThemePlugin;

@SuppressWarnings("restriction")
public abstract class CommonSourceViewerConfiguration extends TextSourceViewerConfiguration implements ITopContentTypesProvider
{
	private AbstractThemeableEditor fTextEditor;
	private CommonDoubleClickStrategy fDoubleClickStrategy;
	private IPreferenceChangeListener fThemeChangeListener;
	private IPreferenceChangeListener fAutoActivationListener;
	private IReconcilingStrategy fReconcilingStrategy;
	protected static final String CONTENTTYPE_HTML_PREFIX = "com.aptana.contenttype.html"; //$NON-NLS-1$
	public static final int DEFAULT_CONTENT_ASSIST_DELAY = 0;
	public static final int LONG_CONTENT_ASSIST_DELAY = 1000;

	/**
	 * CommonSourceViewerConfiguration
	 * 
	 * @param preferenceStore
	 * @param editor
	 */
	public CommonSourceViewerConfiguration(IPreferenceStore preferenceStore, AbstractThemeableEditor editor)
	{
		super(preferenceStore);

		fTextEditor = editor;
	}

	/**
	 * dispose
	 */
	public void dispose()
	{
		fTextEditor = null;
		fDoubleClickStrategy = null;
		if (fAutoActivationListener != null)
		{
			new InstanceScope().getNode(CommonEditorPlugin.PLUGIN_ID).removePreferenceChangeListener(
					fAutoActivationListener);
			fAutoActivationListener = null;
		}
		if (fThemeChangeListener != null)
		{
			new InstanceScope().getNode(ThemePlugin.PLUGIN_ID).removePreferenceChangeListener(fThemeChangeListener);
			fThemeChangeListener = null;
		}
	}

	/**
	 * getAbstractThemeableEditor
	 * 
	 * @deprecated Use {@link #getEditor()}
	 * @return
	 */
	protected AbstractThemeableEditor getAbstractThemeableEditor()
	{
		return getEditor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.
	 * ISourceViewer)
	 */
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
	{
		return new CommonAnnotationHover(false)
		{
			protected boolean isIncluded(Annotation annotation)
			{
				return isShowInVerticalRuler(annotation);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.
	 * ISourceViewer, java.lang.String)
	 */
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
	{
		return new IAutoEditStrategy[] { new RubyRegexpAutoIndentStrategy(contentType, this, sourceViewer,
				CommonEditorPlugin.getDefault().getPreferenceStore()) };
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.
	 * ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{
		final ContentAssistant assistant = new ContentAssistant();
		assistant.setProposalSelectorBackground(getThemeBackground());
		assistant.setProposalSelectorForeground(getThemeForeground());
		assistant.setProposalSelectorSelectionColor(getThemeSelection());

		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		String[] contentTypes = getConfiguredContentTypes(sourceViewer);

		for (String type : contentTypes)
		{
			IContentAssistProcessor processor = getContentAssistProcessor(sourceViewer, type);

			if (processor != null)
			{
				assistant.setContentAssistProcessor(processor, type);
			}
		}

		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		if (fPreferenceStore != null)
		{
			setAutoActivationOptions(assistant);
			// Auto-insert single proposals
			boolean autoInsert = fPreferenceStore.getBoolean(IPreferenceConstants.CONTENT_ASSIST_AUTO_INSERT);
			assistant.enableAutoInsert(autoInsert);
		}

		fAutoActivationListener = new IPreferenceChangeListener()
		{
			public void preferenceChange(PreferenceChangeEvent event)
			{
				setAutoActivationOptions(assistant);
			}
		};
		new InstanceScope().getNode(CommonEditorPlugin.PLUGIN_ID).addPreferenceChangeListener(fAutoActivationListener);

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant.setContextInformationPopupBackground(getThemeBackground());
		assistant.setContextInformationPopupForeground(getThemeForeground());

		fThemeChangeListener = new IPreferenceChangeListener()
		{

			public void preferenceChange(PreferenceChangeEvent event)
			{
				if (event.getKey().equals(IThemeManager.THEME_CHANGED))
				{
					assistant.setProposalSelectorBackground(getThemeBackground());
					assistant.setProposalSelectorForeground(getThemeForeground());
					assistant.setProposalSelectorSelectionColor(getThemeSelection());
				}
			}
		};
		new InstanceScope().getNode(ThemePlugin.PLUGIN_ID).addPreferenceChangeListener(fThemeChangeListener);

		return assistant;
	}

	private void setAutoActivationOptions(final ContentAssistant assistant)
	{
		int delay = fPreferenceStore.getInt(IPreferenceConstants.CONTENT_ASSIST_DELAY);
		if (delay >= 0)
		{
			assistant.enableAutoActivation(true);
			assistant.setAutoActivationDelay(delay);
		}
		else
		{
			assistant.enableAutoActivation(false);
		}
	}

	/**
	 * Returns the content assist processor that will be used for content assist in the given source viewer and for the
	 * given partition type.
	 * 
	 * @param sourceViewer
	 *            the source viewer to be configured by this configuration
	 * @param contentType
	 *            the partition type for which the content assist processor is applicable
	 * @return IContentAssistProcessor or null if the content type is not supported
	 */
	protected IContentAssistProcessor getContentAssistProcessor(ISourceViewer sourceViewer, String contentType)
	{
		return new CommonContentAssistProcessor(getEditor());
	}

	/**
	 * Collects the code formatters by the supported content-types and returns a new {@link import
	 * org.eclipse.jface.text.formatter.MultiPassContentFormatter} that holds them.<br>
	 * The returned content formatter is computed from the result of {@link #getTopContentTypes()}. The first element in
	 * the returned array should define the 'master' formatter. While the rest of the elements should contain the
	 * 'slave' formatter. <br>
	 * Note that each slave formatter is located in the last element of each inner-array that was returned from the
	 * getTopContentTypes call.
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer)
	{
		final String[][] contentTypes = getTopContentTypes();
		final CommonMultiPassContentFormatter formatter = new CommonMultiPassContentFormatter(
				getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
		boolean masterSet = false;
		Set<String> addedFormatters = new HashSet<String>();
		for (String contentTypeArr[] : contentTypes)
		{
			// The first item in the array should contain the master formatter strategy
			// In case it starts with the HTML prefix (like in PHP, ERB etc.), we try to set
			// the master to the HTML formatter.
			if (!masterSet && contentTypeArr[0].startsWith(CONTENTTYPE_HTML_PREFIX))
			{
				if (ScriptFormatterManager.hasFormatterFor(CONTENTTYPE_HTML_PREFIX))
				{
					formatter.setMasterStrategy(CONTENTTYPE_HTML_PREFIX);
					masterSet = true;
					addedFormatters.add(CONTENTTYPE_HTML_PREFIX);
				}
				else
				{
					CommonEditorPlugin
							.logWarning("Could not located an expected code formatter for '" + CONTENTTYPE_HTML_PREFIX + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			String contentType = contentTypeArr[contentTypeArr.length - 1];
			if (!addedFormatters.contains(contentType) && ScriptFormatterManager.hasFormatterFor(contentType))
			{
				if (!masterSet)
				{
					formatter.setMasterStrategy(contentType);
					masterSet = true;
				}
				else
				{
					formatter.setSlaveStrategy(contentType);
				}
			}
		}
		return formatter;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source.
	 * ISourceViewer, java.lang.String)
	 */
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType)
	{
		if (fDoubleClickStrategy == null)
		{
			fDoubleClickStrategy = new CommonDoubleClickStrategy();
		}

		return fDoubleClickStrategy;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getHyperlinkDetectorTargets(org.eclipse.jface.text.
	 * source.ISourceViewer)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer)
	{
		Map targets = super.getHyperlinkDetectorTargets(sourceViewer);

		targets.put("com.aptana.editor.ui.hyperlinkTarget", fTextEditor); //$NON-NLS-1$

		return targets;
	}

	/**
	 * @return the default indentation string (either tab or spaces which represents a tab)
	 */
	public String getIndent()
	{
		boolean useSpaces = fPreferenceStore
				.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);

		if (useSpaces)
		{
			int tabWidth = fPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
			StringBuilder buf = new StringBuilder();

			for (int i = 0; i < tabWidth; ++i)
			{
				buf.append(" "); //$NON-NLS-1$
			}

			return buf.toString();
		}

		return "\t"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationControlCreator(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer)
	{
		return new IInformationControlCreator()
		{
			public IInformationControl createInformationControl(Shell parent)
			{
				return new ThemedInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true));
			}
		};
	}

	protected Color getThemeBackground()
	{
		RGB bg = getCurrentTheme().getBackground();
		return ThemePlugin.getDefault().getColorManager().getColor(bg);
	}

	protected Color getThemeForeground()
	{
		RGB bg = getCurrentTheme().getForeground();
		return ThemePlugin.getDefault().getColorManager().getColor(bg);
	}

	protected Color getThemeSelection()
	{
		RGB bg = getCurrentTheme().getSelectionAgainstBG();
		return ThemePlugin.getDefault().getColorManager().getColor(bg);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
	{
		return new TextHover(sourceViewer);
	}

	protected IInformationControl createTextHoverInformationControl(Shell parent, String statusFieldText)
	{
		return new ThemedInformationControl(parent, SWT.NONE, new HTMLTextPresenter(true), statusFieldText);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationPresenter(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer)
	{
		InformationPresenter presenter = new InformationPresenter(getInformationPresenterControlCreator(sourceViewer));

		presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		presenter.setSizeConstraints(60, 10, true, true);

		return presenter;
	}

	/**
	 * getInformationPresenterControlCreator
	 * 
	 * @param sourceViewer
	 * @return
	 */
	private IInformationControlCreator getInformationPresenterControlCreator(ISourceViewer sourceViewer)
	{
		return new IInformationControlCreator()
		{
			public IInformationControl createInformationControl(Shell parent)
			{
				return new DefaultInformationControl(parent, true);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getOverviewRulerAnnotationHover(org.eclipse.jface.text
	 * .source.ISourceViewer)
	 */
	@Override
	public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer)
	{
		return new CommonAnnotationHover(true)
		{
			protected boolean isIncluded(Annotation annotation)
			{
				return isShowInOverviewRuler(annotation);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer
	 * )
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer)
	{
		if (fTextEditor != null && fTextEditor.isEditable())
		{
			fReconcilingStrategy = new CommonCompositeReconcilingStrategy(fTextEditor,
					getConfiguredDocumentPartitioning(sourceViewer));
			CommonReconciler reconciler = new CommonReconciler(fTextEditor, fReconcilingStrategy, false);

			reconciler.setIsIncrementalReconciler(false);
			reconciler.setIsAllowedToModifyDocument(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);

			return reconciler;
		}

		return null;
	}

	protected AbstractThemeableEditor getEditor()
	{
		return fTextEditor;
	}

	/**
	 * Force the current reconciler to reconcile immediately, rather than on delay
	 */
	public void forceReconcile()
	{
		if (fReconcilingStrategy != null)
		{
			fReconcilingStrategy.reconcile(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getHyperlinkPresenter(org.eclipse.jface.text.source
	 * .ISourceViewer)
	 */
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer)
	{
		RGB rgb = getCurrentTheme().getForegroundAsRGB("hyperlink"); //$NON-NLS-1$
		return new MultipleHyperlinkPresenter(rgb);
	}

	protected Theme getCurrentTheme()
	{
		return ThemePlugin.getDefault().getThemeManager().getCurrentTheme();
	}

	private class TextHover extends DefaultTextHover implements ITextHoverExtension, ITextHoverExtension2 {

		private ITextHover activeTextHover;
		
		public TextHover(ISourceViewer sourceViewer) {
			super(sourceViewer);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
		 */
		@SuppressWarnings("deprecation")
		public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
			activeTextHover = null;
			try {
				QualifiedContentType contentType = CommonEditorPlugin.getDefault().getDocumentScopeManager().getContentType(textViewer.getDocument(), hoverRegion.getOffset());
				EvaluationContext context = new EvaluationContext(null, textViewer);
				context.addVariable(ISources.ACTIVE_EDITOR_ID_NAME, fTextEditor.getSite().getId());
				for (TextHoverDescriptor descriptor : TextHoverDescriptor.getContributedHovers()) {
					if (descriptor.isEnabledFor(contentType, context)) {
						ITextHover textHover = descriptor.createTextHover();
						Object info = null;
						if (textHover instanceof ITextHoverExtension2) {
							info = ((ITextHoverExtension2) textHover).getHoverInfo2(textViewer, hoverRegion);
						} else if (textHover != null) {
							info = textHover.getHoverInfo(textViewer, hoverRegion);
						}
						if (info != null) {
							activeTextHover = textHover;
							return info;
						}
					}
				}
			} catch (BadLocationException e) {
			}
			return super.getHoverInfo(textViewer, hoverRegion);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
		 */
		public IInformationControlCreator getHoverControlCreator() {
			if (activeTextHover instanceof ITextHoverExtension) {
				return ((ITextHoverExtension) activeTextHover).getHoverControlCreator();
			}
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return createTextHoverInformationControl(parent, EditorsUI.getTooltipAffordanceString());
				}
			};
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.DefaultTextHover#isIncluded(org.eclipse.jface.text.source.Annotation)
		 */
		@Override
		protected boolean isIncluded(Annotation annotation) {
			return isShownInText(annotation);
		}
	}
}
