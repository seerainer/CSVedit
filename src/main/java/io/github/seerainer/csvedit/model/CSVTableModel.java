package io.github.seerainer.csvedit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for managing CSV table data. Stores data in a 2D list structure
 * and provides methods for manipulation.
 */
public class CSVTableModel {

    private List<List<String>> data;
    private List<String> headers;

    public CSVTableModel() {
	this.data = new ArrayList<>();
	this.headers = new ArrayList<>();
    }

    /**
     * Adds a new column to all rows
     */
    public void addColumn(final String defaultValue) {
	headers.add("Column " + (headers.size() + 1));
	data.forEach((final List<String> row) -> row.add(defaultValue));
    }

    /**
     * Adds a new row to the table
     */
    public void addRow(final List<String> row) {
	data.add(new ArrayList<>(row));
    }

    /**
     * Clears all data
     */
    public void clear() {
	data.clear();
	headers.clear();
    }

    /**
     * Gets the maximum number of columns across all rows
     */
    public int getColumnCount() {
	var maxCols = headers.size();
	for (final var row : data) {
	    maxCols = Math.max(maxCols, row.size());
	}
	return maxCols;
    }

    /**
     * Gets all data
     */
    public List<List<String>> getData() {
	final List<List<String>> copy = new ArrayList<>();
	data.forEach((final List<String> row) -> copy.add(new ArrayList<>(row)));
	return copy;
    }

    /**
     * Gets a header at the specified index
     */
    public String getHeader(final int index) {
	if (index >= 0 && index < headers.size()) {
	    return headers.get(index);
	}
	return "Column " + (index + 1);
    }

    /**
     * Gets the headers
     */
    public List<String> getHeaders() {
	return new ArrayList<>(headers);
    }

    /**
     * Gets a row at the specified index
     */
    public List<String> getRow(final int index) {
	if (index >= 0 && index < data.size()) {
	    return new ArrayList<>(data.get(index));
	}
	return new ArrayList<>();
    }

    /**
     * Gets the number of rows
     */
    public int getRowCount() {
	return data.size();
    }

    /**
     * Gets a value at the specified row and column
     */
    public String getValue(final int row, final int col) {
	if (row >= 0 && row < data.size()) {
	    final var rowData = data.get(row);
	    if (col >= 0 && col < rowData.size()) {
		return rowData.get(col);
	    }
	}
	return "";
    }

    /**
     * Normalizes all rows to have the same number of columns
     */
    public void normalize() {
	final var maxCols = getColumnCount();
	// Ensure headers match column count
	while (headers.size() < maxCols) {
	    headers.add("Column " + (headers.size() + 1));
	}
	data.forEach((final List<String> row) -> {
	    while (row.size() < maxCols) {
		row.add("");
	    }
	});
    }

    /**
     * Removes a column at the specified index from all rows
     */
    public void removeColumn(final int index) {
	if (index >= 0 && index < headers.size()) {
	    headers.remove(index);
	}
	data.stream().filter((final List<String> row) -> index >= 0 && index < row.size())
		.forEach((final List<String> row) -> row.remove(index));
    }

    /**
     * Removes a row at the specified index
     */
    public void removeRow(final int index) {
	if (index >= 0 && index < data.size()) {
	    data.remove(index);
	}
    }

    /**
     * Sets all data at once
     */
    public void setData(final List<List<String>> newData) {
	this.data = new ArrayList<>();
	newData.forEach((final List<String> row) -> this.data.add(new ArrayList<>(row)));
    }

    /**
     * Sets a header at the specified index
     */
    public void setHeader(final int index, final String header) {
	// Expand headers list if necessary
	while (headers.size() <= index) {
	    headers.add("Column " + (headers.size() + 1));
	}
	headers.set(index, header);
    }

    /**
     * Sets the headers for the table
     */
    public void setHeaders(final List<String> headers) {
	this.headers = new ArrayList<>(headers);
    }

    /**
     * Sets a value at the specified row and column
     */
    public void setValue(final int row, final int col, final String value) {
	if (((row < 0) || (row >= data.size()))) {
	    return;
	}
	final var rowData = data.get(row);
	// Expand row if necessary
	while (rowData.size() <= col) {
	    rowData.add("");
	}
	rowData.set(col, value);
    }
}