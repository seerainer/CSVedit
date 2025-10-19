package io.github.seerainer.csvedit.ui;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.model.UndoRedoManager;

/**
 * Handles applying undo/redo actions to the CSV table model. This class
 * encapsulates the logic for applying different types of actions.
 */
public class UndoRedoHandler {

    private final CSVTableModel model;

    public UndoRedoHandler(final CSVTableModel model) {
	this.model = model;
    }

    /**
     * Apply an undo/redo action to the model
     *
     * @param action The action to apply
     * @param isRedo true if this is a redo operation, false for undo
     */
    public void applyAction(final UndoRedoManager.EditAction action, final boolean isRedo) {
	switch (action.getType()) {
	case CELL_EDIT ->
	    model.setValue(action.getRow(), action.getCol(), isRedo ? action.getNewValue() : action.getOldValue());
	case ROW_ADD -> {
	    if (isRedo) {
		model.addRow(action.getRowData());
	    } else {
		model.removeRow(action.getRow());
	    }
	}
	case ROW_DELETE -> {
	    if (isRedo) {
		model.removeRow(action.getRow());
	    } else {
		model.addRow(action.getRowData());
	    }
	}
	case COLUMN_ADD -> {
	    if (isRedo) {
		model.addColumn("");
		model.setHeader(action.getCol(), action.getColumnHeader());
		restoreColumnData(action, action.getCol());
	    } else {
		model.removeColumn(action.getCol());
	    }
	}
	case COLUMN_DELETE -> {
	    if (isRedo) {
		model.removeColumn(action.getCol());
	    } else {
		model.addColumn("");
		model.setHeader(action.getCol(), action.getColumnHeader());
		restoreColumnData(action, action.getCol());
	    }
	}
	case HEADER_EDIT -> model.setHeader(action.getCol(), isRedo ? action.getNewValue() : action.getOldValue());
	default -> {
	    // No action
	}
	}
    }

    private void restoreColumnData(final UndoRedoManager.EditAction action, final int colIndex) {
	final var columnData = action.getColumnData();
	if (columnData != null) {
	    for (var i = 0; i < columnData.size() && i < model.getRowCount(); i++) {
		model.setValue(i, colIndex, columnData.get(i));
	    }
	}
    }
}
