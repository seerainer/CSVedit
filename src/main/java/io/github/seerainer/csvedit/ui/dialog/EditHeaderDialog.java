package io.github.seerainer.csvedit.ui.dialog;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for editing a column header name.
 */
public class EditHeaderDialog extends ThemedDialog {

    private final String currentHeaderText;
    private String newHeaderText;
    private boolean confirmed = false;

    /**
     * Creates an edit header dialog.
     *
     * @param parent            the parent shell
     * @param currentHeaderText the current header text
     */
    public EditHeaderDialog(final Shell parent, final String currentHeaderText) {
	super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	this.currentHeaderText = currentHeaderText;
	this.newHeaderText = currentHeaderText;
	createContents();
	centerOnParent();
    }

    /**
     * Opens the dialog and returns true if the user confirmed the edit.
     *
     * @return true if the user clicked OK and changed the header text, false
     *         otherwise
     */
    public boolean open() {
	shell.open();
	final var display = shell.getDisplay();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
	return confirmed;
    }

    /**
     * Gets the new header text entered by the user.
     *
     * @return the new header text, or the original text if cancelled
     */
    public String getNewHeaderText() {
	return newHeaderText;
    }

    /**
     * Creates the dialog contents.
     */
    private void createContents() {
	shell.setText("Edit Column Header");
	shell.setLayout(new GridLayout(2, false));

	final var label = new Label(shell, SWT.NONE);
	label.setText("Column Name:");
	label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
	applyTheme(label);

	final var textField = new Text(shell, SWT.BORDER);
	textField.setText(currentHeaderText);
	textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	textField.selectAll();
	applyTheme(textField);

	final var okButton = new Button(shell, SWT.PUSH);
	okButton.setText("OK");
	okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(okButton);
	okButton.addSelectionListener(widgetSelectedAdapter(_ -> handleOk(textField)));

	final var cancelButton = new Button(shell, SWT.PUSH);
	cancelButton.setText("Cancel");
	cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(cancelButton);
	cancelButton.addSelectionListener(widgetSelectedAdapter(_ -> handleCancel()));

	textField.addListener(SWT.Traverse, e -> {
	    if (e.detail == SWT.TRAVERSE_RETURN) {
		handleOk(textField);
		e.doit = false;
	    } else if (e.detail == SWT.TRAVERSE_ESCAPE) {
		handleCancel();
		e.doit = false;
	    }
	});

	shell.pack();
	shell.setMinimumSize(300, shell.getSize().y);
	shell.getDisplay().asyncExec(textField::setFocus);
    }

    /**
     * Handles the OK button action.
     */
    private void handleOk(final Text textField) {
	final var text = textField.getText().trim();
	if (!text.isEmpty() && !text.equals(currentHeaderText)) {
	    newHeaderText = text;
	    confirmed = true;
	}
	shell.close();
    }

    /**
     * Handles the Cancel button action.
     */
    private void handleCancel() {
	confirmed = false;
	shell.close();
    }
}
