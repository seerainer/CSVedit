package io.github.seerainer.csvedit.ui.dialog;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import io.github.seerainer.csvedit.theme.ThemeManager;
import io.github.seerainer.csvedit.ui.Icons;
import io.github.seerainer.csvedit.ui.UIConstants;
import io.github.seerainer.csvedit.util.Settings;

/**
 * Settings dialog for configuring application preferences
 */
public class SettingsDialog extends ThemedDialog {

    private boolean settingsChanged = false;
    // CSV Configuration controls
    private Text delimiterText;
    private Text quoteText;
    private Text escapeText;
    private Button trimWhitespaceCheck;
    private Button detectBOMCheck;
    private Spinner initialBufferSizeSpinner;
    private Spinner maxFieldSizeSpinner;
    // CSV Parsing Options controls
    private Button preserveEmptyFieldsCheck;
    private Button skipEmptyLinesCheck;
    private Button skipBlankLinesCheck;
    private Text nullValueRepresentationText;
    private Button convertEmptyToNullCheck;
    private Button strictQuotingCheck;
    private Button allowUnescapedQuotesCheck;
    private Button normalizeLineEndingsCheck;
    private Spinner maxRecordLengthSpinner;
    private Button failOnMalformedRecordCheck;
    private Button trackFieldPositionsCheck;
    // UI Options controls
    private Spinner defaultRowsSpinner;
    private Spinner defaultColumnsSpinner;
    private Spinner columnWidthSpinner;
    private Button showGridLinesCheck;
    private Button autoSaveCheck;
    private Button confirmDeleteCheck;
    // File Options controls
    private Combo encodingCombo;
    private Combo lineEndingCombo;
    // Font Options controls
    private Button selectFontButton;
    private Label fontSampleLabel;
    private FontData selectedFontData;
    private Font previewFont;

    public SettingsDialog(final Shell parent) {
	super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
	createContents();
	centerOnParent();
    }

    private void createAdvancedParsingTab(final CTabFolder tabFolder) {
	final var tabItem = new CTabItem(tabFolder, SWT.NONE);
	tabItem.setText("Advanced Parsing");

	final var composite = new Composite(tabFolder, SWT.NONE);
	composite.setLayout(new GridLayout(1, false));
	applyTheme(composite);
	tabItem.setControl(composite);

	// Quoting options group
	final var quotingGroup = new Group(composite, SWT.NONE);
	quotingGroup.setText("Quoting Options");
	quotingGroup.setLayout(new GridLayout(1, false));
	quotingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(quotingGroup);

	strictQuotingCheck = new Button(quotingGroup, SWT.CHECK);
	strictQuotingCheck.setText("Enforce strict quoting rules");
	strictQuotingCheck.setToolTipText("Apply strict CSV quoting standards");
	applyTheme(strictQuotingCheck);

	allowUnescapedQuotesCheck = new Button(quotingGroup, SWT.CHECK);
	allowUnescapedQuotesCheck.setText("Allow unescaped quotes in fields");
	allowUnescapedQuotesCheck.setToolTipText("Permit quotes within fields without escaping (less strict)");
	applyTheme(allowUnescapedQuotesCheck);

	// Line ending options group
	final var lineEndingGroup = new Group(composite, SWT.NONE);
	lineEndingGroup.setText("Line Ending Options");
	lineEndingGroup.setLayout(new GridLayout(1, false));
	lineEndingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(lineEndingGroup);

	normalizeLineEndingsCheck = new Button(lineEndingGroup, SWT.CHECK);
	normalizeLineEndingsCheck.setText("Normalize line endings to \\n");
	normalizeLineEndingsCheck.setToolTipText("Convert all line ending types (CRLF, CR) to LF");
	applyTheme(normalizeLineEndingsCheck);

	// Record validation group
	final var validationGroup = new Group(composite, SWT.NONE);
	validationGroup.setText("Record Validation");
	validationGroup.setLayout(new GridLayout(2, false));
	validationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(validationGroup);

	final var maxRecordLabel = space(validationGroup);
	maxRecordLabel.setText("Max Record Length (chars):");
	maxRecordLengthSpinner = new Spinner(validationGroup, SWT.BORDER);
	maxRecordLengthSpinner.setMinimum(1024);
	maxRecordLengthSpinner.setMaximum(Settings.getMaxRecordLength());
	maxRecordLengthSpinner.setIncrement(1024);
	maxRecordLengthSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	maxRecordLengthSpinner.setToolTipText("Maximum allowed characters per record");
	applyTheme(maxRecordLengthSpinner);

	space(validationGroup); // Spacer
	failOnMalformedRecordCheck = new Button(validationGroup, SWT.CHECK);
	failOnMalformedRecordCheck.setText("Fail on malformed records");
	failOnMalformedRecordCheck.setToolTipText("Stop parsing when encountering invalid records (vs. skipping them)");
	applyTheme(failOnMalformedRecordCheck);

	// Debugging options group
	final var debugGroup = new Group(composite, SWT.NONE);
	debugGroup.setText("Debugging Options");
	debugGroup.setLayout(new GridLayout(1, false));
	debugGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(debugGroup);

	trackFieldPositionsCheck = new Button(debugGroup, SWT.CHECK);
	trackFieldPositionsCheck.setText("Track field positions");
	trackFieldPositionsCheck
		.setToolTipText("Record column and character positions for each field (may impact performance)");
	applyTheme(trackFieldPositionsCheck);

	// Info label
	final var infoLabel = new Label(composite, SWT.WRAP);
	infoLabel.setText("Advanced options for fine-tuning CSV parsing behavior. "
		+ "Only modify these if you have specific requirements.");
	infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyTheme(infoLabel);
    }

