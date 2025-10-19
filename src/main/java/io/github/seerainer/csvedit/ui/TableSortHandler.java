package io.github.seerainer.csvedit.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import io.github.seerainer.csvedit.model.CSVTableModel;

/**
 * Handles table sorting operations. Maintains sort state and provides sorting
 * functionality.
 */
public class TableSortHandler {

    private final Table table;
    private final CSVTableModel model;
    private final Runnable refreshCallback;

    private int lastSortColumn = -1;
    private int lastSortDirection = SWT.NONE;

    public TableSortHandler(final Table table, final CSVTableModel model, final Runnable refreshCallback) {
	this.table = table;
	this.model = model;
	this.refreshCallback = refreshCallback;
    }

    /**
     * Sort the table by the specified column
     */
    public boolean sortByColumn(final int columnIndex) {
	if (columnIndex < 0 || columnIndex >= table.getColumnCount() || model.getRowCount() < 2) {
	    return false;
	}

	final var sortDirection = determineSortDirection(columnIndex);
	final var rows = collectRows();

	// rows.sort(
	// Comparator.comparing(row -> row, (row1, row2) -> compareRows(row1, row2,
	// columnIndex, sortDirection)));
	rows.sort((r1, r2) -> compareRows(r1, r2, columnIndex, sortDirection));

	model.setData(rows);
	refreshCallback.run();

	updateSortIndicator(columnIndex, sortDirection);

	lastSortColumn = columnIndex;
	lastSortDirection = sortDirection;
	return true;
    }

    /**
     * Get the column index from an event
     */
    public int getColumnIndex(final Event event) {
	if (event.widget instanceof final TableColumn column) {
	    for (var i = 0; i < table.getColumnCount(); i++) {
		if (table.getColumn(i) == column) {
		    return i;
		}
	    }
	}
	return -1;
    }

    private List<List<String>> collectRows() {
	final var rows = new ArrayList<List<String>>();
	for (var i = 0; i < model.getRowCount(); i++) {
	    rows.add(model.getRow(i));
	}
	return rows;
    }

    private static int compareRows(final List<String> row1, final List<String> row2, final int columnIndex,
	    final int sortDirection) {
	final var val1 = columnIndex < row1.size() ? row1.get(columnIndex) : "";
	final var val2 = columnIndex < row2.size() ? row2.get(columnIndex) : "";

	int result;
	try {
	    final var num1 = Double.parseDouble(val1);
	    final var num2 = Double.parseDouble(val2);
	    result = Double.compare(num1, num2);
	} catch (final NumberFormatException e) {
	    result = val1.compareToIgnoreCase(val2);
	}

	return sortDirection == SWT.DOWN ? -result : result;
    }

    private int determineSortDirection(final int columnIndex) {
	if (lastSortColumn == columnIndex && lastSortDirection == SWT.UP) {
	    return SWT.DOWN;
	}
	return SWT.UP;
    }

    private void updateSortIndicator(final int columnIndex, final int sortDirection) {
	final var sortedColumn = table.getColumn(columnIndex);
	table.setSortColumn(sortedColumn);
	table.setSortDirection(sortDirection);
    }
}
