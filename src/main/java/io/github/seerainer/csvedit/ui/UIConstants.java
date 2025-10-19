package io.github.seerainer.csvedit.ui;

/**
 * UI constants for the CSV Editor application. Contains hardcoded values for
 * window dimensions, performance tuning, and UI spacing.
 */
public final class UIConstants {

    // Window dimensions
    public static final int DEFAULT_WINDOW_WIDTH = 800;
    public static final int DEFAULT_WINDOW_HEIGHT = 600;
    public static final int MIN_FIND_REPLACE_WIDTH = 450;
    public static final int SETTINGS_DIALOG_WIDTH = 700;
    public static final int SETTINGS_DIALOG_HEIGHT = 600;
    public static final int DUPLICATE_DIALOG_WIDTH = 700;
    public static final int DUPLICATE_DIALOG_HEIGHT = 500;
    public static final int PROGRESS_DIALOG_WIDTH = 500;
    public static final int PROGRESS_DIALOG_HEIGHT = 200;

    // CSV loading performance tuning
    public static final int CSV_PREVIEW_ROWS = 100;
    public static final long PROGRESS_UPDATE_INTERVAL = 1000;
    public static final long LARGE_FILE_THRESHOLD_BYTES = 10 * 1024 * 1024; // 10MB

    // Undo/Redo management
    public static final int MAX_UNDO_STACK_SIZE = 1000;

    // UI spacing and padding
    public static final int COLUMN_INDEX_WIDTH = 60;
    public static final int DEFAULT_COLUMN_WIDTH = 100;
    public static final int OK_BUTTON_WIDTH = 100;

    // About dialog animation
    public static final int ABOUT_CANVAS_WIDTH = 500;
    public static final int ABOUT_CANVAS_HEIGHT = 400;
    public static final int ABOUT_NUM_STARS = 100;
    public static final int ABOUT_ANIMATION_DELAY_MS = 15;
    public static final int ABOUT_MIN_HEIGHT_OFFSET = 100;

    // Text element animation delays (milliseconds)
    public static final int TEXT_ANIMATION_DELAY_TITLE = 400;
    public static final int TEXT_ANIMATION_DELAY_VERSION = 800;
    public static final int TEXT_ANIMATION_DELAY_COPYRIGHT = 1200;
    public static final int TEXT_ANIMATION_DELAY_DESCRIPTION = 1600;
    public static final int TEXT_ANIMATION_DELAY_BUILD_INFO = 2000;

    // Text element Y positions
    public static final int TEXT_POS_TITLE = 80;
    public static final int TEXT_POS_VERSION = 140;
    public static final int TEXT_POS_COPYRIGHT = 200;
    public static final int TEXT_POS_DESCRIPTION = 250;
    public static final int TEXT_POS_BUILD_INFO = 300;

    // Font sizes
    public static final int FONT_SIZE_TITLE = 26;
    public static final int FONT_SIZE_NORMAL = 16;
    public static final int FONT_SIZE_SMALL = 12;

    // Star animation
    public static final float STAR_SPEED_MIN = 1.0f;
    public static final float STAR_SPEED_MAX = 3.0f;
    public static final int STAR_SIZE_BASE = 1;
    public static final int STAR_SIZE_VARIANCE = 2;
    public static final int STAR_BRIGHTNESS_MIN = 100;
    public static final int STAR_BRIGHTNESS_RANGE = 156; // MAX - MIN

    // Animation timing
    public static final int ANIMATION_FADE_DURATION_MS = 1000;
    public static final float ANIMATION_Y_OFFSET = 50f;
    public static final int PROGRESS_AUTO_CLOSE_DELAY_MS = 500;

    private UIConstants() {
	throw new IllegalStateException("Utility class - do not instantiate");
    }
}