    private void createButtonBar() {
	final var buttonBar = new Composite(shell, SWT.NONE);
	buttonBar.setLayout(new GridLayout(4, false));
	buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(buttonBar);

	// Spacer
	final var spacer = new Label(buttonBar, SWT.NONE);
	spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(spacer);

	// Reset to defaults button
	final var resetButton = new Button(buttonBar, SWT.PUSH);
	resetButton.setText("Reset to Defaults");
	final var buttonData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
	buttonData.widthHint = UIConstants.OK_BUTTON_WIDTH;
	resetButton.setLayoutData(buttonData);
	resetButton.addSelectionListener(widgetSelectedAdapter(_ -> resetToDefaults()));
	applyTheme(resetButton);

	// OK button
	final var okButton = new Button(buttonBar, SWT.PUSH);
	okButton.setText("OK");
	okButton.setLayoutData(buttonData);
	okButton.addSelectionListener(widgetSelectedAdapter(_ -> {
	    saveSettings();
	    settingsChanged = true;
	    shell.close();
	}));
	applyTheme(okButton);

	// Cancel button
	final var cancelButton = new Button(buttonBar, SWT.PUSH);
	cancelButton.setText("Cancel");
	cancelButton.setLayoutData(buttonData);
	cancelButton.addSelectionListener(widgetSelectedAdapter(_ -> shell.close()));
	applyTheme(cancelButton);

	shell.setDefaultButton(okButton);
    }

    private void createContents() {
	shell.setText("Settings");
	shell.setLayout(new GridLayout(1, false));
	shell.setSize(UIConstants.SETTINGS_DIALOG_WIDTH, UIConstants.SETTINGS_DIALOG_HEIGHT);
	shell.setImage(Icons.getImage(Icons.APP_ICON));

	// Create tab folder
	final var tabFolder = new CTabFolder(shell, SWT.BORDER);
	tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyTheme(tabFolder);

	// Apply CTabFolder specific styling for dark mode
	if (ThemeManager.isDarkTheme()) {
	    // Selected tab colors
	    tabFolder.setSelectionBackground(themeManager.getDarkBack());
	    tabFolder.setSelectionForeground(themeManager.getDarkFore());
	    // Unselected tab colors
	    tabFolder.setBackground(themeManager.getDarkBack());
	    tabFolder.setForeground(themeManager.getDarkFore());
	    // Tab area background
	    final var gradientColors = new Color[] { themeManager.getDarkBack(), themeManager.getDarkBack() };
	    final var gradientPercents = new int[] { 100 };
	    tabFolder.setSelectionBackground(gradientColors, gradientPercents, true);
	    // Border color
	    tabFolder.setBorderVisible(true);
	}

	createCSVConfigTab(tabFolder);
	createParsingOptionsTab(tabFolder);
	createAdvancedParsingTab(tabFolder);
	createUIOptionsTab(tabFolder);
	createFileOptionsTab(tabFolder);
	createFontOptionsTab(tabFolder);

	tabFolder.setSelection(0);

	// Button bar
	createButtonBar();

	// Load current settings
	loadSettings();
    }

