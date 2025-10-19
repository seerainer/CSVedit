package io.github.seerainer.csvedit.ui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import io.github.seerainer.csvedit.io.FileOperations;
import io.github.seerainer.csvedit.io.JSONOperations;
import io.github.seerainer.csvedit.io.XMLOperations;
import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.model.UndoRedoManager;
import io.github.seerainer.csvedit.table.CellEditor;
import io.github.seerainer.csvedit.table.TableManager;
import io.github.seerainer.csvedit.theme.ThemeManager;
import io.github.seerainer.csvedit.ui.dialog.AboutDialog;
import io.github.seerainer.csvedit.ui.dialog.DialogHelper;
import io.github.seerainer.csvedit.ui.dialog.DuplicateRowsDialog;
import io.github.seerainer.csvedit.ui.dialog.EditHeaderDialog;
import io.github.seerainer.csvedit.ui.dialog.FindReplaceDialog;
import io.github.seerainer.csvedit.ui.dialog.SettingsDialog;
import io.github.seerainer.csvedit.ui.dialog.TextEditorDialog;
import io.github.seerainer.csvedit.util.PrintHandler;
import io.github.seerainer.csvedit.util.Settings;

public class MainWindow {

    private final Display display;
    private final ThemeManager themeManager;
    private final DialogHelper dialogHelper;
    private Shell shell;
    private Table table;
    private final CSVTableModel model;
    private TableManager tableManager;
    private CellEditor cellEditor;
    private String currentFilePath;
    private boolean isDirty = false;
    private final UndoRedoManager undoRedoManager;
    private FindReplaceDialog findReplaceDialog;
    private MenuItem undoMenuItem;
    private MenuItem redoMenuItem;
    private Label statusLabel;
    private Font tableFont;
    private PrintHandler printHandler;

    private final UndoRedoHandler undoRedoHandler;
    private final SearchHandler searchHandler;
    private final TableSortHandler sortHandler;
    private final StatusBarManager statusBarManager;
    private final FileOperationsHandler fileOperationsHandler;

    public MainWindow(final Display display, final String arg) {
	this.display = display;
	this.model = new CSVTableModel();
	this.undoRedoManager = new UndoRedoManager();
	this.themeManager = new ThemeManager(display);

	themeManager.applyToDisplay(display);
	Icons.initialize(display);
	createShell();
	createMenuBar();
	createTableArea();
	createStatusBar();

	this.dialogHelper = new DialogHelper(shell);
	this.undoRedoHandler = new UndoRedoHandler(model);
	this.searchHandler = new SearchHandler(table, model, dialogHelper);
	this.searchHandler.setUndoRedoManager(undoRedoManager);
	this.sortHandler = new TableSortHandler(table, model, this::refreshTable);
	this.statusBarManager = new StatusBarManager(statusLabel, table, model);
	this.fileOperationsHandler = new FileOperationsHandler(shell, display, table, model, dialogHelper);

	fileOperationsHandler.enableFileDrop(this::confirmDiscardChanges, this::handleFileLoaded);
	themeManager.applyToControl(shell);
	themeManager.applyToControl(table);

	applyTableFont();

	shell.setSize(UIConstants.DEFAULT_WINDOW_WIDTH, UIConstants.DEFAULT_WINDOW_HEIGHT);
	shell.addDisposeListener(_ -> {
	    themeManager.dispose();
	    if (cellEditor != null) {
		cellEditor.dispose();
	    }
	    if (tableFont != null) {
		tableFont.dispose();
	    }
	});
	shell.open();

	if (arg != null && !arg.isBlank()) {
	    openFile(new File(arg));
	}

	if (Settings.getAutoSave()) {
	    setupAutoSave();
	}

	updateUndoRedoMenuItems();
	updateStatusBar();
    }

    private static String getJsonFileName(final String csvPath) {
	final var baseName = new File(csvPath).getName();
	final var dotIndex = baseName.lastIndexOf('.');
	return dotIndex > 0 ? baseName.substring(0, dotIndex) + ".json" : baseName + ".json";
    }

