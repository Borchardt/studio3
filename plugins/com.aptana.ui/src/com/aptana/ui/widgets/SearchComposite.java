package com.aptana.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.aptana.core.util.StringUtil;
import com.aptana.ui.UIPlugin;

public class SearchComposite extends Composite
{

	public static interface Client
	{
		public void search(String text, boolean isCaseSensitive, boolean isRegularExpression);
	}

	private static final String CASE_SENSITIVE_ICON_PATH = "icons/full/elcl16/casesensitive.png"; //$NON-NLS-1$
	private static final String REGULAR_EXPRESSION_ICON_PATH = "icons/full/elcl16/regularexpression.png"; //$NON-NLS-1$
	private static final String INITIAL_TEXT = Messages.SingleProjectView_InitialFileFilterText;

	private Text searchText;
	private ToolItem caseSensitiveMenuItem;
	private ToolItem regularExressionMenuItem;
	private boolean searchOnEnter = true;
	private String initialText = INITIAL_TEXT;
	private String lastSearch = StringUtil.EMPTY;
	private boolean lastCaseSensitiveState;
	private boolean lastRegularExpressionState;

	private Client client;

	public SearchComposite(Composite parent, Client client)
	{
		this(parent, SWT.NONE, client);
	}

	public SearchComposite(Composite parent, int style, Client client)
	{
		super(parent, style);
		this.client = client;

		GridLayout searchGridLayout = new GridLayout(2, false);
		searchGridLayout.marginWidth = 2;
		searchGridLayout.marginHeight = 0;
		setLayout(searchGridLayout);

		searchText = new Text(this, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		searchText.setText(initialText);
		searchText.setToolTipText(Messages.SingleProjectView_Wildcard);
		searchText.setForeground(searchText.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		searchText.addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (searchText.getText().length() == 0)
				{
					searchText.setText(initialText);
				}
				searchText.setForeground(searchText.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
			}

			@Override
			public void focusGained(FocusEvent e)
			{
				if (searchText.getText().equals(initialText))
				{
					searchText.setText(""); //$NON-NLS-1$
				}
				searchText.setForeground(null);
			}
		});

		searchText.addKeyListener(new KeyListener()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				if (!e.doit)
				{
					return;
				}
				if (searchOnEnter && e.keyCode == SWT.CR)
				{
					searchText();
					e.doit = false;
				}
			}
		});
		searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!searchOnEnter) {
					searchText();
				}
			}
		});

		// Button for search options
		ToolBar toolbar = new ToolBar(this, SWT.NONE);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		caseSensitiveMenuItem = new ToolItem(toolbar, SWT.CHECK);
		caseSensitiveMenuItem.setImage(UIPlugin.getImage(CASE_SENSITIVE_ICON_PATH));
		caseSensitiveMenuItem.setToolTipText(Messages.SingleProjectView_CaseSensitive);
		caseSensitiveMenuItem.setSelection(lastCaseSensitiveState);
		caseSensitiveMenuItem.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				searchText.setFocus();
				if (!searchOnEnter) {
					searchText();
				}
			}
		});

		regularExressionMenuItem = new ToolItem(toolbar, SWT.CHECK);
		regularExressionMenuItem.setImage(UIPlugin.getImage(REGULAR_EXPRESSION_ICON_PATH));
		regularExressionMenuItem.setToolTipText(Messages.SingleProjectView_RegularExpression);
		regularExressionMenuItem.setSelection(lastRegularExpressionState);
		regularExressionMenuItem.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				searchText.setFocus();
				if (!searchOnEnter) {
					searchText();
				}
			}
		});
	}

	/**
	 * @param searchOnEnter the searchOnEnter to set
	 */
	public void setSearchOnEnter(boolean searchOnEnter) {
		this.searchOnEnter = searchOnEnter;
	}

	/**
	 * @param initialText the initialText to set
	 */
	public void setInitialText(String initialText) {
		if (searchText.getText().equals(this.initialText)) {
			this.initialText = initialText;
			searchText.setText(initialText);
		}
		this.initialText = initialText;
	}

	@Override
	public boolean setFocus()
	{
		return searchText.setFocus();
	}

	public Text getTextControl()
	{
		return searchText;
	}

	private void searchText()
	{
		String text = searchText.getText();
		if (initialText.equals(text)) {
			text = StringUtil.EMPTY;
		}
		if (client != null
				&& (searchOnEnter
						|| !text.equals(lastSearch)
						|| (caseSensitiveMenuItem.getSelection() != lastCaseSensitiveState)
						|| (regularExressionMenuItem.getSelection() != lastRegularExpressionState)))
		{
			lastSearch = text;
			lastCaseSensitiveState = caseSensitiveMenuItem.getSelection();
			lastRegularExpressionState = regularExressionMenuItem.getSelection();
			client.search(text, lastCaseSensitiveState, lastRegularExpressionState);
		}
	}
}
