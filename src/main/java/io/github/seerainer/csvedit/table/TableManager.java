package io.github.seerainer.csvedit.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.theme.ThemeManager;
import io.github.seerainer.csvedit.util.Settings;

public class TableManager {

    private final Table table;
    private final CSVTableModel model;
    private final ThemeManager themeManager;

    public TableManager(final Table table, final CSVTableModel model, final ThemeManager themeManager) {
	this.table = table;
	this.model = model;
	this.themeManager = themeManager;
    }

    public void addColumn(final Listener sortListener, final Listener editHeaderListener) {
	final var columnName = "Column " + (table.getColumnCount() + 1);
	final var column = new TableColumn(table, SWT.NONE);
	column.setText(columnName);
	column.setWidth(Settings.getColumnWidth());
	column.setMoveable(true);

	table.getColumnCount();
	if (editHeaderListener != null) {
	    column.addListener(SWT.DefaultSelection, editHeaderListener::handleEvent);
	}
	if (sortListener != null) {
	    column.addListener(SWT.Selection, sortListener::handleEvent);
	}

	model.addColumn("");

	for (final var item : table.getItems()) {
	    item.setText(table.getColumnCount() - 1, "");
	}
    }

    public void addRow() {
	final var columnCount = table.getColumnCount();
	final List<String> newRow = new ArrayList<>();
	for (var i = 0; i < columnCount; i++) {
	    newRow.add("");
	}

	model.addRow(newRow);
	final var item = new TableItem(table, SWT.NONE);
	for (var i = 0; i < columnCount; i++) {
	    item.setText(i, "");
	}

	// Apply alternating row background
	final var rowIndex = table.getItemCount() - 1;
	if (rowIndex % 2 == 0) {
	    item.setBackground(themeManager.getEvenRow());
	}
    }

    public void deleteColumn(final int columnIndex) {
	if (((columnIndex < 0) || (columnIndex >= table.getColumnCount()))) {
	    return;
	}
	table.getColumn(columnIndex).dispose();
	model.removeColumn(columnIndex);
    }

    public void deleteRow(final int rowIndex) {
	if (((rowIndex < 0) || (rowIndex >= table.getItemCount()))) {
	    return;
	}
	model.removeRow(rowIndex);
	table.remove(rowIndex);
    }

    public boolean moveRow(final int fromIndex, final int toIndex) {
	if (fromIndex < 0 || fromIndex >= table.getItemCount() || toIndex < 0 || toIndex >= table.getItemCount()
		|| fromIndex == toIndex) {
	    return false;
	}

	// Move in model
	if (!model.moveRow(fromIndex, toIndex)) {
	    return false;
	}

	table.setRedraw(false);
	try {
	    // Move in UI
	    final var items = table.getItems();
	    final var fromItem = items[fromIndex];

	    // Save the data from the source row
	    final var columnCount = table.getColumnCount();
	    final var rowData = new String[columnCount];
	    for (var i = 0; i < columnCount; i++) {
		rowData[i] = fromItem.getText(i);
	    }
	    final var background = fromItem.getBackground();

	    // Remove the source row
	    table.remove(fromIndex);

	    // Create new item at target position
	    final var newItem = new TableItem(table, SWT.NONE, toIndex);
	    for (var i = 0; i < columnCount; i++) {
		newItem.setText(i, rowData[i]);
	    }
	    newItem.setBackground(background);

	    // Reselect the moved row
	    table.setSelection(toIndex);

	    return true;
	} finally {
	    table.setRedraw(true);
	}
    }

    public boolean moveRowUp(final int currentIndex) {
	if (currentIndex <= 0) {
	    return false;
	}
	return moveRow(currentIndex, currentIndex - 1);
    }

    public boolean moveRowDown(final int currentIndex) {
	if (currentIndex < 0 || currentIndex >= table.getItemCount() - 1) {
	    return false;
	}
	return moveRow(currentIndex, currentIndex + 1);
    }

    public boolean moveRowToFirst(final int currentIndex) {
	// Move in model
	if (currentIndex <= 0 || currentIndex >= table.getItemCount() || !model.moveRowToFirst(currentIndex)) {
	    return false;
	}

	table.setRedraw(false);
	try {
	    // Move in UI
	    final var items = table.getItems();
	    final var fromItem = items[currentIndex];

	    // Save the data from the source row
	    final var columnCount = table.getColumnCount();
	    final var rowData = new String[columnCount];
	    for (var i = 0; i < columnCount; i++) {
		rowData[i] = fromItem.getText(i);
	    }
	    final var background = fromItem.getBackground();

	    // Remove the source row
	    table.remove(currentIndex);

	    // Create new item at first position
	    final var newItem = new TableItem(table, SWT.NONE, 0);
	    for (var i = 0; i < columnCount; i++) {
		newItem.setText(i, rowData[i]);
	    }
	    newItem.setBackground(background);

	    // Reselect the moved row
	    table.setSelection(0);

	    return true;
	} finally {
	    table.setRedraw(true);
	}
    }