    private void createCSVConfigTab(final CTabFolder tabFolder) {
	final var tabItem = new CTabItem(tabFolder, SWT.NONE);
	tabItem.setText("CSV Configuration");

	final var composite = new Composite(tabFolder, SWT.NONE);
	composite.setLayout(new GridLayout(1, false));
	applyTheme(composite);
	tabItem.setControl(composite);

	final var group = new Group(composite, SWT.NONE);
	group.setText("CSV Format Settings");
	group.setLayout(new GridLayout(2, false));
	group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(group);

	// Delimiter
	final var delimiterLabel = space(group);
	delimiterLabel.setText("Field Delimiter:");
	delimiterText = new Text(group, SWT.BORDER);
	delimiterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	delimiterText.setTextLimit(1);
	delimiterText.setToolTipText("Character used to separate fields (usually comma)");
	applyTheme(delimiterText);

	// Quote character
	final var quoteLabel = space(group);
	quoteLabel.setText("Quote Character:");
	quoteText = new Text(group, SWT.BORDER);
	quoteText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	quoteText.setTextLimit(1);
	quoteText.setToolTipText("Character used to quote fields containing special characters");
	applyTheme(quoteText);

	// Escape character
	final var escapeLabel = space(group);
	escapeLabel.setText("Escape Character:");
	escapeText = new Text(group, SWT.BORDER);
	escapeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	escapeText.setTextLimit(1);
	escapeText.setToolTipText("Character used to escape quotes within quoted fields (usually same as quote)");
	applyTheme(escapeText);

	// Trim whitespace
	space(group); // Spacer
	trimWhitespaceCheck = new Button(group, SWT.CHECK);
	trimWhitespaceCheck.setText("Trim whitespace from fields");
	trimWhitespaceCheck.setToolTipText("Remove leading and trailing whitespace from field values");
	applyTheme(trimWhitespaceCheck);

	// Detect BOM
	space(group); // Spacer
	detectBOMCheck = new Button(group, SWT.CHECK);
	detectBOMCheck.setText("Detect BOM (Byte Order Mark)");
	detectBOMCheck.setToolTipText("Automatically detect and handle UTF-8 BOM at file start");
	applyTheme(detectBOMCheck);

	// Performance settings group
	final var perfGroup = new Group(composite, SWT.NONE);
	perfGroup.setText("Performance Settings");
	perfGroup.setLayout(new GridLayout(2, false));
	perfGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(perfGroup);

	// Initial buffer size
	final var bufferLabel = space(perfGroup);
	bufferLabel.setText("Initial Buffer Size (bytes):");
	initialBufferSizeSpinner = new Spinner(perfGroup, SWT.BORDER);
	initialBufferSizeSpinner.setMinimum(128);
	initialBufferSizeSpinner.setMaximum(65536);
	initialBufferSizeSpinner.setIncrement(128);
	initialBufferSizeSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	initialBufferSizeSpinner.setToolTipText("Starting size for internal buffers (default: 1024)");
	applyTheme(initialBufferSizeSpinner);

	// Max field size
	final var maxFieldLabel = space(perfGroup);
	maxFieldLabel.setText("Max Field Size (bytes):");
	maxFieldSizeSpinner = new Spinner(perfGroup, SWT.BORDER);
	maxFieldSizeSpinner.setMinimum(1024);
	maxFieldSizeSpinner.setMaximum(Settings.getMaxFieldSize());
	maxFieldSizeSpinner.setIncrement(1024);
	maxFieldSizeSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	maxFieldSizeSpinner.setToolTipText("Maximum allowed size for a single field (default: 1MB)");
	applyTheme(maxFieldSizeSpinner);

	// Info label
	final var infoLabel = new Label(composite, SWT.WRAP);
	infoLabel.setText("Note: These settings affect how CSV files are parsed and saved. "
		+ "Changes will apply to newly opened files.");
	infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyTheme(infoLabel);
    }

