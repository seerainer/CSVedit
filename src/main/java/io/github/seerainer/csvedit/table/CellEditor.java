package io.github.seerainer.csvedit.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.theme.ThemeManager;

public class CellEditor {

    private final Table table;
    private final CSVTableModel model;
    private final ThemeManager themeManager;
    private Text editingText;

    public CellEditor(final Table table, final CSVTableModel model, final ThemeManager themeManager) {
	this.table = table;
	this.model = model;
	this.themeManager = themeManager;
    }

    private void commitEdit(final TableItem item, final int row, final int col, final String oldValue,
	    final CellEditCallback callback) {
	final var newValue = editingText.getText();
	if (!newValue.equals(oldValue)) {
	    item.setText(col, newValue);
	    model.setValue(row, col, newValue);
	    if (callback != null) {
		callback.onCellEdited(row, col, oldValue, newValue);
	    }
	}
	editingText.dispose();
    }

    public void dispose() {
	if (editingText != null && !editingText.isDisposed()) {
	    editingText.dispose();
	}
    }

    public void editCell(final TableItem item, final int row, final int col, final CellEditCallback callback) {
	if (editingText != null && !editingText.isDisposed()) {
	    editingText.dispose();
	}

	final var oldValue = item.getText(col);

	editingText = new Text(table, SWT.NONE);
	themeManager.applyToControl(editingText);
	editingText.setText(oldValue);

	final var bounds = item.getBounds(col);
	editingText.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	editingText.selectAll();
	editingText.setFocus();

	editingText.addListener(SWT.FocusOut, _ -> {
	    if (!editingText.isDisposed()) {
		commitEdit(item, row, col, oldValue, callback);
	    }
	});

	editingText.addListener(SWT.Traverse, e -> {
	    if (e.detail == SWT.TRAVERSE_RETURN) {
		commitEdit(item, row, col, oldValue, callback);
		e.doit = false;
	    } else if (e.detail == SWT.TRAVERSE_ESCAPE) {
		editingText.dispose();
		e.doit = false;
	    }
	});
    }

    public interface CellEditCallback {
	void onCellEdited(int row, int col, String oldValue, String newValue);
    }
}
