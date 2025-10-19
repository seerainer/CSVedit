package io.github.seerainer.csvedit.ui.dialog;

import static org.eclipse.swt.events.MenuListener.menuShownAdapter;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static org.eclipse.swt.events.ShellListener.shellClosedAdapter;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import io.github.seerainer.csvedit.io.CSVConfigurationFactory;
import io.github.seerainer.csvedit.io.CSVParserUtil;
import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.ui.Icons;
import io.github.seerainer.csvedit.ui.UIConstants;
import io.github.seerainer.csvedit.util.Settings;

/**
 * Text editor dialog for editing CSV data as plain text
 */
public class TextEditorDialog extends ThemedDialog {

    private final CSVTableModel model;
    private final DialogHelper dialogHelper;

    private Font textEditorFont;
    private StyledText textWidget;
    private boolean changesMade = false;
    private boolean isClosing = false;

    public TextEditorDialog(final Shell parent, final CSVTableModel model) {
	super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | SWT.MIN);
	this.model = model;
	this.dialogHelper = new DialogHelper(parent);
	createContents();
	loadModelToText();
	centerOnParent();
    }

    private static MenuItem separator(final Menu contextMenu) {
	return new MenuItem(contextMenu, SWT.SEPARATOR);
    }

    private void applyTextEditorFont() {
	// Get font data from settings and create our own Font instance
	final var fontData = Settings.getFontData();
	if (fontData == null) {
	    return;
	}
	// Create our own Font instance that we are responsible for disposing
	textEditorFont = new Font(shell.getDisplay(), fontData);
	textWidget.setFont(textEditorFont);
    }

    private void createContents() {
	shell.addDisposeListener(_ -> {
	    if (textEditorFont != null) {
		textEditorFont.dispose();
	    }
	});
	shell.addShellListener(shellClosedAdapter(e -> {
	    if (isClosing) {
		return;
	    }
	    e.doit = false; // Prevent default close
	    handleCancel();
	}));
	shell.setText("Text Editor View");
	shell.setLayout(new GridLayout(1, false));
	shell.setSize(800, 600);
	shell.setImage(Icons.getImage(Icons.APP_ICON));

	// Info label
	final var infoLabel = new Label(shell, SWT.NONE);
	infoLabel.setText("Edit CSV data as text. Changes will be applied to the table when you click OK.");
	infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(infoLabel);

	// Text editor
	textWidget = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	textWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	textWidget.setTabs(4);
	applyTheme(textWidget);

	// Explicitly set key binding for Ctrl+A (Select All)
	// SWT.MOD1 is Ctrl on Windows/Linux, Cmd on Mac
	textWidget.setKeyBinding('A' | SWT.MOD1, ST.SELECT_ALL);

	// Apply configured font to text editor
	applyTextEditorFont();

	// Create context menu for text editor
	createTextEditorContextMenu();

	// Track changes
	textWidget.addModifyListener(_ -> changesMade = true);

	// Button bar
	final var buttonBar = new Composite(shell, SWT.NONE);
	buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	buttonBar.setLayout(new GridLayout(3, false));
	applyTheme(buttonBar);

	// Spacer
	final var spacer = new Label(buttonBar, SWT.NONE);
	spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	// OK button
	final var okButton = new Button(buttonBar, SWT.PUSH);
	okButton.setText("OK");
	final var buttonData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	buttonData.widthHint = UIConstants.OK_BUTTON_WIDTH;
	okButton.setLayoutData(buttonData);
	okButton.addListener(SWT.Selection, _ -> handleOk());
	applyTheme(okButton);

	// Cancel button
	final var cancelButton = new Button(buttonBar, SWT.PUSH);
	cancelButton.setText("Cancel");
	cancelButton.setLayoutData(buttonData);
	cancelButton.addListener(SWT.Selection, _ -> handleCancel());
	applyTheme(cancelButton);

	shell.setDefaultButton(okButton);
    }

    private void createTextEditorContextMenu() {
	final var contextMenu = new Menu(textWidget);
	textWidget.setMenu(contextMenu);

	// Cut
	final var cutItem = new MenuItem(contextMenu, SWT.PUSH);
	cutItem.setText("Cu&t\tCtrl+X");
	cutItem.addSelectionListener(widgetSelectedAdapter(_ -> textWidget.cut()));

	// Copy
	final var copyItem = new MenuItem(contextMenu, SWT.PUSH);
	copyItem.setText("&Copy\tCtrl+C");
	copyItem.addSelectionListener(widgetSelectedAdapter(_ -> textWidget.copy()));

	// Paste
	final var pasteItem = new MenuItem(contextMenu, SWT.PUSH);
	pasteItem.setText("&Paste\tCtrl+V");
	pasteItem.addSelectionListener(widgetSelectedAdapter(_ -> textWidget.paste()));

	// Separator
	separator(contextMenu);

	// Select All
	final var selectAllItem = new MenuItem(contextMenu, SWT.PUSH);
	selectAllItem.setText("Select &All\tCtrl+A");
	selectAllItem.addSelectionListener(widgetSelectedAdapter(_ -> textWidget.selectAll()));

	// Enable/disable menu items based on context
	contextMenu.addMenuListener(menuShownAdapter(_ -> {
	    final var hasSelection = textWidget.getSelectionCount() > 0;
	    final var hasText = textWidget.getCharCount() > 0;

	    cutItem.setEnabled(hasSelection);
	    copyItem.setEnabled(hasSelection);
	    selectAllItem.setEnabled(hasText);
	}));
    }

    private void handleCancel() {
	if (isClosing) {
	    return;
	}
	isClosing = true;

	if (changesMade) {
	    final var result = dialogHelper.showConfirmation("Unsaved Changes",
		    "You have unsaved changes. Are you sure you want to cancel?");
	    if (result == SWT.YES) {
		shell.close();
	    }
	} else {
	    shell.close();
	}
	isClosing = false;
    }

    private void handleOk() {
	if (!changesMade) {
	    shell.close();
	    return;
	}

	isClosing = true;
	try {
	    parseTextToModel();
	    shell.close();
	} catch (final Exception e) {
	    dialogHelper.showError("Parse Error", "Failed to parse CSV text: " + e.getMessage());
	}
    }

    /**
     * Convert the model to CSV text format
     */
    private void loadModelToText() {
	final var sb = new StringBuilder();
	final var headers = model.getHeaders();
	final var rowCount = model.getRowCount();
	final var colCount = model.getColumnCount();

	// Write headers
	for (var j = 0; j < colCount; j++) {
	    var value = j < headers.size() ? headers.get(j) : "Column " + (j + 1);
	    value = CSVConfigurationFactory.escapeValue(value);
	    sb.append(value);
	    if (j < colCount - 1) {
		sb.append(',');
	    }
	}
	sb.append('\n');

	// Write data rows
	for (var i = 0; i < rowCount; i++) {
	    final var row = model.getRow(i);
	    for (var j = 0; j < colCount; j++) {
		var value = j < row.size() ? row.get(j) : "";
		value = CSVConfigurationFactory.escapeValue(value);
		sb.append(value);
		if (j < colCount - 1) {
		    sb.append(',');
		}
	    }
	    sb.append('\n');
	}

	textWidget.setText(sb.toString());
	changesMade = false;
    }

    /**
     * Open the dialog
     */
    public void open() {
	shell.open();
	final var display = parentShell.getDisplay();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
    }

    /**
     * Parse the text content back into the model
     */
    private void parseTextToModel() throws IOException {
	final var csvText = textWidget.getText();

	// Create CSV parser configuration from settings
	final var config = CSVConfigurationFactory.createConfiguration();

	// Parse the CSV text
	final var bytes = csvText.getBytes(config.getEncoding());
	final var parsed = CSVParserUtil.parseCSVBytes(bytes);

	// Update the model
	model.clear();
	model.setHeaders(parsed.getHeaders());
	model.setData(parsed.getData());
	model.normalize();
    }

    /**
     * Check if changes were made and saved
     */
    public boolean wasModified() {
	return changesMade;
    }
}