    private void createFileOptionsTab(final CTabFolder tabFolder) {
	final var tabItem = new CTabItem(tabFolder, SWT.NONE);
	tabItem.setText("File Options");

	final var composite = new Composite(tabFolder, SWT.NONE);
	composite.setLayout(new GridLayout(1, false));
	applyTheme(composite);
	tabItem.setControl(composite);

	final var group = new Group(composite, SWT.NONE);
	group.setText("File Encoding & Format");
	group.setLayout(new GridLayout(2, false));
	group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(group);

	// Encoding
	final var encodingLabel = space(group);
	encodingLabel.setText("Character Encoding:");
	encodingCombo = new Combo(group, SWT.READ_ONLY);
	encodingCombo.setItems("UTF-8", "UTF-16", "UTF-32", "ISO-8859-1", "Windows-1252", "US-ASCII");
	// encodingCombo.setItems(Charset.availableCharsets().keySet().toArray(new
	// String[0]));
	encodingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	encodingCombo.setToolTipText("Character encoding for reading and writing files");
	applyTheme(encodingCombo);

	// Line ending
	final var lineEndingLabel = space(group);
	lineEndingLabel.setText("Line Ending:");
	lineEndingCombo = new Combo(group, SWT.READ_ONLY);
	lineEndingCombo.setItems("System", "Windows (CRLF)", "Unix (LF)", "Mac (CR)");
	lineEndingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	lineEndingCombo.setToolTipText("Line ending style for saved files");
	applyTheme(lineEndingCombo);

	// Info label
	final var infoLabel = new Label(composite, SWT.WRAP);
	infoLabel.setText("UTF-8 is recommended for maximum compatibility. "
		+ "System line endings will use your operating system's default.");
	infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyTheme(infoLabel);
    }

    private void createFontOptionsTab(final CTabFolder tabFolder) {
	final var tabItem = new CTabItem(tabFolder, SWT.NONE);
	tabItem.setText("Font Options");

	final var composite = new Composite(tabFolder, SWT.NONE);
	composite.setLayout(new GridLayout(1, false));
	applyTheme(composite);
	tabItem.setControl(composite);

	// Font selection group
	final var fontGroup = new Group(composite, SWT.NONE);
	fontGroup.setText("Select Font");
	fontGroup.setLayout(new GridLayout(2, false));
	fontGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(fontGroup);

	// Sample text label - save as field
	fontSampleLabel = new Label(fontGroup, SWT.NONE);
	fontSampleLabel.setText("Sample Text: The quick brown fox jumps over the lazy dog");
	fontSampleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(fontSampleLabel);

	// Font selection button
	selectFontButton = new Button(fontGroup, SWT.PUSH);
	selectFontButton.setText("Select Font...");
	selectFontButton.addSelectionListener(widgetSelectedAdapter(_ -> openFontDialog()));
	applyTheme(selectFontButton);

	// Info label
	final var infoLabel = new Label(composite, SWT.WRAP);
	infoLabel.setText("Choose a font for displaying CSV data. " + "This will not affect the actual CSV files.");
	infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyTheme(infoLabel);
    }

