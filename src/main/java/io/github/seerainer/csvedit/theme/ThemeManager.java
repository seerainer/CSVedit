package io.github.seerainer.csvedit.theme;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

public class ThemeManager {

    private static final boolean DARK_THEME = Display.isSystemDarkTheme();
    private static final boolean MACOS = getPlatform("cocoa");
    private static final boolean WIN32 = getPlatform("win32");

    private final Color darkHead;
    private final Color darkBack;
    private final Color darkBack2;
    private final Color darkFore;

    public ThemeManager(final Display display) {
	this.darkHead = new Color(display, 60, 60, 60);
	this.darkBack = new Color(display, 48, 48, 48);
	this.darkBack2 = new Color(display, 36, 36, 36);
	this.darkFore = new Color(display, 255, 255, 255);
    }

    public static boolean isDarkTheme() {
	return DARK_THEME;
    }

    public void applyToControl(final Control control) {
	if (!DARK_THEME || MACOS || control == null || control.isDisposed()) {
	    return;
	}
	control.setBackground(darkBack);
	control.setForeground(darkFore);
	if (!(control instanceof final Table table)) {
	    return;
	}
	table.setHeaderBackground(darkBack);
	table.setHeaderForeground(darkFore);
	table.setHeaderBackground(darkHead);
    }

    public void applyToDisplay(final Display display) {
	if (!DARK_THEME || !WIN32) {
	    return;
	}
	display.setData("org.eclipse.swt.internal.win32.useDarkModeExplorerTheme", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.useShellTitleColoring", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.all.use_WS_BORDER", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.menuBarForegroundColor", darkFore);
	display.setData("org.eclipse.swt.internal.win32.menuBarBackgroundColor", darkBack);
	display.setData("org.eclipse.swt.internal.win32.Combo.useDarkTheme", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.Text.useDarkThemeIcons", Boolean.TRUE);
    }

    public void dispose() {
	darkHead.dispose();
	darkBack.dispose();
	darkBack2.dispose();
	darkFore.dispose();
    }

    public Color getDarkBack() {
	return darkBack;
    }

    public Color getDarkBack2() {
	return darkBack2;
    }

    public Color getDarkFore() {
	return darkFore;
    }

    public Color getDarkHead() {
	return darkHead;
    }

    private static boolean getPlatform(final String platform) {
	return platform.equals(SWT.getPlatform());
    }
}
