package io.github.seerainer.csvedit.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import io.github.seerainer.csvedit.ui.UIConstants;

/**
 * Manages undo/redo operations for the CSV editor
 */
public class UndoRedoManager {

    private final Deque<EditAction> redoStack = new ArrayDeque<>();
    private final Deque<EditAction> undoStack = new ArrayDeque<>();

    /**
     * Check if redo is available
     */
    public boolean canRedo() {
	return !redoStack.isEmpty();
    }

    /**
     * Check if undo is available
     */
    public boolean canUndo() {
	return !undoStack.isEmpty();
    }

    /**
     * Clear all undo/redo history
     */
    public void clear() {
	undoStack.clear();
	redoStack.clear();
    }

    /**
     * Records an action that can be undone
     */
    public void recordAction(final EditAction action) {
	undoStack.push(action);
	redoStack.clear(); // Clear redo stack when new action is performed

	// Limit stack size to prevent memory issues
	if (undoStack.size() > UIConstants.MAX_UNDO_STACK_SIZE) {
	    undoStack.removeLast();
	}
    }

    /**
     * Redo the last undone action
     *
     * @return the action that was redone, or null if nothing to redo
     */
    public EditAction redo() {
	if (redoStack.isEmpty()) {
	    return null;
	}
	final var action = redoStack.pop();
	undoStack.push(action);
	return action;
    }

    /**
     * Undo the last action
     *
     * @return the action that was undone, or null if nothing to undo
     */
    public EditAction undo() {
	if (undoStack.isEmpty()) {
	    return null;
	}
	final var action = undoStack.pop();
	redoStack.push(action);
	return action;
    }

    public enum ActionType {
	CELL_EDIT, ROW_ADD, ROW_DELETE, COLUMN_ADD, COLUMN_DELETE, HEADER_EDIT
    }

    /**
     * Represents an editable action
     */
    public static class EditAction {
	private final ActionType type;
	private final int row;
	private final int col;
	private final String oldValue;
	private final String newValue;
	private final List<String> rowData;
	private final String columnHeader;
	private final List<String> columnData;

	// Cell edit action
	public EditAction(final ActionType type, final int row, final int col, final String oldValue,
		final String newValue) {
	    this.type = type;
	    this.row = row;
	    this.col = col;
	    this.oldValue = oldValue;
	    this.newValue = newValue;
	    this.rowData = null;
	    this.columnHeader = null;
	    this.columnData = null;
	}

	// Row add/delete action
	public EditAction(final ActionType type, final int row, final List<String> rowData) {
	    this.type = type;
	    this.row = row;
	    this.col = -1;
	    this.oldValue = null;
	    this.newValue = null;
	    this.rowData = new ArrayList<>(rowData);
	    this.columnHeader = null;
	    this.columnData = null;
	}

	// Column add/delete action
	public EditAction(final ActionType type, final int col, final String columnHeader,
		final List<String> columnData) {
	    this.type = type;
	    this.row = -1;
	    this.col = col;
	    this.oldValue = null;
	    this.newValue = null;
	    this.rowData = null;
	    this.columnHeader = columnHeader;
	    this.columnData = columnData != null ? new ArrayList<>(columnData) : null;
	}

	/**
	 * @param isHeaderEdit dummy parameter to differentiate constructor
	 */
	public EditAction(final int col, final String oldHeader, final String newHeader, final boolean isHeaderEdit) {
	    this.type = ActionType.HEADER_EDIT;
	    this.row = -1;
	    this.col = col;
	    this.oldValue = oldHeader;
	    this.newValue = newHeader;
	    this.rowData = null;
	    this.columnHeader = null;
	    this.columnData = null;
	}

	public int getCol() {
	    return col;
	}

	public String getColumnHeader() {
	    return columnHeader;
	}

	public List<String> getColumnData() {
	    return columnData != null ? new ArrayList<>(columnData) : null;
	}

	public String getNewValue() {
	    return newValue;
	}

	public String getOldValue() {
	    return oldValue;
	}

	public int getRow() {
	    return row;
	}

	public List<String> getRowData() {
	    return rowData != null ? new ArrayList<>(rowData) : null;
	}

	public ActionType getType() {
	    return type;
	}
    }
}