    private void createParsingOptionsTab(final CTabFolder tabFolder) {
	final var tabItem = new CTabItem(tabFolder, SWT.NONE);
	tabItem.setText("Parsing Options");

	final var composite = new Composite(tabFolder, SWT.NONE);
	composite.setLayout(new GridLayout(1, false));
	applyTheme(composite);
	tabItem.setControl(composite);

	final var group = new Group(composite, SWT.NONE);
	group.setText("CSV Parsing Behavior");
	group.setLayout(new GridLayout(1, false));
	group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(group);

	preserveEmptyFieldsCheck = new Button(group, SWT.CHECK);
	preserveEmptyFieldsCheck.setText("Preserve empty fields");
	preserveEmptyFieldsCheck.setToolTipText("Keep empty fields in the data (recommended)");
	applyTheme(preserveEmptyFieldsCheck);

	skipEmptyLinesCheck = new Button(group, SWT.CHECK);
	skipEmptyLinesCheck.setText("Skip empty lines");
	skipEmptyLinesCheck.setToolTipText("Ignore lines that contain no data");
	applyTheme(skipEmptyLinesCheck);

	skipBlankLinesCheck = new Button(group, SWT.CHECK);
	skipBlankLinesCheck.setText("Skip blank lines");
	skipBlankLinesCheck.setToolTipText("Ignore lines that are completely blank");
	applyTheme(skipBlankLinesCheck);

	// Null handling group
	final var nullGroup = new Group(composite, SWT.NONE);
	nullGroup.setText("Null Value Handling");
	nullGroup.setLayout(new GridLayout(2, false));
	nullGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(nullGroup);

	final var nullLabel = space(nullGroup);
	nullLabel.setText("Null Value Representation:");
	nullValueRepresentationText = new Text(nullGroup, SWT.BORDER);
	nullValueRepresentationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	nullValueRepresentationText.setToolTipText(
		"Specific string to represent null (e.g., 'NULL', '\\N'). Leave empty for no special handling.");
	applyTheme(nullValueRepresentationText);

	space(nullGroup); // Spacer
	convertEmptyToNullCheck = new Button(nullGroup, SWT.CHECK);
	convertEmptyToNullCheck.setText("Convert empty strings to null");
	convertEmptyToNullCheck.setToolTipText("Treat empty fields as null values");
	applyTheme(convertEmptyToNullCheck);

	// Info label
	final var infoLabel = new Label(composite, SWT.WRAP);
	infoLabel.setText("These options control how the CSV parser handles special cases "
		+ "when reading files. Most users should keep the default settings.");
	infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	applyTheme(infoLabel);
    }

    private void createUIOptionsTab(final CTabFolder tabFolder) {
	final var tabItem = new CTabItem(tabFolder, SWT.NONE);
	tabItem.setText("UI Options");

	final var composite = new Composite(tabFolder, SWT.NONE);
	composite.setLayout(new GridLayout(1, false));
	applyTheme(composite);
	tabItem.setControl(composite);

	// Table defaults group
	final var tableGroup = new Group(composite, SWT.NONE);
	tableGroup.setText("Default Table Size");
	tableGroup.setLayout(new GridLayout(2, false));
	tableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(tableGroup);

	final var rowsLabel = space(tableGroup);
	rowsLabel.setText("Default Rows:");
	defaultRowsSpinner = new Spinner(tableGroup, SWT.BORDER);
	defaultRowsSpinner.setMinimum(0);
	defaultRowsSpinner.setMaximum(1000);
	defaultRowsSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(defaultRowsSpinner);

	final var columnsLabel = space(tableGroup);
	columnsLabel.setText("Default Columns:");
	defaultColumnsSpinner = new Spinner(tableGroup, SWT.BORDER);
	defaultColumnsSpinner.setMinimum(1);
	defaultColumnsSpinner.setMaximum(100);
	defaultColumnsSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(defaultColumnsSpinner);

	final var widthLabel = space(tableGroup);
	widthLabel.setText("Column Width (px):");
	columnWidthSpinner = new Spinner(tableGroup, SWT.BORDER);
	columnWidthSpinner.setMinimum(50);
	columnWidthSpinner.setMaximum(1000);
	columnWidthSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(columnWidthSpinner);

	// Display options group
	final var displayGroup = new Group(composite, SWT.NONE);
	displayGroup.setText("Display Options");
	displayGroup.setLayout(new GridLayout(1, false));
	displayGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(displayGroup);

	showGridLinesCheck = new Button(displayGroup, SWT.CHECK);
	showGridLinesCheck.setText("Show grid lines");
	showGridLinesCheck.setToolTipText("Display grid lines in the table");
	applyTheme(showGridLinesCheck);

	// Behavior options group
	final var behaviorGroup = new Group(composite, SWT.NONE);
	behaviorGroup.setText("Behavior Options");
	behaviorGroup.setLayout(new GridLayout(1, false));
	behaviorGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	applyTheme(behaviorGroup);

	autoSaveCheck = new Button(behaviorGroup, SWT.CHECK);
	autoSaveCheck.setText("Auto-save on file change");
	autoSaveCheck.setToolTipText("Automatically save changes without confirmation");
	applyTheme(autoSaveCheck);

	confirmDeleteCheck = new Button(behaviorGroup, SWT.CHECK);
	confirmDeleteCheck.setText("Confirm before deleting rows/columns");
	confirmDeleteCheck.setToolTipText("Show confirmation dialog before deleting");
	applyTheme(confirmDeleteCheck);
    }

