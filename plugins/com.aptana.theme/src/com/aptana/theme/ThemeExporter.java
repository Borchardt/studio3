/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.theme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;

/**
 * @author cwilliams
 */
public class ThemeExporter
{

	/**
	 * Export a theme to a *.tmTheme file.
	 * 
	 * @param themeFile
	 *            The destination file
	 * @param theme
	 *            The theme to export to ruby (ruble) code
	 */
	@SuppressWarnings("nls")
	public void export(File themeFile, Theme theme)
	{
		// Spit out a tmTheme plist file!
		StringBuilder buffer = new StringBuilder();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
		buffer.append("<plist version=\"1.0\">\n");
		buffer.append("<dict>\n");
		buffer.append("  <key>name</key>\n");
		buffer.append("  <string>").append(theme.getName()).append("</string>\n");
		buffer.append("  <key>uuid</key>\n");
		buffer.append("  <string>").append(UUID.nameUUIDFromBytes(theme.getName().getBytes())).append("</string>\n");
		buffer.append("  <key>settings</key>\n");
		buffer.append("  <array>\n");
		buffer.append("    <dict>\n");
		buffer.append("      <key>settings</key>\n");
		buffer.append("      <dict>\n");
		// Global Theme colors
		buffer.append("        <key>foreground</key>\n");
		buffer.append("        <string>").append(Theme.toHex(theme.getForeground())).append("</string>\n");
		buffer.append("        <key>background</key>\n");
		buffer.append("        <string>").append(Theme.toHex(theme.getBackground())).append("</string>\n");
		buffer.append("        <key>caret</key>\n");
		buffer.append("        <string>").append(Theme.toHex(theme.getCaret())).append("</string>\n");
		buffer.append("        <key>lineHighlight</key>\n");
		buffer.append("        <string>").append(Theme.toHex(theme.getLineHighlight())).append("</string>\n");
		buffer.append("        <key>selection</key>\n");
		buffer.append("        <string>").append(Theme.toHex(theme.getSelection())).append("</string>\n");
		buffer.append("        <key>invisibles</key>\n");
		buffer.append("        <string>#404040</string>\n");
		buffer.append("      </dict>\n");
		buffer.append("    </dict>\n");

		// Add a dict for each rule
		for (ThemeRule rule : theme.getTokens())
		{
			buffer.append("    <dict>\n");
			buffer.append("      <key>name</key>\n");
			buffer.append("      <string>").append(rule.getName()).append("</string>\n");
			buffer.append("      <key>scope</key>\n");
			buffer.append("      <string>").append(rule.getScopeSelector().toString()).append("</string>\n");
			buffer.append("      <key>settings</key>\n");
			buffer.append("      <dict>\n");

			DelayedTextAttribute attr = rule.getTextAttribute();
			RGBa color = attr.getForeground();
			if (color != null)
			{
				buffer.append("        <key>foreground</key>\n");
				buffer.append("        <string>").append(Theme.toHex(color)).append("</string>\n");
			}
			color = attr.getBackground();
			if (color != null)
			{
				buffer.append("        <key>background</key>\n");
				buffer.append("        <string>").append(Theme.toHex(color)).append("</string>\n");
			}

			// Spit out italic, bold, etc
			StringBuilder value = new StringBuilder();
			int style = attr.getStyle();
			if ((style & SWT.ITALIC) != 0)
			{
				value.append("italic").append(",");
			}
			if ((style & TextAttribute.UNDERLINE) != 0)
			{
				value.append("underline").append(",");
			}
			if ((style & SWT.BOLD) != 0)
			{
				value.append("bold").append(",");
			}
			if (value.length() > 0)
			{
				value.deleteCharAt(value.length() - 1);
				buffer.append("        <key>fontStyle</key>\n");
				buffer.append("        <string>").append(value).append("</string>\n");
			}

			buffer.append("      </dict>\n");
			buffer.append("    </dict>\n");
		}

		buffer.append("  </array>\n");
		buffer.append("</dict>\n");
		buffer.append("</plist>\n");

		Writer writer = null;
		try
		{
			writer = new FileWriter(themeFile);
			writer.write(buffer.toString());
		}
		catch (IOException e)
		{
			ThemePlugin.logError(e);
		}
		finally
		{
			try
			{
				if (writer != null)
				{
					writer.close();
				}
			}
			catch (IOException e)
			{
				// ignore
			}
		}
	}

}
