package io.github.seerainer.csvedit.ui.dialog;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import io.github.seerainer.csvedit.ui.UIConstants;

/**
 * About dialog displaying application information with animation
 */
public class AboutDialog extends ThemedDialog {

    private static final Random random = new SecureRandom();
    private static final String APP_NAME = "CSV Editor";
    private static final String VERSION = "0.1.2";
    private static final String COPYRIGHT = "© 2025  Philipp Seerainer";
    private static final String DESCRIPTION = "A feature-rich CSV (Comma-Separated Values) editor.";
    private final Display display;
    private Canvas canvas;
    private List<Star> stars;
    private long animationStartTime;
    private Font titleFont;
    private Font normalFont;
    private Font smallFont;
    private boolean animationRunning = true;
    private List<TextElement> textElements;
    private Color blackColor;
    private Color whiteColor;

    public AboutDialog(final Shell parent) {
	super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	this.display = parent.getDisplay();
	createContents();
	centerOnParent();
    }

    private void createContents() {
	shell.setText("About " + APP_NAME);
	shell.setLayout(new GridLayout(1, false));

	// Create reusable colors
	blackColor = new Color(display, 0, 0, 0);
	whiteColor = new Color(display, 255, 255, 255);

	// Initialize stars
	stars = new ArrayList<>();
	for (var i = 0; i < UIConstants.ABOUT_NUM_STARS; i++) {
	    stars.add(new Star(UIConstants.ABOUT_CANVAS_WIDTH, UIConstants.ABOUT_CANVAS_HEIGHT));
	}

	// Initialize text elements with staggered delays
	textElements = new ArrayList<>();
	textElements.add(new TextElement(APP_NAME, UIConstants.TEXT_POS_TITLE, UIConstants.TEXT_ANIMATION_DELAY_TITLE));
	textElements.add(new TextElement("Version " + VERSION, UIConstants.TEXT_POS_VERSION,
		UIConstants.TEXT_ANIMATION_DELAY_VERSION));
	textElements.add(
		new TextElement(COPYRIGHT, UIConstants.TEXT_POS_COPYRIGHT, UIConstants.TEXT_ANIMATION_DELAY_COPYRIGHT));
	textElements.add(new TextElement(DESCRIPTION, UIConstants.TEXT_POS_DESCRIPTION,
		UIConstants.TEXT_ANIMATION_DELAY_DESCRIPTION));
	textElements.add(new TextElement(
		new StringBuilder().append("Built with Java ").append(System.getProperty("java.version"))
			.append(" and SWT").toString(),
		UIConstants.TEXT_POS_BUILD_INFO, UIConstants.TEXT_ANIMATION_DELAY_BUILD_INFO));

	// Create fonts
	createFonts();

	// Create canvas
	canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
	canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	canvas.addPaintListener(e -> paintCanvas(e.gc));

	// Set canvas size
	final var canvasData = (GridData) canvas.getLayoutData();
	canvasData.widthHint = UIConstants.ABOUT_CANVAS_WIDTH;
	canvasData.heightHint = UIConstants.ABOUT_CANVAS_HEIGHT;

	// OK button
	final var buttonComposite = new Composite(shell, SWT.NONE);
	buttonComposite.setLayout(new GridLayout(1, false));
	buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
	applyTheme(buttonComposite);

	final var okButton = new Button(buttonComposite, SWT.PUSH);
	okButton.setText("OK");
	final var buttonData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	buttonData.widthHint = UIConstants.OK_BUTTON_WIDTH;
	okButton.setLayoutData(buttonData);
	okButton.addListener(SWT.Selection, _ -> {
	    animationRunning = false;
	    shell.close();
	});
	applyTheme(okButton);

	shell.setDefaultButton(okButton);
	shell.pack();
	shell.setMinimumSize(UIConstants.ABOUT_CANVAS_WIDTH,
		UIConstants.ABOUT_CANVAS_HEIGHT + UIConstants.ABOUT_MIN_HEIGHT_OFFSET);

	// Add additional disposal for fonts and colors
	shell.addDisposeListener(_ -> {
	    disposeFonts();
	    disposeColors();
	});

	animationStartTime = System.currentTimeMillis();
    }

    private void createFonts() {
	// Title font (large and bold)
	var fontData = display.getSystemFont().getFontData();
	fontData[0].setHeight(UIConstants.FONT_SIZE_TITLE);
	fontData[0].setStyle(SWT.BOLD);
	titleFont = new Font(display, fontData[0]);

	// Normal font
	fontData = display.getSystemFont().getFontData();
	fontData[0].setHeight(UIConstants.FONT_SIZE_NORMAL);
	normalFont = new Font(display, fontData[0]);

	// Small font
	fontData = display.getSystemFont().getFontData();
	fontData[0].setHeight(UIConstants.FONT_SIZE_SMALL);
	fontData[0].setStyle(SWT.ITALIC);
	smallFont = new Font(display, fontData[0]);
    }