    public boolean moveRowToLast(final int currentIndex) {
	// Move in model
	if (currentIndex < 0 || currentIndex >= table.getItemCount() - 1 || !model.moveRowToLast(currentIndex)) {
	    return false;
	}

	table.setRedraw(false);
	try {
	    // Move in UI
	    final var items = table.getItems();
	    final var fromItem = items[currentIndex];

	    // Save the data from the source row
	    final var columnCount = table.getColumnCount();
	    final var rowData = new String[columnCount];
	    for (var i = 0; i < columnCount; i++) {
		rowData[i] = fromItem.getText(i);
	    }
	    final var background = fromItem.getBackground();

	    // Remove the source row
	    table.remove(currentIndex);

	    // Create new item at last position
	    final var lastIndex = table.getItemCount();
	    final var newItem = new TableItem(table, SWT.NONE, lastIndex);
	    for (var i = 0; i < columnCount; i++) {
		newItem.setText(i, rowData[i]);
	    }
	    newItem.setBackground(background);

	    // Reselect the moved row
	    table.setSelection(lastIndex);

	    return true;
	} finally {
	    table.setRedraw(true);
	}
    }

    public void initializeEmptyTable(final Listener sortListener, final Listener editHeaderListener) {
	final var defaultColumns = Settings.getDefaultColumns();
	final var defaultRows = Settings.getDefaultRows();
	final var columnWidth = Settings.getColumnWidth();

	for (var i = 0; i < defaultColumns; i++) {
	    final var column = new TableColumn(table, SWT.NONE);
	    final var columnName = "Column " + (i + 1);
	    column.setText(columnName);
	    column.setWidth(columnWidth);
	    column.setMoveable(true);

	    model.setHeader(i, columnName);

	    if (sortListener != null) {
		column.addListener(SWT.Selection, sortListener::handleEvent);
	    }
	    if (editHeaderListener != null) {
		column.addListener(SWT.DefaultSelection, editHeaderListener::handleEvent);
	    }
	}

	for (var i = 0; i < defaultRows; i++) {
	    final List<String> newRow = new ArrayList<>();
	    for (var j = 0; j < defaultColumns; j++) {
		newRow.add("");
	    }
	    model.addRow(newRow);
	    final var item = new TableItem(table, SWT.NONE);
	    for (var j = 0; j < defaultColumns; j++) {
		item.setText(j, "");
	    }

	    // Apply alternating row background
	    if (i % 2 == 0) {
		item.setBackground(themeManager.getEvenRow());
	    }
	}
    }

    public void refreshTable(final Listener sortListener, final Listener editHeaderListener) {
	table.setRedraw(false);
	try {
	    // Save the current column order before removing columns
	    final var savedColumnOrder = table.getColumnCount() > 0 ? table.getColumnOrder() : null;

	    table.removeAll();

	    for (final var col : table.getColumns()) {
		col.dispose();
	    }

	    final var columnCount = model.getColumnCount();
	    final var columnWidth = Settings.getColumnWidth();
	    for (var i = 0; i < columnCount; i++) {
		final var column = new TableColumn(table, SWT.NONE);
		column.setText(model.getHeader(i));
		column.setWidth(columnWidth);
		column.setMoveable(true);

		if (sortListener != null) {
		    column.addListener(SWT.Selection, sortListener::handleEvent);
		}
		if (editHeaderListener != null) {
		    column.addListener(SWT.DefaultSelection, editHeaderListener::handleEvent);
		}
	    }

	    // Restore the column order if it was saved and is still valid
	    if (savedColumnOrder != null && savedColumnOrder.length == columnCount) {
		table.setColumnOrder(savedColumnOrder);
	    }

	    // Batch create table items for better performance
	    final var rowCount = model.getRowCount();
	    final var items = new TableItem[rowCount];
	    for (var i = 0; i < rowCount; i++) {
		items[i] = new TableItem(table, SWT.NONE);
	    }

	    // Now populate the items
	    for (var i = 0; i < rowCount; i++) {
		final var row = model.getRow(i);
		for (var j = 0; j < row.size(); j++) {
		    items[i].setText(j, row.get(j));
		}

		// Apply alternating row background
		if (i % 2 == 0) {
		    items[i].setBackground(themeManager.getEvenRow());
		}
	    }
	} finally {
	    table.setRedraw(true);
	}
    }

    public void reorderColumns(final int[] columnOrder) {
	if (columnOrder == null || columnOrder.length == 0) {
	    return;
	}

	final var oldHeaders = model.getHeaders();
	final List<String> newHeaders = new ArrayList<>();
	for (final var index : columnOrder) {
	    if (index < oldHeaders.size()) {
		newHeaders.add(oldHeaders.get(index));
	    }
	}

	final List<List<String>> newData = new ArrayList<>();
	for (var i = 0; i < model.getRowCount(); i++) {
	    final var oldRow = model.getRow(i);
	    final List<String> newRow = new ArrayList<>();
	    for (final var index : columnOrder) {
		if (index < oldRow.size()) {
		    newRow.add(oldRow.get(index));
		} else {
		    newRow.add("");
		}
	    }
	    newData.add(newRow);
	}

	model.setHeaders(newHeaders);
	model.setData(newData);
    }

    public void resizeAllColumns() {
	for (final var column : table.getColumns()) {
	    column.pack();
	}
    }
}