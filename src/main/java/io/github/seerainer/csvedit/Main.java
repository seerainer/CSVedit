package io.github.seerainer.csvedit;

import org.eclipse.swt.widgets.Display;

import io.github.seerainer.csvedit.ui.MainWindow;

public class Main {

    static {
	System.setProperty("org.eclipse.swt.display.useSystemTheme", "true");
    }

    private Main() {
	throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static void main(final String[] args) {
	final var display = Display.getDefault();
	final var mainUI = new MainWindow(display, args.length > 0 ? args[0] : null);
	final var shell = mainUI.getShell();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
	display.dispose();
    }
}