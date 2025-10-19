package io.github.seerainer.csvedit.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import io.github.seerainer.csvedit.ui.UIConstants;

/**
 * Progress dialog for showing file loading progress
 */
public class ProgressDialog extends ThemedDialog {

    private ProgressBar progressBar;
    private Label statusLabel;
    private Label detailsLabel;
    private Button cancelButton;
    private volatile boolean cancelled = false;
    private CancelCallback cancelCallback;

    public ProgressDialog(final Shell parent, final String title) {
	super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	createContents(title);
	centerOnParent();
    }

    private void createContents(final String title) {
	shell.setText(title);
	shell.setLayout(new GridLayout(1, false));
	shell.setSize(UIConstants.PROGRESS_DIALOG_WIDTH, UIConstants.PROGRESS_DIALOG_HEIGHT);

	// Status label
	statusLabel = new Label(shell, SWT.NONE);
	statusLabel.setText("Initializing...");
	statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(statusLabel);

	// Progress bar
	progressBar = new ProgressBar(shell, SWT.SMOOTH);
	progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	progressBar.setMinimum(0);
	progressBar.setMaximum(100);

	// Details label
	detailsLabel = new Label(shell, SWT.NONE);
	detailsLabel.setText("0 rows loaded");
	detailsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(detailsLabel);

	// Spacer
	final var spacer = new Label(shell, SWT.NONE);
	spacer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	// Cancel button
	cancelButton = new Button(shell, SWT.PUSH);
	cancelButton.setText("Cancel");
	final var buttonData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	buttonData.widthHint = UIConstants.OK_BUTTON_WIDTH;
	cancelButton.setLayoutData(buttonData);
	cancelButton.addListener(SWT.Selection, _ -> handleCancel());
	applyTheme(cancelButton);
    }

    private void handleCancel() {
	cancelled = true;
	if (cancelCallback != null) {
	    cancelCallback.onCancel();
	}
	shell.close();
    }

    public boolean isCancelled() {
	return cancelled;
    }

    /**
     * Close the dialog.
     */
    public void close() {
	if (!shell.isDisposed()) {
	    shell.close();
	}
    }

    public void open() {
	shell.open();
    }

    public void setCancelCallback(final CancelCallback callback) {
	this.cancelCallback = callback;
    }

    /**
     * Update progress - must be called from UI thread
     */
    public void updateProgress(final int rowsLoaded, final long totalRows, final boolean isComplete) {
	if (shell.isDisposed()) {
	    return;
	}

	final var display = shell.getDisplay();
	display.asyncExec(() -> {
	    if (shell.isDisposed()) {
		return;
	    }

	    if (isComplete) {
		statusLabel.setText("Loading complete!");
		progressBar.setSelection(100);
		detailsLabel.setText(rowsLoaded + " rows loaded");
		cancelButton.setEnabled(false);

		// Auto-close after a short delay
		display.timerExec(UIConstants.PROGRESS_AUTO_CLOSE_DELAY_MS, shell::close);
	    } else {
		statusLabel.setText("Loading data...");

		if (totalRows > 0) {
		    final var percentage = (int) ((rowsLoaded * 100.0) / totalRows);
		    progressBar.setSelection(percentage);
		    detailsLabel.setText(
			    "%d / %d rows loaded".formatted(Integer.valueOf(rowsLoaded), Long.valueOf(totalRows)));
		} else {
		    // Indeterminate progress
		    progressBar.setSelection(progressBar.getSelection() + 5);
		    if (progressBar.getSelection() >= 100) {
			progressBar.setSelection(0);
		    }
		    detailsLabel.setText(rowsLoaded + " rows loaded");
		}
	    }

	    shell.layout(true, true);
	});
    }

    /**
     * Update status message - can be called from any thread
     */
    public void updateStatus(final String message) {
	if (shell.isDisposed()) {
	    return;
	}

	final var display = shell.getDisplay();
	// Check if we're on UI thread
	if (Display.getCurrent() != null) {
	    // Already on UI thread, update directly
	    if (!statusLabel.isDisposed()) {
		statusLabel.setText(message);
		shell.layout(true, true);
		shell.update();
	    }
	} else {
	    // Not on UI thread, use asyncExec
	    display.asyncExec(() -> {
		if (!shell.isDisposed() && !statusLabel.isDisposed()) {
		    statusLabel.setText(message);
		    shell.layout(true, true);
		}
	    });
	}
    }

    @FunctionalInterface
    public interface CancelCallback {
	void onCancel();
    }
}