    private static String getXmlFileName(final String csvPath) {
	final var baseName = new File(csvPath).getName();
	final var dotIndex = baseName.lastIndexOf('.');
	return dotIndex > 0 ? baseName.substring(0, dotIndex) + ".xml" : baseName + ".xml";
    }

    private static MenuItem separator(final Menu menu) {
	return new MenuItem(menu, SWT.SEPARATOR);
    }

    private void applyAction(final UndoRedoManager.EditAction action, final boolean isRedo) {
	undoRedoHandler.applyAction(action, isRedo);
    }

    private void applyTableFont() {
	if (tableFont != null) {
	    tableFont.dispose();
	    tableFont = null;
	}

	final var fontData = Settings.getFontData();
	if (fontData == null) {
	    return;
	}
	// Create our own Font instance that we are responsible for disposing
	tableFont = new Font(display, fontData);
	table.setFont(tableFont);
    }

    private boolean confirmDiscardChanges() {
	if (isDirty) {
	    final var result = dialogHelper.showYesNoCancelDialog("Unsaved Changes",
		    "You have unsaved changes. Do you want to save them?");
	    if (result == SWT.YES) {
		handleSave();
		return !isDirty;
	    }
	    if (result == SWT.CANCEL) {
		return false;
	    }
	}
	return true;
    }

    private void createEditMenu(final Menu menuBar) {
	final var editMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	editMenuItem.setText("&Edit");

	final var editMenu = new Menu(shell, SWT.DROP_DOWN);
	editMenuItem.setMenu(editMenu);

	undoMenuItem = new MenuItem(editMenu, SWT.PUSH);
	undoMenuItem.setText("&Undo\tCtrl+Z");
	undoMenuItem.setAccelerator(SWT.MOD1 + 'Z');
	undoMenuItem.addSelectionListener(widgetSelectedAdapter(_ -> handleUndo()));
	undoMenuItem.setEnabled(false);

	redoMenuItem = new MenuItem(editMenu, SWT.PUSH);
	redoMenuItem.setText("&Redo\tCtrl+Y");
	redoMenuItem.setAccelerator(SWT.MOD1 + 'Y');
	redoMenuItem.addSelectionListener(widgetSelectedAdapter(_ -> handleRedo()));
	redoMenuItem.setEnabled(false);

	separator(editMenu);

	final var addRowItem = new MenuItem(editMenu, SWT.PUSH);
	addRowItem.setText("Add &Row\tCtrl+R");
	addRowItem.setAccelerator(SWT.MOD1 + 'R');
	addRowItem.addSelectionListener(widgetSelectedAdapter(_ -> handleAddRow()));

	final var addColumnItem = new MenuItem(editMenu, SWT.PUSH);
	addColumnItem.setText("Add &Column\tCtrl+L");
	addColumnItem.setAccelerator(SWT.MOD1 + 'L');
	addColumnItem.addSelectionListener(widgetSelectedAdapter(_ -> handleAddColumn()));

	separator(editMenu);

	final var deleteRowItem = new MenuItem(editMenu, SWT.PUSH);
	deleteRowItem.setText("&Delete Row\tCtrl+D");
	deleteRowItem.setAccelerator(SWT.MOD1 + 'D');
	deleteRowItem.addSelectionListener(widgetSelectedAdapter(_ -> handleDeleteRow()));

	final var deleteColumnItem = new MenuItem(editMenu, SWT.PUSH);
	deleteColumnItem.setText("Delete Co&lumn");
	deleteColumnItem.addSelectionListener(widgetSelectedAdapter(_ -> handleDeleteColumn()));

	separator(editMenu);

	final var findItem = new MenuItem(editMenu, SWT.PUSH);
	findItem.setText("&Find and Replace...\tCtrl+F");
	findItem.setAccelerator(SWT.MOD1 + 'F');
	findItem.addSelectionListener(widgetSelectedAdapter(_ -> handleFindReplace()));

	separator(editMenu);

	final var removeDuplicatesItem = new MenuItem(editMenu, SWT.PUSH);
	removeDuplicatesItem.setText("Remove D&uplicates...");
	removeDuplicatesItem.addSelectionListener(widgetSelectedAdapter(_ -> handleRemoveDuplicates()));
    }

