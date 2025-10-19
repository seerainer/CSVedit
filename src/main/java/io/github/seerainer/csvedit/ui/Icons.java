package io.github.seerainer.csvedit.ui;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Centralized icon manager with proper resource management. Icons are loaded
 * once and shared across all windows.
 */
public class Icons {
    public static final String APP_ICON = """
    	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
    	""";

    private static Icons instance;

    private final Map<String, Image> imageCache = new HashMap<>();

    private final Display display;

    private Icons(final Display display) {
	this.display = display;
    }

    /**
     * Dispose all cached images. Call this when the application shuts down.
     */
    public static void dispose() {
	if (instance == null) {
	    return;
	}
	instance.disposeAll();
	instance = null;
    }

    /**
     * Get a shared image instance. The image is cached and reused. Do NOT dispose
     * images returned by this method.
     *
     * @param imageKey The image key (e.g., APP_ICON)
     * @return The cached image instance
     */
    public static Image getImage(final String imageKey) {
	if (instance == null) {
	    throw new IllegalStateException("Icons not initialized. Call Icons.initialize() first.");
	}
	return instance.loadImage(imageKey);
    }

    /**
     * Initialize the icon manager. Must be called before using getImage().
     *
     * @param display The display to use for creating images
     */
    public static void initialize(final Display display) {
	if (instance == null) {
	    instance = new Icons(display);
	}
    }

    private void disposeAll() {
	imageCache.values().stream().filter((final var image) -> image != null && !image.isDisposed())
		.forEach(Image::dispose);
	imageCache.clear();
    }

    private Image loadImage(final String imageKey) {
	// Return cached image if available
	if (imageCache.containsKey(imageKey)) {
	    return imageCache.get(imageKey);
	}

	// Load and cache the image
	final var bytes = Base64.getMimeDecoder().decode(imageKey.getBytes(UTF_8));
	final var bais = new ByteArrayInputStream(bytes);
	final var image = new Image(display, bais);
	image.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

	imageCache.put(imageKey, image);
	return image;
    }
}