    private void disposeColors() {
	if (blackColor != null && !blackColor.isDisposed()) {
	    blackColor.dispose();
	}
	if (whiteColor != null && !whiteColor.isDisposed()) {
	    whiteColor.dispose();
	}
    }

    private void disposeFonts() {
	if (titleFont != null && !titleFont.isDisposed()) {
	    titleFont.dispose();
	}
	if (normalFont != null && !normalFont.isDisposed()) {
	    normalFont.dispose();
	}
	if (smallFont != null && !smallFont.isDisposed()) {
	    smallFont.dispose();
	}
    }

    public void open() {
	shell.open();
	startAnimation();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
    }

    private void paintCanvas(final GC gc) {
	final var bounds = canvas.getBounds();

	// Black background - use reusable color
	gc.setBackground(blackColor);
	gc.fillRectangle(0, 0, bounds.width, bounds.height);

	// Draw stars
	stars.forEach((final Star star) -> {
	    final var color = new Color(star.brightness, star.brightness, star.brightness);
	    gc.setBackground(color);
	    gc.fillOval((int) star.x, (int) star.y, star.size, star.size);
	    color.dispose();
	});

	// Update animation state
	final var elapsed = System.currentTimeMillis() - animationStartTime;

	// Update and draw text elements - use reusable white color
	gc.setForeground(whiteColor);
	for (var i = 0; i < textElements.size(); i++) {
	    final var elem = textElements.get(i);

	    // Calculate animation progress
	    final var elemTime = elapsed - elem.delay;
	    if (elemTime > 0) {
		final var progress = Math.min(1.0f, elemTime / (float) UIConstants.ANIMATION_FADE_DURATION_MS);
		elem.alpha = progress;
		elem.yOffset = UIConstants.ANIMATION_Y_OFFSET * (1.0f - progress);
	    }

	    if (elem.alpha > 0) {
		switch (i) {
		case 0 -> gc.setFont(titleFont);
		case 3, 4 -> gc.setFont(smallFont);
		default -> gc.setFont(normalFont);
		}

		// Calculate alpha color
		final var alpha = (int) (elem.alpha * 255);
		final var textColor = new Color(255, 255, 255, alpha);
		gc.setForeground(textColor);

		// Draw text centered
		final var textSize = gc.textExtent(elem.text);
		final var x = (bounds.width - textSize.x) / 2;
		final var y = elem.targetY + (int) elem.yOffset;

		gc.drawText(elem.text, x, y, true);
		textColor.dispose();
	    }
	}
    }

    private void startAnimation() {
	final Runnable animator = new Runnable() {
	    @Override
	    public void run() {
		if (!animationRunning || canvas.isDisposed()) {
		    return;
		}

		// Update stars
		stars.forEach((final Star star) -> star.update(UIConstants.ABOUT_CANVAS_WIDTH,
			UIConstants.ABOUT_CANVAS_HEIGHT));

		// Redraw canvas
		if (canvas.isDisposed()) {
		    return;
		}
		canvas.redraw();
		display.timerExec(UIConstants.ABOUT_ANIMATION_DELAY_MS, this);
	    }
	};

	display.timerExec(UIConstants.ABOUT_ANIMATION_DELAY_MS, animator);
    }

    // Star class for animated stars
    private static class Star {
	float x;
	float y;
	float speedX;
	float speedY;
	int size;
	int brightness;

	Star(final int width, final int height) {
	    this.x = random.nextFloat() * width;
	    this.y = random.nextFloat() * height;
	    // this.speedX = (random.nextFloat() - 0.5f) * 1.0f;
	    this.speedY = random.nextFloat() * (UIConstants.STAR_SPEED_MAX - UIConstants.STAR_SPEED_MIN)
		    + UIConstants.STAR_SPEED_MIN;
	    this.size = random.nextInt(UIConstants.STAR_SIZE_VARIANCE) + UIConstants.STAR_SIZE_BASE;
	    this.brightness = random.nextInt(UIConstants.STAR_BRIGHTNESS_RANGE) + UIConstants.STAR_BRIGHTNESS_MIN;
	}

	public void update(final int width, final int height) {
	    x += speedX;
	    y += speedY;

	    // Wrap around horizontally
	    if (x < 0) {
		x = width;
	    }
	    if (x > width) {
		x = 0;
	    }
	    // Reset to top when falling off bottom
	    if (y <= height) {
		return;
	    }
	    y = 0;
	    x = random.nextFloat() * width;
	}
    }

    // Text animation parameters
    private static class TextElement {
	String text;
	int targetY;
	float alpha;
	float yOffset;
	int delay;

	TextElement(final String text, final int targetY, final int delay) {
	    this.text = text;
	    this.targetY = targetY;
	    this.delay = delay;
	    this.alpha = 0f;
	    this.yOffset = UIConstants.ANIMATION_Y_OFFSET;
	}
    }
}
