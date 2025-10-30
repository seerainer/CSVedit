package io.github.seerainer.csvedit.util;

import java.util.prefs.Preferences;

import org.eclipse.swt.graphics.FontData;

/**
 * Application settings management using Java Preferences API
 */
public class Settings {

    private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);

    // CSV Configuration
    private static final String KEY_DELIMITER = "csv.delimiter";
    private static final String KEY_QUOTE = "csv.quote";
    private static final String KEY_ESCAPE = "csv.escape";
    private static final String KEY_TRIM_WHITESPACE = "csv.trimWhitespace";
    private static final String KEY_DETECT_BOM = "csv.detectBOM";
    private static final String KEY_INITIAL_BUFFER_SIZE = "csv.initialBufferSize";
    private static final String KEY_MAX_FIELD_SIZE = "csv.maxFieldSize";

    // CSV Parsing Options
    private static final String KEY_PRESERVE_EMPTY_FIELDS = "csv.preserveEmptyFields";
    private static final String KEY_SKIP_EMPTY_LINES = "csv.skipEmptyLines";
    private static final String KEY_SKIP_BLANK_LINES = "csv.skipBlankLines";
    private static final String KEY_NULL_VALUE_REPRESENTATION = "csv.nullValueRepresentation";
    private static final String KEY_CONVERT_EMPTY_TO_NULL = "csv.convertEmptyToNull";
    private static final String KEY_STRICT_QUOTING = "csv.strictQuoting";
    private static final String KEY_ALLOW_UNESCAPED_QUOTES = "csv.allowUnescapedQuotes";
    private static final String KEY_NORMALIZE_LINE_ENDINGS = "csv.normalizeLineEndings";
    private static final String KEY_MAX_RECORD_LENGTH = "csv.maxRecordLength";
    private static final String KEY_FAIL_ON_MALFORMED_RECORD = "csv.failOnMalformedRecord";
    private static final String KEY_TRACK_FIELD_POSITIONS = "csv.trackFieldPositions";

    // UI Options
    private static final String KEY_DEFAULT_ROWS = "ui.defaultRows";
    private static final String KEY_DEFAULT_COLUMNS = "ui.defaultColumns";
    private static final String KEY_COLUMN_WIDTH = "ui.columnWidth";
    private static final String KEY_SHOW_GRID_LINES = "ui.showGridLines";
    private static final String KEY_AUTO_SAVE = "ui.autoSave";
    private static final String KEY_CONFIRM_DELETE = "ui.confirmDelete";

    // File Options
    private static final String KEY_ENCODING = "file.encoding";
    private static final String KEY_LINE_ENDING = "file.lineEnding";

    // Font Options
    private static final String KEY_FONT_NAME = "ui.fontName";
    private static final String KEY_FONT_HEIGHT = "ui.fontHeight";
    private static final String KEY_FONT_STYLE = "ui.fontStyle";

    private Settings() {
	throw new IllegalStateException("Utility class");
    }

    public static boolean getAllowUnescapedQuotes() {
	return prefs.getBoolean(KEY_ALLOW_UNESCAPED_QUOTES, false);
    }

    public static boolean getAutoSave() {
	return prefs.getBoolean(KEY_AUTO_SAVE, false);
    }

    public static int getColumnWidth() {
	return prefs.getInt(KEY_COLUMN_WIDTH, 150);
    }

    public static boolean getConfirmDelete() {
	return prefs.getBoolean(KEY_CONFIRM_DELETE, true);
    }

    public static boolean getConvertEmptyToNull() {
	return prefs.getBoolean(KEY_CONVERT_EMPTY_TO_NULL, false);
    }

    public static int getDefaultColumns() {
	return prefs.getInt(KEY_DEFAULT_COLUMNS, 5);
    }

    public static int getDefaultRows() {
	return prefs.getInt(KEY_DEFAULT_ROWS, 0);
    }

    public static char getDelimiter() {
	return prefs.get(KEY_DELIMITER, ",").charAt(0);
    }

    public static boolean getDetectBOM() {
	return prefs.getBoolean(KEY_DETECT_BOM, true);
    }

    public static String getEncoding() {
	return prefs.get(KEY_ENCODING, "UTF-8");
    }

    public static char getEscape() {
	return prefs.get(KEY_ESCAPE, "\"").charAt(0);
    }

    public static boolean getFailOnMalformedRecord() {
	return prefs.getBoolean(KEY_FAIL_ON_MALFORMED_RECORD, false);
    }

    /**
     * Get the configured font data. Returns null if no font is configured. Caller
     * should create their own Font instance from this data and dispose it.
     */
    public static FontData getFontData() {
	final var fontName = getFontName();
	final var fontHeight = getFontHeight();
	final var fontStyle = getFontStyle();

	try {
	    return new FontData(fontName, fontHeight, fontStyle);
	} catch (final Exception e) {
	    return null;
	}
    }

    public static int getFontHeight() {
	return prefs.getInt(KEY_FONT_HEIGHT, 10);
    }

    public static String getFontName() {
	return prefs.get(KEY_FONT_NAME, "Arial");
    }

    public static int getFontStyle() {
	return prefs.getInt(KEY_FONT_STYLE, 0);
    }

    public static int getInitialBufferSize() {
	return prefs.getInt(KEY_INITIAL_BUFFER_SIZE, 8192);
    }

    public static String getLineEnding() {
	return prefs.get(KEY_LINE_ENDING, "System");
    }

    public static int getMaxFieldSize() {
	return prefs.getInt(KEY_MAX_FIELD_SIZE, 1024 * 1024);
    }

    public static int getMaxRecordLength() {
	return prefs.getInt(KEY_MAX_RECORD_LENGTH, Integer.MAX_VALUE);
    }

    public static boolean getNormalizeLineEndings() {
	return prefs.getBoolean(KEY_NORMALIZE_LINE_ENDINGS, true);
    }

    public static String getNullValueRepresentation() {
	return prefs.get(KEY_NULL_VALUE_REPRESENTATION, "");
    }

    public static boolean getPreserveEmptyFields() {
	return prefs.getBoolean(KEY_PRESERVE_EMPTY_FIELDS, true);
    }

    public static char getQuote() {
	return prefs.get(KEY_QUOTE, "\"").charAt(0);
    }

    public static boolean getShowGridLines() {
	return prefs.getBoolean(KEY_SHOW_GRID_LINES, true);
    }

    public static boolean getSkipBlankLines() {
	return prefs.getBoolean(KEY_SKIP_BLANK_LINES, false);
    }

    public static boolean getSkipEmptyLines() {
	return prefs.getBoolean(KEY_SKIP_EMPTY_LINES, false);
    }

    public static boolean getStrictQuoting() {
	return prefs.getBoolean(KEY_STRICT_QUOTING, true);
    }

    public static boolean getTrackFieldPositions() {
	return prefs.getBoolean(KEY_TRACK_FIELD_POSITIONS, false);
    }

    public static boolean getTrimWhitespace() {
	return prefs.getBoolean(KEY_TRIM_WHITESPACE, false);
    }

    /**
     * Reset all settings to defaults
     */
    public static void resetToDefaults() {
	try {
	    prefs.clear();
	} catch (final Exception e) {
	    // Ignore exceptions during reset
	}
    }

    public static void setAllowUnescapedQuotes(final boolean allow) {
	prefs.putBoolean(KEY_ALLOW_UNESCAPED_QUOTES, allow);
    }

    public static void setAutoSave(final boolean autoSave) {
	prefs.putBoolean(KEY_AUTO_SAVE, autoSave);
    }

    public static void setColumnWidth(final int width) {
	prefs.putInt(KEY_COLUMN_WIDTH, width);
    }

    public static void setConfirmDelete(final boolean confirm) {
	prefs.putBoolean(KEY_CONFIRM_DELETE, confirm);
    }

    public static void setConvertEmptyToNull(final boolean convert) {
	prefs.putBoolean(KEY_CONVERT_EMPTY_TO_NULL, convert);
    }

    public static void setDefaultColumns(final int columns) {
	prefs.putInt(KEY_DEFAULT_COLUMNS, columns);
    }

    public static void setDefaultRows(final int rows) {
	prefs.putInt(KEY_DEFAULT_ROWS, rows);
    }

    public static void setDelimiter(final char delimiter) {
	prefs.put(KEY_DELIMITER, String.valueOf(delimiter));
    }

    public static void setDetectBOM(final boolean detect) {
	prefs.putBoolean(KEY_DETECT_BOM, detect);
    }

    public static void setEncoding(final String encoding) {
	prefs.put(KEY_ENCODING, encoding);
    }

    public static void setEscape(final char escape) {
	prefs.put(KEY_ESCAPE, String.valueOf(escape));
    }

    public static void setFailOnMalformedRecord(final boolean fail) {
	prefs.putBoolean(KEY_FAIL_ON_MALFORMED_RECORD, fail);
    }

    /**
     * Set the font data to persist.
     */
    public static void setFontData(final FontData fontData) {
	if (fontData == null) {
	    return;
	}
	setFontName(fontData.getName());
	setFontHeight(fontData.getHeight());
	setFontStyle(fontData.getStyle());
    }

    public static void setFontHeight(final int fontHeight) {
	prefs.putInt(KEY_FONT_HEIGHT, fontHeight);
    }

    public static void setFontName(final String fontName) {
	prefs.put(KEY_FONT_NAME, fontName);
    }

    public static void setFontStyle(final int fontStyle) {
	prefs.putInt(KEY_FONT_STYLE, fontStyle);
    }

    public static void setInitialBufferSize(final int size) {
	prefs.putInt(KEY_INITIAL_BUFFER_SIZE, size);
    }

    public static void setLineEnding(final String lineEnding) {
	prefs.put(KEY_LINE_ENDING, lineEnding);
    }

    public static void setMaxFieldSize(final int size) {
	prefs.putInt(KEY_MAX_FIELD_SIZE, size);
    }

    public static void setMaxRecordLength(final int maxLength) {
	prefs.putInt(KEY_MAX_RECORD_LENGTH, maxLength);
    }

    public static void setNormalizeLineEndings(final boolean normalize) {
	prefs.putBoolean(KEY_NORMALIZE_LINE_ENDINGS, normalize);
    }

    public static void setNullValueRepresentation(final String nullRep) {
	prefs.put(KEY_NULL_VALUE_REPRESENTATION, nullRep);
    }

    public static void setPreserveEmptyFields(final boolean preserve) {
	prefs.putBoolean(KEY_PRESERVE_EMPTY_FIELDS, preserve);
    }

    public static void setQuote(final char quote) {
	prefs.put(KEY_QUOTE, String.valueOf(quote));
    }

    public static void setShowGridLines(final boolean show) {
	prefs.putBoolean(KEY_SHOW_GRID_LINES, show);
    }

    public static void setSkipBlankLines(final boolean skip) {
	prefs.putBoolean(KEY_SKIP_BLANK_LINES, skip);
    }

    public static void setSkipEmptyLines(final boolean skip) {
	prefs.putBoolean(KEY_SKIP_EMPTY_LINES, skip);
    }

    public static void setStrictQuoting(final boolean strict) {
	prefs.putBoolean(KEY_STRICT_QUOTING, strict);
    }

    public static void setTrackFieldPositions(final boolean track) {
	prefs.putBoolean(KEY_TRACK_FIELD_POSITIONS, track);
    }

    public static void setTrimWhitespace(final boolean trim) {
	prefs.putBoolean(KEY_TRIM_WHITESPACE, trim);
    }
}