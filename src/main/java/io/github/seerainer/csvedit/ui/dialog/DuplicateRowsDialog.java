package io.github.seerainer.csvedit.ui.dialog;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.ui.Icons;
import io.github.seerainer.csvedit.ui.UIConstants;

/**
 * Dialog for detecting and removing duplicate rows
 */
public class DuplicateRowsDialog extends ThemedDialog {

    private final CSVTableModel model;
    private final DialogHelper dialogHelper;
    private Table duplicatesTable;
    private Label statusLabel;
    private final List<Integer> duplicateRowIndices = new ArrayList<>();
    private DuplicateCallback callback;

    public DuplicateRowsDialog(final Shell parent, final CSVTableModel model) {
	super(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	this.model = model;
	this.dialogHelper = new DialogHelper(parent);
	createContents();
	centerOnParent();
    }

    private void createContents() {
	shell.setText("Find Duplicate Rows");
	shell.setLayout(new GridLayout(1, false));
	shell.setSize(UIConstants.DUPLICATE_DIALOG_WIDTH, UIConstants.DUPLICATE_DIALOG_HEIGHT);
	shell.setImage(Icons.getImage(Icons.APP_ICON));

	// Status label
	statusLabel = new Label(shell, SWT.NONE);
	statusLabel.setText("Click 'Scan for Duplicates' to find duplicate rows");
	statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(statusLabel);

	// Table to show duplicates
	duplicatesTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	duplicatesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	duplicatesTable.setHeaderVisible(true);
	duplicatesTable.setLinesVisible(true);
	applyTheme(duplicatesTable);

	// Buttons
	final var buttonBar = new Composite(shell, SWT.NONE);
	buttonBar.setLayout(new GridLayout(4, false));
	buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(buttonBar);

	final var scanButton = new Button(buttonBar, SWT.PUSH);
	scanButton.setText("Scan for Duplicates");
	scanButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(scanButton);
	scanButton.addSelectionListener(widgetSelectedAdapter(_ -> scanForDuplicates()));

	final var removeSelectedButton = new Button(buttonBar, SWT.PUSH);
	removeSelectedButton.setText("Remove Selected");
	removeSelectedButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(removeSelectedButton);
	removeSelectedButton.addSelectionListener(widgetSelectedAdapter(_ -> removeSelectedDuplicates()));

	final var removeAllButton = new Button(buttonBar, SWT.PUSH);
	removeAllButton.setText("Remove All Duplicates");
	removeAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(removeAllButton);
	removeAllButton.addSelectionListener(widgetSelectedAdapter(_ -> removeAllDuplicates()));

	final var closeButton = new Button(buttonBar, SWT.PUSH);
	closeButton.setText("Close");
	closeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	applyTheme(closeButton);
	closeButton.addSelectionListener(widgetSelectedAdapter(_ -> shell.close()));
    }

    public void open() {
	shell.open();
	scanForDuplicates(); // Auto-scan on open
	final var display = shell.getDisplay();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
    }

    private void removeAllDuplicates() {
	if (duplicateRowIndices.isEmpty()) {
	    dialogHelper.showInfo("No Duplicates", "No duplicate rows to remove. Click 'Scan for Duplicates' first.");
	    return;
	}

	final var message = "Remove all %d duplicate row%s?".formatted(Integer.valueOf(duplicateRowIndices.size()),
		duplicateRowIndices.size() == 1 ? "" : "s");

	if (dialogHelper.showConfirmation("Confirm Removal", message) != SWT.YES) {
	    return;
	}

	if (callback != null) {
	    callback.onRemoveDuplicates(new ArrayList<>(duplicateRowIndices));
	}

	// Rescan after removal
	scanForDuplicates();
    }

    private void removeSelectedDuplicates() {
	final var selectedIndices = duplicatesTable.getSelectionIndices();
	if (selectedIndices.length == 0) {
	    dialogHelper.showInfo("No Selection", "Please select rows to remove from the list.");
	    return;
	}

	final var message = selectedIndices.length == 1 ? "Remove this duplicate row?"
		: "Remove %d duplicate rows?".formatted(Integer.valueOf(selectedIndices.length));

	if (dialogHelper.showConfirmation("Confirm Removal", message) != SWT.YES) {
	    return;
	}

	// Get the actual row indices to remove
	final List<Integer> rowsToRemove = new ArrayList<>();
	for (final var idx : selectedIndices) {
	    final var item = duplicatesTable.getItem(idx);
	    final var rowNum = Integer.parseInt(item.getText(0)) - 1; // Convert back to 0-based
	    rowsToRemove.add(Integer.valueOf(rowNum));
	}

	if (callback != null) {
	    callback.onRemoveDuplicates(rowsToRemove);
	}

	// Rescan after removal
	scanForDuplicates();
    }

    private void scanForDuplicates() {
	duplicateRowIndices.clear();
	duplicatesTable.removeAll();
	for (final var col : duplicatesTable.getColumns()) {
	    col.dispose();
	}

	if (model.getRowCount() == 0) {
	    statusLabel.setText("No data to scan");
	    return;
	}

	// Create row index column
	final var indexColumn = new TableColumn(duplicatesTable, SWT.NONE);
	indexColumn.setText("Row #");
	indexColumn.setWidth(UIConstants.COLUMN_INDEX_WIDTH);

	// Create columns matching the model
	for (var i = 0; i < model.getColumnCount(); i++) {
	    final var column = new TableColumn(duplicatesTable, SWT.NONE);
	    column.setText(model.getHeader(i));
	    column.setWidth(UIConstants.DEFAULT_COLUMN_WIDTH);
	}

	// Find duplicates
	final Map<String, List<Integer>> rowMap = new HashMap<>();

	for (var i = 0; i < model.getRowCount(); i++) {
	    final var row = model.getRow(i);
	    final var rowKey = String.join("|", row);

	    rowMap.computeIfAbsent(rowKey, _ -> new ArrayList<>()).add(Integer.valueOf(i));
	}

	// Collect duplicate rows (keeping first occurrence, marking subsequent as
	// duplicates)
	var duplicateCount = 0;
	for (final var entry : rowMap.entrySet()) {
	    final var rowIndices = entry.getValue();
	    if (rowIndices.size() > 1) {
		// Skip the first occurrence, mark the rest as duplicates
		for (var i = 1; i < rowIndices.size(); i++) {
		    final var rowIndex = rowIndices.get(i);
		    duplicateRowIndices.add(rowIndex);
		    duplicateCount++;

		    final var item = new TableItem(duplicatesTable, SWT.NONE);
		    item.setText(0, String.valueOf(rowIndex.intValue() + 1)); // Display as 1-based

		    final var row = model.getRow(rowIndex.intValue());
		    for (var j = 0; j < row.size() && j < model.getColumnCount(); j++) {
			item.setText(j + 1, row.get(j));
		    }
		}
	    }
	}

	if (duplicateCount == 0) {
	    statusLabel.setText("No duplicate rows found");
	} else {
	    statusLabel.setText("Found %d duplicate row%s".formatted(Integer.valueOf(duplicateCount),
		    duplicateCount == 1 ? "" : "s"));
	}

	// Auto-resize columns
	for (final var col : duplicatesTable.getColumns()) {
	    col.pack();
	}
    }

    public void setCallback(final DuplicateCallback callback) {
	this.callback = callback;
    }

    public interface DuplicateCallback {
	void onRemoveDuplicates(List<Integer> rowIndices);
    }
}