    private void createFileMenu(final Menu menuBar) {
	final var fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	fileMenuItem.setText("&File");

	final var fileMenu = new Menu(shell, SWT.DROP_DOWN);
	fileMenuItem.setMenu(fileMenu);

	final var newItem = new MenuItem(fileMenu, SWT.PUSH);
	newItem.setText("&New\tCtrl+N");
	newItem.setAccelerator(SWT.MOD1 + 'N');
	newItem.addSelectionListener(widgetSelectedAdapter(_ -> handleNew()));

	final var openItem = new MenuItem(fileMenu, SWT.PUSH);
	openItem.setText("&Open...\tCtrl+O");
	openItem.setAccelerator(SWT.MOD1 + 'O');
	openItem.addSelectionListener(widgetSelectedAdapter(_ -> handleOpen()));

	separator(fileMenu);

	final var saveItem = new MenuItem(fileMenu, SWT.PUSH);
	saveItem.setText("&Save\tCtrl+S");
	saveItem.setAccelerator(SWT.MOD1 + 'S');
	saveItem.addSelectionListener(widgetSelectedAdapter(_ -> handleSave()));

	final var saveAsItem = new MenuItem(fileMenu, SWT.PUSH);
	saveAsItem.setText("Save &As...\tCtrl+Shift+S");
	saveAsItem.setAccelerator(SWT.MOD1 + SWT.SHIFT + 'S');
	saveAsItem.addSelectionListener(widgetSelectedAdapter(_ -> handleSaveAs()));

	separator(fileMenu);

	final var printItem = new MenuItem(fileMenu, SWT.PUSH);
	printItem.setText("&Print...\tCtrl+P");
	printItem.setAccelerator(SWT.MOD1 + 'P');
	printItem.addSelectionListener(widgetSelectedAdapter(_ -> handlePrint()));

	separator(fileMenu);

	final var importMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
	importMenuItem.setText("&Import");

	final var importMenu = new Menu(shell, SWT.DROP_DOWN);
	importMenuItem.setMenu(importMenu);

	final var importJsonItem = new MenuItem(importMenu, SWT.PUSH);
	importJsonItem.setText("From &JSON...");
	importJsonItem.addSelectionListener(widgetSelectedAdapter(_ -> handleImportJSON()));

	final var importXmlItem = new MenuItem(importMenu, SWT.PUSH);
	importXmlItem.setText("From &XML...");
	importXmlItem.addSelectionListener(widgetSelectedAdapter(_ -> handleImportXML()));

	final var exportMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
	exportMenuItem.setText("&Export");

	final var exportMenu = new Menu(shell, SWT.DROP_DOWN);
	exportMenuItem.setMenu(exportMenu);

	final var exportJsonItem = new MenuItem(exportMenu, SWT.PUSH);
	exportJsonItem.setText("To &JSON...");
	exportJsonItem.addSelectionListener(widgetSelectedAdapter(_ -> handleExportJSON()));

	final var exportXmlItem = new MenuItem(exportMenu, SWT.PUSH);
	exportXmlItem.setText("To &XML...");
	exportXmlItem.addSelectionListener(widgetSelectedAdapter(_ -> handleExportXML()));

	separator(fileMenu);

	final var exitItem = new MenuItem(fileMenu, SWT.PUSH);
	exitItem.setText("E&xit");
	exitItem.addSelectionListener(widgetSelectedAdapter(_ -> handleExit()));
    }

    private void createHelpMenu(final Menu menuBar) {
	final var helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	helpMenuItem.setText("&Help");

	final var helpMenu = new Menu(shell, SWT.DROP_DOWN);
	helpMenuItem.setMenu(helpMenu);

	final var aboutItem = new MenuItem(helpMenu, SWT.PUSH);
	aboutItem.setText("&About");
	aboutItem.addSelectionListener(widgetSelectedAdapter(_ -> showAbout()));
    }

    private void createMenuBar() {
	final var menuBar = new Menu(shell, SWT.BAR);
	shell.setMenuBar(menuBar);

	createFileMenu(menuBar);
	createEditMenu(menuBar);
	createViewMenu(menuBar);
	createHelpMenu(menuBar);
    }

