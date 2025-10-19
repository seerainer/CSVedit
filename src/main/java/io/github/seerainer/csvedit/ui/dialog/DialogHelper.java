package io.github.seerainer.csvedit.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DialogHelper {

    private final Shell shell;

    public DialogHelper(final Shell shell) {
	this.shell = shell;
    }

    public int showConfirmation(final String title, final String message) {
	final var box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
	box.setText(title);
	box.setMessage(message);
	return box.open();
    }

    public void showError(final String title, final String message) {
	final var box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
	box.setText(title);
	box.setMessage(message);
	box.open();
    }

    public void showInfo(final String title, final String message) {
	final var box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
	box.setText(title);
	box.setMessage(message);
	box.open();
    }

    public String showOpenFileDialog(final String[] extensions, final String[] names) {
	final var dialog = new FileDialog(shell, SWT.OPEN);
	dialog.setFilterExtensions(extensions);
	dialog.setFilterNames(names);
	return dialog.open();
    }

    public String showSaveFileDialog(final String[] extensions, final String[] names, final String fileName) {
	final var dialog = new FileDialog(shell, SWT.SAVE);
	dialog.setFilterExtensions(extensions);
	dialog.setFilterNames(names);
	dialog.setOverwrite(true);
	if (fileName != null) {
	    dialog.setFileName(fileName);
	}
	return dialog.open();
    }

    public int showYesNoCancelDialog(final String title, final String message) {
	final var box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
	box.setText(title);
	box.setMessage(message);
	return box.open();
    }
}
