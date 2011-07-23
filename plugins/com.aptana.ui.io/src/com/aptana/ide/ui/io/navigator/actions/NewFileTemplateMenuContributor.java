/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.ui.io.navigator.actions;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.jruby.embed.io.ReaderInputStream;

import com.aptana.editor.common.internal.scripting.NewFileWizard;
import com.aptana.editor.common.internal.scripting.NewTemplateFileWizard;
import com.aptana.scripting.model.AbstractElement;
import com.aptana.scripting.model.BundleManager;
import com.aptana.scripting.model.CommandElement;
import com.aptana.scripting.model.TemplateElement;
import com.aptana.scripting.model.filters.IModelFilter;
import com.aptana.ui.util.UIUtils;

/**
 * @author Michael Xia
 */
public class NewFileTemplateMenuContributor extends ContributionItem
{

	public NewFileTemplateMenuContributor()
	{
	}

	public NewFileTemplateMenuContributor(String id)
	{
		super(id);
	}

	@Override
	public boolean isDynamic()
	{
		return true;
	}

	@Override
	public void fill(Menu menu, int index)
	{
		// constructs the menus
		Map<String, List<TemplateElement>> templatesByBundle = getNewFileTemplates();
		Set<String> bundles = templatesByBundle.keySet();
		List<TemplateElement> templates;
		// first level shows the bundles which have file templates defined
		for (String bundle : bundles)
		{
			MenuItem bundleItem = new MenuItem(menu, SWT.CASCADE);
			bundleItem.setText(bundle);

			Menu bundleMenu = new Menu(menu);
			bundleItem.setMenu(bundleMenu);

			// second level shows the templates for each bundle
			templates = templatesByBundle.get(bundle);
			for (final TemplateElement template : templates)
			{
				MenuItem templateItem = new MenuItem(bundleMenu, SWT.PUSH);
				templateItem.setText(template.getDisplayName());
				templateItem.addSelectionListener(new SelectionAdapter()
				{

					@Override
					public void widgetSelected(SelectionEvent e)
					{
						createNewFileFromTemplate(template);
					}
				});
			}
		}
	}

	protected void createNewFileFromTemplate(final TemplateElement template)
	{
		IStructuredSelection selection = getActiveSelection();
		if (!selection.isEmpty())
		{
			Object element = selection.getFirstElement();
			if (element instanceof IAdaptable)
			{
				IFileStore fileStore = (IFileStore) ((IAdaptable) element).getAdapter(IFileStore.class);
				if (fileStore != null)
				{
					// this is a non-workspace selection
					String filetype = template.getFiletype();
					// strips the leading * before . if there is one
					int index = filetype.lastIndexOf("."); //$NON-NLS-1$
					if (index > -1)
					{
						filetype = filetype.substring(index);
					}
					NewFileAction action = new NewFileAction(UIUtils.getActiveWorkbenchWindow(), "new_file" + filetype) //$NON-NLS-1$
					{

						@Override
						protected InputStream getInitialContents(IPath path)
						{
							String templateContent = NewFileWizard.getTemplateContent(template, path);
							if (templateContent != null)
							{
								return new ReaderInputStream(new StringReader(templateContent), "UTF-8"); //$NON-NLS-1$
							}
							return super.getInitialContents(path);
						}
					};
					action.updateSelection(selection);
					action.run();
					return;
				}
			}
		}

		NewTemplateFileWizard wizard = new NewTemplateFileWizard(template);
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(UIUtils.getActiveShell(), wizard);
		dialog.open();
	}

	private static IStructuredSelection getActiveSelection()
	{
		IStructuredSelection selection = null;
		IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getService(
				IEvaluationService.class);
		if (evaluationService != null)
		{
			IEvaluationContext currentState = evaluationService.getCurrentState();
			Object variable = currentState.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			if (variable instanceof IStructuredSelection)
			{
				selection = (IStructuredSelection) variable;
			}
		}
		return (selection == null) ? StructuredSelection.EMPTY : selection;
	}

	private Map<String, List<TemplateElement>> getNewFileTemplates()
	{
		Map<String, List<TemplateElement>> templatesByBundle = new TreeMap<String, List<TemplateElement>>();
		List<CommandElement> commands = BundleManager.getInstance().getExecutableCommands(new IModelFilter()
		{

			public boolean include(AbstractElement element)
			{
				if (element instanceof TemplateElement)
				{
					TemplateElement te = (TemplateElement) element;
					return te.getFiletype() != null && te.getOwningBundle() != null;
				}
				return false;
			}
		});
		if (commands != null)
		{
			String bundleName;
			List<TemplateElement> templates;
			for (CommandElement command : commands)
			{
				bundleName = command.getOwningBundle().getDisplayName();
				templates = templatesByBundle.get(bundleName);
				if (templates == null)
				{
					templates = new ArrayList<TemplateElement>();
					templatesByBundle.put(bundleName, templates);
				}
				templates.add((TemplateElement) command);
			}
		}
		return templatesByBundle;
	}
}
