package io.github.seerainer.csvedit.ui.dialog;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import io.github.seerainer.csvedit.ui.UIConstants;

/**
 * Find and Replace dialog for CSV table
 */
public class FindReplaceDialog extends ThemedDialog {

    private Text findText;
    private Text replaceText;
    private Button matchCaseCheck;
    private Button wholeWordCheck;
    private FindCallback findCallback;
    private ReplaceCallback replaceCallback;
    private ReplaceAllCallback replaceAllCallback;
    private String lastFindTerm = "";
    private String lastReplaceTerm = "";

    public FindReplaceDialog(final Shell parent) {
	super(parent, SWT.DIALOG_TRIM | SWT.TOOL | SWT.MODELESS);
	createContents();
    }

    private void createContents() {
	// Prevent disposal on close - just hide instead
	shell.addListener(SWT.Close, e -> {
	    e.doit = false;
	    shell.setVisible(false);
	});
	shell.setText("Find and Replace");
	shell.setLayout(new GridLayout(1, false));

	// Find field
	final var findComposite = new Composite(shell, SWT.NONE);
	findComposite.setLayout(new GridLayout(2, false));
	findComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(findComposite);

	final var findLabel = new Label(findComposite, SWT.NONE);
	findLabel.setText("Find:");
	applyTheme(findLabel);

	findText = new Text(findComposite, SWT.BORDER);
	findText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(findText);

	// Replace field
	final var replaceLabel = new Label(findComposite, SWT.NONE);
	replaceLabel.setText("Replace:");
	applyTheme(replaceLabel);

	replaceText = new Text(findComposite, SWT.BORDER);
	replaceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(replaceText);

	// Options
	final var optionsComposite = new Composite(shell, SWT.NONE);
	optionsComposite.setLayout(new GridLayout(1, false));
	optionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(optionsComposite);

	matchCaseCheck = new Button(optionsComposite, SWT.CHECK);
	matchCaseCheck.setText("Match case");
	applyTheme(matchCaseCheck);

	wholeWordCheck = new Button(optionsComposite, SWT.CHECK);
	wholeWordCheck.setText("Match whole word only");
	applyTheme(wholeWordCheck);

	// Buttons
	final var buttonBar = new Composite(shell, SWT.NONE);
	buttonBar.setLayout(new GridLayout(2, false));
	buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(buttonBar);

	final var findNextButton = new Button(buttonBar, SWT.PUSH);
	findNextButton.setText("Find Next");
	findNextButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(findNextButton);
	findNextButton.addSelectionListener(widgetSelectedAdapter(_ -> performFind()));

	final var replaceButton = new Button(buttonBar, SWT.PUSH);
	replaceButton.setText("Replace");
	replaceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(replaceButton);
	replaceButton.addSelectionListener(widgetSelectedAdapter(_ -> performReplace()));

	final var replaceAllButton = new Button(buttonBar, SWT.PUSH);
	replaceAllButton.setText("Replace All");
	replaceAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(replaceAllButton);
	replaceAllButton.addSelectionListener(widgetSelectedAdapter(_ -> performReplaceAll()));

	final var closeButton = new Button(buttonBar, SWT.PUSH);
	closeButton.setText("Close");
	closeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(closeButton);
	closeButton.addSelectionListener(widgetSelectedAdapter(_ -> shell.setVisible(false)));

	// Handle Enter key
	findText.addListener(SWT.Traverse, e -> {
	    if (e.detail == SWT.TRAVERSE_RETURN) {
		performFind();
		e.doit = false;
	    }
	});

	replaceText.addListener(SWT.Traverse, e -> {
	    if (e.detail == SWT.TRAVERSE_RETURN) {
		performReplace();
		e.doit = false;
	    }
	});

	shell.pack();
	shell.setMinimumSize(UIConstants.MIN_FIND_REPLACE_WIDTH, shell.getSize().y);
    }

    private void performFind() {
	final var term = findText.getText();
	if (term.isEmpty()) {
	    return;
	}
	lastFindTerm = term;
	if (findCallback != null) {
	    findCallback.onFind(term, matchCaseCheck.getSelection(), wholeWordCheck.getSelection());
	}
    }

    private void performReplace() {
	final var findTerm = findText.getText();
	final var replaceTerm = replaceText.getText();
	if (findTerm.isEmpty()) {
	    return;
	}
	lastFindTerm = findTerm;
	lastReplaceTerm = replaceTerm;
	if (replaceCallback != null) {
	    replaceCallback.onReplace(findTerm, replaceTerm, matchCaseCheck.getSelection(),
		    wholeWordCheck.getSelection());
	}
    }

    private void performReplaceAll() {
	final var findTerm = findText.getText();
	final var replaceTerm = replaceText.getText();
	if (findTerm.isEmpty()) {
	    return;
	}
	lastFindTerm = findTerm;
	lastReplaceTerm = replaceTerm;
	if (replaceAllCallback != null) {
	    replaceAllCallback.onReplaceAll(findTerm, replaceTerm, matchCaseCheck.getSelection(),
		    wholeWordCheck.getSelection());
	}
    }

    public void setFindCallback(final FindCallback callback) {
	this.findCallback = callback;
    }

    public void setReplaceCallback(final ReplaceCallback callback) {
	this.replaceCallback = callback;
    }

    public void setReplaceAllCallback(final ReplaceAllCallback callback) {
	this.replaceAllCallback = callback;
    }

    public void show() {
	if (!shell.isVisible()) {
	    // Center on parent
	    final var parentBounds = parentShell.getBounds();
	    final var shellSize = shell.getSize();
	    shell.setLocation(parentBounds.x + (parentBounds.width - shellSize.x) / 2, parentBounds.y + 100);
	    shell.open();
	}
	findText.setFocus();
	if (!lastFindTerm.isEmpty()) {
	    findText.setText(lastFindTerm);
	}
	if (!lastReplaceTerm.isEmpty()) {
	    replaceText.setText(lastReplaceTerm);
	}
	findText.selectAll();
    }

    /**
     * Callback for find operations
     */
    @FunctionalInterface
    public interface FindCallback {
	void onFind(String findTerm, boolean matchCase, boolean wholeWord);
    }

    /**
     * Callback for replace operations
     */
    @FunctionalInterface
    public interface ReplaceCallback {
	void onReplace(String findTerm, String replaceTerm, boolean matchCase, boolean wholeWord);
    }

    /**
     * Callback for replace all operations
     */
    @FunctionalInterface
    public interface ReplaceAllCallback {
	void onReplaceAll(String findTerm, String replaceTerm, boolean matchCase, boolean wholeWord);
    }
}
