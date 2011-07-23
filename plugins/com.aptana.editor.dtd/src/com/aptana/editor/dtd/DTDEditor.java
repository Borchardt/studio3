/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.dtd;

import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.editor.common.AbstractThemeableEditor;

public class DTDEditor extends AbstractThemeableEditor
{

	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();

		this.setSourceViewerConfiguration(new DTDSourceViewerConfiguration(this.getPreferenceStore(), this));
		this.setDocumentProvider(DTDPlugin.getDefault().getDTDDocumentProvider());
	}

	@Override
	protected IPreferenceStore getPluginPreferenceStore()
	{
		return DTDPlugin.getDefault().getPreferenceStore();
	}
}
