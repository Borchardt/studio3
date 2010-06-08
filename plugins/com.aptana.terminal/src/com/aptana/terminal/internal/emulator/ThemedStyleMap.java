/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.terminal.internal.emulator;

import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.tm.internal.terminal.textcanvas.StyleMap;
import org.eclipse.tm.terminal.model.StyleColor;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.theme.Theme;

/**
 * @author Max Stepanov
 *
 */
/* package */ class ThemedStyleMap extends StyleMap {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.textcanvas.StyleMap#getColor(java.util.Map, org.eclipse.tm.terminal.model.StyleColor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Color getColor(Map map, StyleColor color) {
		if (color.getName().equalsIgnoreCase(StyleMap.BLACK)) {
			return getForegroundColor();
		}
		if (color.getName().equalsIgnoreCase(StyleMap.WHITE)) {
			return getBackgroundColor();
		}
		if (color.getName().equalsIgnoreCase(StyleMap.WHITE_FOREGROUND)) {
			// FIXME turn to "ansi.white"
		}

		// Just grab colors straight from theme!
		String ansiName = "ansi." + color.getName().toLowerCase(); //$NON-NLS-1$
		Theme theme = CommonEditorPlugin.getDefault().getThemeManager().getCurrentTheme();
		if (theme.hasEntry(ansiName)) {
			return theme.getForeground(ansiName);
		}
		// fall back to defaults...
		return super.getColor(map, color);
	}

	protected Color getBackgroundColor() {
		Theme theme = CommonEditorPlugin.getDefault().getThemeManager().getCurrentTheme();
		return CommonEditorPlugin.getDefault().getColorManager().getColor(theme.getBackground());
	}

	protected Color getForegroundColor() {
		Theme theme = CommonEditorPlugin.getDefault().getThemeManager().getCurrentTheme();
		return CommonEditorPlugin.getDefault().getColorManager().getColor(theme.getForeground());
	}


}