    private void createShell() {
	shell = new Shell(display);
	shell.setBackgroundMode(SWT.INHERIT_FORCE);
	shell.setText("CSV Editor");
	shell.setLayout(new GridLayout(1, false));
	shell.setImage(Icons.getImage(Icons.APP_ICON));
    }

    private void createStatusBar() {
	final var statusBar = new Composite(shell, SWT.NONE);
	statusBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	statusBar.setLayout(new GridLayout(1, false));

	statusLabel = new Label(statusBar, SWT.NONE);
	statusLabel.setText("Ready");
	statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	themeManager.applyToControl(statusLabel);
    }

    private void createTableArea() {
	table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
	table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	table.setHeaderVisible(true);
	table.setLinesVisible(Settings.getShowGridLines());

	tableManager = new TableManager(table, model, themeManager);
	cellEditor = new CellEditor(table, model, themeManager);

	createTableContextMenu();
	setupTableListeners();
	enableColumnReordering();

	tableManager.initializeEmptyTable(e -> sortByColumn(getColumnIndex(e)), e -> editHeader(getColumnIndex(e)));
    }

    private void createTableContextMenu() {
	final var contextMenu = new Menu(table);
	table.setMenu(contextMenu);

	final var editCellItem = new MenuItem(contextMenu, SWT.PUSH);
	editCellItem.setText("Edit Cell");
	editCellItem.addSelectionListener(widgetSelectedAdapter(_ -> handleEditCell()));

	separator(contextMenu);

	final var addRowItem = new MenuItem(contextMenu, SWT.PUSH);
	addRowItem.setText("Add Row");
	addRowItem.addSelectionListener(widgetSelectedAdapter(_ -> handleAddRow()));

	final var addColumnItem = new MenuItem(contextMenu, SWT.PUSH);
	addColumnItem.setText("Add Column");
	addColumnItem.addSelectionListener(widgetSelectedAdapter(_ -> handleAddColumn()));

	separator(contextMenu);

	final var deleteRowItem = new MenuItem(contextMenu, SWT.PUSH);
	deleteRowItem.setText("Delete Row");
	deleteRowItem.addSelectionListener(widgetSelectedAdapter(_ -> handleDeleteRow()));

	final var deleteColumnItem = new MenuItem(contextMenu, SWT.PUSH);
	deleteColumnItem.setText("Delete Column");
	deleteColumnItem.addSelectionListener(widgetSelectedAdapter(_ -> handleDeleteColumn()));
    }

    private void createViewMenu(final Menu menuBar) {
	final var viewMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	viewMenuItem.setText("&View");

	final var viewMenu = new Menu(shell, SWT.DROP_DOWN);
	viewMenuItem.setMenu(viewMenu);

	final var refreshItem = new MenuItem(viewMenu, SWT.PUSH);
	refreshItem.setText("&Refresh\tF5");
	refreshItem.setAccelerator(SWT.F5);
	refreshItem.addSelectionListener(widgetSelectedAdapter(_ -> refreshTable()));

	separator(viewMenu);

	final var resizeColumnsItem = new MenuItem(viewMenu, SWT.PUSH);
	resizeColumnsItem.setText("Resize All &Columns");
	resizeColumnsItem.addSelectionListener(widgetSelectedAdapter(_ -> handleResizeAllColumns()));

	separator(viewMenu);

	final var textEditorItem = new MenuItem(viewMenu, SWT.PUSH);
	textEditorItem.setText("&Text Editor...\tCtrl+T");
	textEditorItem.setAccelerator(SWT.MOD1 + 'T');
	textEditorItem.addSelectionListener(widgetSelectedAdapter(_ -> handleTextEditor()));

	separator(viewMenu);

	final var settingsItem = new MenuItem(viewMenu, SWT.PUSH);
	settingsItem.setText("&Settings...");
	settingsItem.addSelectionListener(widgetSelectedAdapter(_ -> handleSettings()));
    }

