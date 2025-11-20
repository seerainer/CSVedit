package io.github.seerainer.csvedit.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import io.github.seerainer.csvedit.theme.ThemeManager;

/**
 * Base class for dialogs with consistent theme application. Handles
 * ThemeManager lifecycle and provides common theming utilities.
 */
abstract class ThemedDialog {

    protected final Shell shell;
    protected final Shell parentShell;
    protected final ThemeManager themeManager;

    /**
     * Creates a themed dialog with the specified style.
     *
     * @param parent the parent shell
     * @param style  the shell style flags (e.g., SWT.DIALOG_TRIM |
     *               SWT.APPLICATION_MODAL)
     */
    protected ThemedDialog(final Shell parent, final int style) {
	this.parentShell = parent;
	this.themeManager = new ThemeManager();
	this.shell = new Shell(parent, style);
	setupTheming();
    }

    /**
     * Apply theme to a control. Convenience method for subclasses.
     *
     * @param control the control to theme
     */
    protected void applyTheme(final Control control) {
	themeManager.applyToControl(control);
    }

    /**
     * Center the dialog on its parent shell.
     */
    protected void centerOnParent() {
	final var parentBounds = parentShell.getBounds();
	final var shellSize = shell.getSize();
	shell.setLocation(parentBounds.x + (parentBounds.width - shellSize.x) / 2,
		parentBounds.y + (parentBounds.height - shellSize.y) / 2);
    }

    /**
     * Initialize theming for the shell and set up disposal.
     */
    private void setupTheming() {
	shell.setBackgroundMode(SWT.INHERIT_FORCE);
	themeManager.applyToControl(shell);
    }
}