    private void loadSettings() {
	// CSV Configuration
	delimiterText.setText(String.valueOf(Settings.getDelimiter()));
	quoteText.setText(String.valueOf(Settings.getQuote()));
	escapeText.setText(String.valueOf(Settings.getEscape()));
	trimWhitespaceCheck.setSelection(Settings.getTrimWhitespace());
	detectBOMCheck.setSelection(Settings.getDetectBOM());
	initialBufferSizeSpinner.setSelection(Settings.getInitialBufferSize());
	maxFieldSizeSpinner.setSelection(Settings.getMaxFieldSize());

	// Parsing Options
	preserveEmptyFieldsCheck.setSelection(Settings.getPreserveEmptyFields());
	skipEmptyLinesCheck.setSelection(Settings.getSkipEmptyLines());
	skipBlankLinesCheck.setSelection(Settings.getSkipBlankLines());
	nullValueRepresentationText.setText(Settings.getNullValueRepresentation());
	convertEmptyToNullCheck.setSelection(Settings.getConvertEmptyToNull());

	// Advanced Parsing
	strictQuotingCheck.setSelection(Settings.getStrictQuoting());
	allowUnescapedQuotesCheck.setSelection(Settings.getAllowUnescapedQuotes());
	normalizeLineEndingsCheck.setSelection(Settings.getNormalizeLineEndings());
	maxRecordLengthSpinner.setSelection(Settings.getMaxRecordLength());
	failOnMalformedRecordCheck.setSelection(Settings.getFailOnMalformedRecord());
	trackFieldPositionsCheck.setSelection(Settings.getTrackFieldPositions());

	// UI Options
	defaultRowsSpinner.setSelection(Settings.getDefaultRows());
	defaultColumnsSpinner.setSelection(Settings.getDefaultColumns());
	columnWidthSpinner.setSelection(Settings.getColumnWidth());
	showGridLinesCheck.setSelection(Settings.getShowGridLines());
	autoSaveCheck.setSelection(Settings.getAutoSave());
	confirmDeleteCheck.setSelection(Settings.getConfirmDelete());

	// File Options
	encodingCombo.setText(Settings.getEncoding());
	lineEndingCombo.setText(Settings.getLineEnding());

	// Font Options
	selectedFontData = Settings.getFontData();
	if (selectedFontData != null) {
	    // Update font dialog button to show selected font
	    selectFontButton.setText("Font: %s, %d".formatted(selectedFontData.getName(),
		    Integer.valueOf(selectedFontData.getHeight())));

	    // Apply the font to the sample label immediately
	    if (previewFont != null && !previewFont.isDisposed()) {
		previewFont.dispose();
	    }
	    previewFont = new Font(shell.getDisplay(), selectedFontData);
	    fontSampleLabel.setFont(previewFont);
	} else {
	    selectFontButton.setText("Select Font...");
	}
    }

