package io.github.seerainer.csvedit.ui;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import io.github.seerainer.csvedit.model.CSVTableModel;

/**
 * Manages status bar updates. Displays information about row count, column
 * count, selections, and load time.
 */
public class StatusBarManager {

    private final Label statusLabel;
    private final Table table;
    private final CSVTableModel model;

    public StatusBarManager(final Label statusLabel, final Table table, final CSVTableModel model) {
	this.statusLabel = statusLabel;
	this.table = table;
	this.model = model;
    }

    /**
     * Update the status bar with current statistics
     */
    public void updateStatusBar(final long loadTimeMs) {
	final var rowCount = model.getRowCount();
	final var colCount = table.getColumnCount();
	final var selectedCount = table.getSelectionCount();

	final var statusText = new StringBuilder();
	statusText.append("Rows: ").append(rowCount);
	statusText.append(" | Columns: ").append(colCount);
	if (selectedCount > 0) {
	    statusText.append(" | Selected: ").append(selectedCount);
	}
	if (loadTimeMs > 0) {
	    statusText.append(" | Load time: ").append(loadTimeMs).append(" ms");
	}

	statusLabel.setText(statusText.toString());
    }
}