    private void editHeader(final int columnIndex) {
	if (columnIndex < 0 || columnIndex >= table.getColumnCount()) {
	    return;
	}

	final var column = table.getColumn(columnIndex);
	final var currentText = column.getText();

	final var dialog = new EditHeaderDialog(shell, currentText);
	if (!dialog.open()) {
	    return;
	}
	final var newText = dialog.getNewHeaderText();
	column.setText(newText);
	model.setHeader(columnIndex, newText);
	undoRedoManager.recordAction(new UndoRedoManager.EditAction(UndoRedoManager.ActionType.HEADER_EDIT, columnIndex,
		currentText, newText, true));
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void enableColumnReordering() {
	table.addListener(SWT.Move, event -> {
	    if (event.widget instanceof final TableColumn movedColumn) {
		var newIndex = -1;

		for (var i = 0; i < table.getColumnCount(); i++) {
		    if (table.getColumn(i) == movedColumn) {
			newIndex = i;
			break;
		    }
		}

		final var columnOrder = table.getColumnOrder();

		if (newIndex >= 0 && columnOrder != null && columnOrder.length > 0) {
		    tableManager.reorderColumns(columnOrder);
		    markDirty();
		}
	    }
	});
    }

    private void handleFileLoaded(final String filePath, final boolean imported) {
	if (imported) {
	    currentFilePath = null;
	    isDirty = true;
	} else {
	    currentFilePath = filePath;
	    isDirty = false;
	}
	undoRedoManager.clear();
	updateUndoRedoMenuItems();
	updateTitle();
	refreshTable();
	updateStatusBar();
    }

    private int getColumnIndex(final Event event) {
	return sortHandler.getColumnIndex(event);
    }

    public Shell getShell() {
	return shell;
    }

    private void handleAddColumn() {
	final var columnIndex = table.getColumnCount();
	final var columnName = "Column " + (columnIndex + 1);

	tableManager.addColumn(e -> sortByColumn(getColumnIndex(e)), e -> editHeader(getColumnIndex(e)));

	final List<String> columnData = new ArrayList<>();
	for (var i = 0; i < model.getRowCount(); i++) {
	    columnData.add("");
	}
	undoRedoManager.recordAction(new UndoRedoManager.EditAction(UndoRedoManager.ActionType.COLUMN_ADD, columnIndex,
		columnName, columnData));
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void handleAddRow() {
	final var rowIndex = model.getRowCount();
	final var columnCount = table.getColumnCount();
	final List<String> newRow = new ArrayList<>();
	for (var i = 0; i < columnCount; i++) {
	    newRow.add("");
	}

	tableManager.addRow();
	undoRedoManager
		.recordAction(new UndoRedoManager.EditAction(UndoRedoManager.ActionType.ROW_ADD, rowIndex, newRow));
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void handleDeleteColumn() {
	if ((table.getColumnCount() <= 0) || (Settings.getConfirmDelete() && (dialogHelper
		.showConfirmation("Confirm Delete", "Are you sure you want to delete the last column?") != SWT.YES))) {
	    return;
	}

	final var lastColumnIndex = table.getColumnCount() - 1;

	// Capture column data before deletion for undo
	final var columnHeader = model.getHeader(lastColumnIndex);
	final List<String> columnData = new ArrayList<>();
	for (var i = 0; i < model.getRowCount(); i++) {
	    columnData.add(model.getValue(i, lastColumnIndex));
	}

	tableManager.deleteColumn(lastColumnIndex);

	undoRedoManager.recordAction(new UndoRedoManager.EditAction(UndoRedoManager.ActionType.COLUMN_DELETE,
		lastColumnIndex, columnHeader, columnData));
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void handleDeleteRow() {
	final var selectedIndices = table.getSelectionIndices();
	if (selectedIndices.length == 0) {
	    dialogHelper.showInfo("No selection", "Please select one or more rows to delete.");
	    return;
	}

	if (Settings.getConfirmDelete()) {
	    final var message = selectedIndices.length == 1 ? "Are you sure you want to delete this row?"
		    : "Are you sure you want to delete %d rows?".formatted(Integer.valueOf(selectedIndices.length));
	    if (dialogHelper.showConfirmation("Confirm Delete", message) != SWT.YES) {
		return;
	    }
	}

	// This prevents index shifting issues
	final var sortedIndices = new int[selectedIndices.length];
	System.arraycopy(selectedIndices, 0, sortedIndices, 0, selectedIndices.length);
	Arrays.sort(sortedIndices);

	// Delete from highest index to lowest to avoid index shifting
	for (var i = sortedIndices.length - 1; i >= 0; i--) {
	    final var rowIndex = sortedIndices[i];
	    final var rowData = model.getRow(rowIndex);
	    undoRedoManager.recordAction(
		    new UndoRedoManager.EditAction(UndoRedoManager.ActionType.ROW_DELETE, rowIndex, rowData));
	    tableManager.deleteRow(rowIndex);
	}

	updateUndoRedoMenuItems();
	markDirty();
    }

    private void handleEditCell() {
	final var selectedIndex = table.getSelectionIndex();
	if (selectedIndex < 0) {
	    dialogHelper.showInfo("No selection", "Please select a cell to edit.");
	    return;
	}

	final var item = table.getItem(selectedIndex);
	cellEditor.editCell(item, selectedIndex, 0, this::recordCellEdit);
    }

    private void handleExit() {
	if (!confirmDiscardChanges()) {
	    return;
	}
	Icons.dispose();
	shell.close();
    }

    private void handleExportJSON() {
	final var fileName = currentFilePath != null ? getJsonFileName(currentFilePath) : null;
	var path = dialogHelper.showSaveFileDialog(new String[] { "*.json", "*.*" },
		new String[] { "JSON Files (*.json)", "All Files (*.*)" }, fileName);

	if (path == null) {
	    return;
	}
	if (!path.toLowerCase().endsWith(".json")) {
	    path += ".json";
	}

	try {
	    JSONOperations.saveJSON(path, model);
	} catch (final Exception e) {
	    dialogHelper.showError("Error exporting to JSON", e.getMessage());
	}
    }

    private void handleExportXML() {
	final var fileName = currentFilePath != null ? getXmlFileName(currentFilePath) : null;
	var path = dialogHelper.showSaveFileDialog(new String[] { "*.xml", "*.*" },
		new String[] { "XML Files (*.xml)", "All Files (*.*)" }, fileName);

	if (path == null) {
	    return;
	}
	if (!path.toLowerCase().endsWith(".xml")) {
	    path += ".xml";
	}

	try {
	    XMLOperations.saveXML(path, model);
	} catch (final Exception e) {
	    dialogHelper.showError("Error exporting to XML", e.getMessage());
	}
    }

    private void handleFindReplace() {
	if (findReplaceDialog == null) {
	    findReplaceDialog = new FindReplaceDialog(shell);
	    findReplaceDialog.setFindCallback(this::performSearch);
	    findReplaceDialog.setReplaceCallback(
		    (findTerm, replaceTerm, matchCase, wholeWord) -> searchHandler.performReplace(findTerm, replaceTerm,
			    matchCase, wholeWord, () -> performSearch(findTerm, matchCase, false), () -> {
				updateUndoRedoMenuItems();
				markDirty();
				refreshTable();
			    }));
	    findReplaceDialog.setReplaceAllCallback((findTerm, replaceTerm, matchCase, wholeWord) -> searchHandler
		    .performReplaceAll(findTerm, replaceTerm, matchCase, wholeWord, () -> {
			updateUndoRedoMenuItems();
			markDirty();
			refreshTable();
		    }));
	}
	findReplaceDialog.show();
    }

    private void handleImportJSON() {
	if (!confirmDiscardChanges()) {
	    return;
	}

	final var path = dialogHelper.showOpenFileDialog(new String[] { "*.json", "*.*" },
		new String[] { "JSON Files (*.json)", "All Files (*.*)" });

	if (path != null) {
	    importJSONFile(path);
	}
    }

    private void handleImportXML() {
	if (!confirmDiscardChanges()) {
	    return;
	}

	final var path = dialogHelper.showOpenFileDialog(new String[] { "*.xml", "*.*" },
		new String[] { "XML Files (*.xml)", "All Files (*.*)" });

	if (path != null) {
	    importXMLFile(path);
	}
    }

    private void handleNew() {
	if (!confirmDiscardChanges()) {
	    return;
	}

	table.setRedraw(false);
	try {
	    model.clear();
	    table.removeAll();
	    for (final var col : table.getColumns()) {
		col.dispose();
	    }

	    currentFilePath = null;
	    isDirty = false;
	    undoRedoManager.clear();
	    updateUndoRedoMenuItems();
	    updateTitle();
	    tableManager.initializeEmptyTable(e -> sortByColumn(getColumnIndex(e)), e -> editHeader(getColumnIndex(e)));
	} finally {
	    table.setRedraw(true);
	}
    }

    private void handleOpen() {
	if (!confirmDiscardChanges()) {
	    return;
	}

	final var path = dialogHelper.showOpenFileDialog(new String[] { "*.csv;*.csv.gz", "*.csv", "*.csv.gz", "*.*" },
		new String[] { "CSV Files (*.csv, *.csv.gz)", "CSV Files (*.csv)", "Gzipped CSV Files (*.csv.gz)",
			"All Files (*.*)" });

	if (path == null) {
	    return;
	}

	openFile(new File(path));
    }

    private void handlePrint() {
	if (printHandler == null) {
	    printHandler = new PrintHandler(shell, model);
	}
	try {
	    printHandler.print();
	} catch (final Exception e) {
	    dialogHelper.showError("Print Error", "Failed to print: " + e.getMessage());
	}
    }

    private void handleRedo() {
	final var action = undoRedoManager.redo();
	if (action == null) {
	    return;
	}

	applyAction(action, true);
	refreshTable();
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void handleRemoveDuplicates() {
	final var duplicateDialog = new DuplicateRowsDialog(shell, model);
	duplicateDialog.setCallback(rowIndices -> {
	    if (rowIndices.isEmpty()) {
		return;
	    }

	    // Sort indices in descending order to delete from bottom to top
	    rowIndices.sort(Comparator.comparing(Integer::intValue).reversed());
	    rowIndices.forEach((final var rowIndex) -> {
		final var rowData = model.getRow(rowIndex.intValue());
		undoRedoManager.recordAction(new UndoRedoManager.EditAction(UndoRedoManager.ActionType.ROW_DELETE,
			rowIndex.intValue(), rowData));
		tableManager.deleteRow(rowIndex.intValue());
	    });

	    updateUndoRedoMenuItems();
	    markDirty();
	    refreshTable();

	    dialogHelper.showInfo("Duplicates Removed", "Successfully removed %d duplicate row%s."
		    .formatted(Integer.valueOf(rowIndices.size()), rowIndices.size() == 1 ? "" : "s"));
	});
	duplicateDialog.open();
    }

    private void handleResizeAllColumns() {
	tableManager.resizeAllColumns();
    }

    private void handleSave() {
	if (currentFilePath == null) {
	    handleSaveAs();
	} else {
	    saveToFile(currentFilePath);
	}
    }

    private void handleSaveAs() {
	final var fileName = currentFilePath != null ? new File(currentFilePath).getName() : null;
	var path = dialogHelper.showSaveFileDialog(new String[] { "*.csv;*.csv.gz", "*.csv", "*.csv.gz", "*.*" },
		new String[] { "CSV Files (*.csv, *.csv.gz)", "CSV Files (*.csv)", "Gzipped CSV Files (*.csv.gz)",
			"All Files (*.*)" },
		fileName);

	if (path == null) {
	    return;
	}
	if (!path.toLowerCase().endsWith(".csv") && !path.toLowerCase().endsWith(".csv.gz")) {
	    path += ".csv";
	}
	currentFilePath = path;
	saveToFile(path);
    }

    private void handleSettings() {
	final var dialog = new SettingsDialog(shell);
	final var changed = dialog.open();

	if (!changed) {
	    return;
	}
	table.setLinesVisible(Settings.getShowGridLines());
	applyTableFont();
	dialogHelper.showInfo("Settings Saved", "Settings have been saved. Some changes will take effect "
		+ "when you create a new file or reload the current file.");
    }

    private void handleTextEditor() {
	final var textEditorDialog = new TextEditorDialog(shell, model);
	textEditorDialog.open();

	if (!textEditorDialog.wasModified()) {
	    return;
	}
	markDirty();
	refreshTable();
    }

    private void handleUndo() {
	final var action = undoRedoManager.undo();
	if (action == null) {
	    return;
	}

	applyAction(action, false);
	refreshTable();
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void importJSONFile(final String path) {
	fileOperationsHandler.importJSONFile(path, this::handleFileLoaded);
    }

    private void importXMLFile(final String path) {
	fileOperationsHandler.importXMLFile(path, this::handleFileLoaded);
    }

    private void markDirty() {
	isDirty = true;
	updateTitle();
    }

    private void openFile(final File file) {
	fileOperationsHandler.openFile(file, this::handleFileLoaded);
    }

    private void performSearch(final String searchTerm, final boolean matchCase, final boolean useRegex) {
	searchHandler.findNext(searchTerm, matchCase, useRegex);
    }

    private void recordCellEdit(final int row, final int col, final String oldValue, final String newValue) {
	undoRedoManager.recordAction(
		new UndoRedoManager.EditAction(UndoRedoManager.ActionType.CELL_EDIT, row, col, oldValue, newValue));
	updateUndoRedoMenuItems();
	markDirty();
    }

    private void refreshTable() {
	tableManager.refreshTable(e -> sortByColumn(getColumnIndex(e)), e -> editHeader(getColumnIndex(e)));
	updateStatusBar();
    }

    private void saveToFile(final String path) {
	try {
	    FileOperations.saveCSV(path, model);
	    isDirty = false;
	    updateTitle();
	    updateStatusBar();
	} catch (final Exception e) {
	    dialogHelper.showError("Error saving file", e.getMessage());
	}
    }

    private void setupAutoSave() {
	final Runnable autoSaveTask = new Runnable() {
	    @Override
	    public void run() {
		if (!shell.isDisposed() && isDirty && currentFilePath != null) {
		    try {
			FileOperations.saveCSV(currentFilePath, model);
		    } catch (final Exception e) {
			// Silent failure
		    }
		}
		if (!shell.isDisposed() && Settings.getAutoSave()) {
		    display.timerExec(30000, this);
		}
	    }
	};
	display.timerExec(30000, autoSaveTask);
    }

    private void setupTableListeners() {
	table.addListener(SWT.MouseDoubleClick, event -> {
	    final var item = table.getItem(new Point(event.x, event.y));
	    if (item == null) {
		return;
	    }

	    final var rowIndex = table.indexOf(item);
	    var columnIndex = -1;

	    for (var i = 0; i < table.getColumnCount(); i++) {
		if (item.getBounds(i).contains(event.x, event.y)) {
		    columnIndex = i;
		    break;
		}
	    }

	    if (columnIndex >= 0) {
		cellEditor.editCell(item, rowIndex, columnIndex, this::recordCellEdit);
	    }
	});
    }

    private void showAbout() {
	final var aboutDialog = new AboutDialog(shell);
	aboutDialog.open();
    }

    private void sortByColumn(final int columnIndex) {
	if (!sortHandler.sortByColumn(columnIndex)) {
	    return;
	}
	markDirty();
	updateStatusBar();
    }

    private void updateStatusBar() {
	statusBarManager.updateStatusBar(fileOperationsHandler.getLastLoadTimeMs());
    }

    private void updateTitle() {
	final var title = new StringBuilder("CSV Editor");
	if (currentFilePath != null) {
	    title.append(" - ").append(new File(currentFilePath).getName());
	}
	if (isDirty) {
	    title.append(" *");
	}
	shell.setText(title.toString());
    }

    private void updateUndoRedoMenuItems() {
	if (undoMenuItem != null && !undoMenuItem.isDisposed()) {
	    undoMenuItem.setEnabled(undoRedoManager.canUndo());
	}
	if (redoMenuItem != null && !redoMenuItem.isDisposed()) {
	    redoMenuItem.setEnabled(undoRedoManager.canRedo());
	}
    }
}