    public boolean open() {
	shell.open();
	final var display = parentShell.getDisplay();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
	return settingsChanged;
    }

    private void openFontDialog() {
	final var fontDialog = new FontDialog(shell);
	fontDialog.setText("Select Font");
	if (selectedFontData != null) {
	    fontDialog.setFontList(new org.eclipse.swt.graphics.FontData[] { selectedFontData });
	}
	final var newFontData = fontDialog.open();
	if (newFontData == null) {
	    return;
	}

	// Dispose old preview font if it exists
	if (previewFont != null && !previewFont.isDisposed()) {
	    previewFont.dispose();
	}

	// Store FontData instead of Font
	selectedFontData = newFontData;
	previewFont = new Font(shell.getDisplay(), newFontData);

	// Update button text to show selected font
	selectFontButton
		.setText("Font: %s, %d".formatted(newFontData.getName(), Integer.valueOf(newFontData.getHeight())));

	// Apply the font to the sample label
	fontSampleLabel.setFont(previewFont);
	fontSampleLabel.getParent().layout(true);
    }

    private void resetToDefaults() {
	Settings.resetToDefaults();
	loadSettings();
    }

    private void saveSettings() {
	// CSV Configuration
	if (!delimiterText.getText().isEmpty()) {
	    Settings.setDelimiter(delimiterText.getText().charAt(0));
	}
	if (!quoteText.getText().isEmpty()) {
	    Settings.setQuote(quoteText.getText().charAt(0));
	}
	if (!escapeText.getText().isEmpty()) {
	    Settings.setEscape(escapeText.getText().charAt(0));
	}
	Settings.setTrimWhitespace(trimWhitespaceCheck.getSelection());
	Settings.setDetectBOM(detectBOMCheck.getSelection());
	Settings.setInitialBufferSize(initialBufferSizeSpinner.getSelection());
	Settings.setMaxFieldSize(maxFieldSizeSpinner.getSelection());

	// Parsing Options
	Settings.setPreserveEmptyFields(preserveEmptyFieldsCheck.getSelection());
	Settings.setSkipEmptyLines(skipEmptyLinesCheck.getSelection());
	Settings.setSkipBlankLines(skipBlankLinesCheck.getSelection());
	Settings.setNullValueRepresentation(nullValueRepresentationText.getText());
	Settings.setConvertEmptyToNull(convertEmptyToNullCheck.getSelection());

	// Advanced Parsing
	Settings.setStrictQuoting(strictQuotingCheck.getSelection());
	Settings.setAllowUnescapedQuotes(allowUnescapedQuotesCheck.getSelection());
	Settings.setNormalizeLineEndings(normalizeLineEndingsCheck.getSelection());
	Settings.setMaxRecordLength(maxRecordLengthSpinner.getSelection());
	Settings.setFailOnMalformedRecord(failOnMalformedRecordCheck.getSelection());
	Settings.setTrackFieldPositions(trackFieldPositionsCheck.getSelection());

	// UI Options
	Settings.setDefaultRows(defaultRowsSpinner.getSelection());
	Settings.setDefaultColumns(defaultColumnsSpinner.getSelection());
	Settings.setColumnWidth(columnWidthSpinner.getSelection());
	Settings.setShowGridLines(showGridLinesCheck.getSelection());
	Settings.setAutoSave(autoSaveCheck.getSelection());
	Settings.setConfirmDelete(confirmDeleteCheck.getSelection());

	// File Options
	Settings.setEncoding(encodingCombo.getText());
	Settings.setLineEnding(lineEndingCombo.getText());

	// Font Options
	Settings.setFontData(selectedFontData);
    }

    private Label space(final Group group) {
	final var label = new Label(group, SWT.NONE);
	applyTheme(label);
	return label;
    }
}
