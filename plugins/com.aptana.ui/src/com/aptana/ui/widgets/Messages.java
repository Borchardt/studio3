package com.aptana.ui.widgets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.ui.widgets.messages"; //$NON-NLS-1$

	public static String SingleProjectView_CaseSensitive;
	public static String SingleProjectView_InitialFileFilterText;
	public static String SingleProjectView_RegularExpression;
	public static String SingleProjectView_Wildcard;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
