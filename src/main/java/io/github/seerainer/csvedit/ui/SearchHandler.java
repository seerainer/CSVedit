package io.github.seerainer.csvedit.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Table;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.model.UndoRedoManager;
import io.github.seerainer.csvedit.ui.dialog.DialogHelper;

/**
 * Handles search and replace operations in the CSV table. Maintains search
 * state and provides methods for finding and replacing text.
 */
public class SearchHandler {

    private final Table table;
    private final CSVTableModel model;
    private final DialogHelper dialogHelper;
    private UndoRedoManager undoRedoManager;

    private int currentSearchRow = -1;
    private int currentSearchCol = -1;

    public SearchHandler(final Table table, final CSVTableModel model, final DialogHelper dialogHelper) {
	this.table = table;
	this.model = model;
	this.dialogHelper = dialogHelper;
    }

    /**
     * Set the undo/redo manager for recording edit actions
     */
    public void setUndoRedoManager(final UndoRedoManager undoRedoManager) {
	this.undoRedoManager = undoRedoManager;
    }

    /**
     * Find the next occurrence of the search text
     */
    public void findNext(final String searchText, final boolean caseSensitive, final boolean useRegex) {
	if (searchText == null || searchText.isEmpty()) {
	    return;
	}

	final var startRow = currentSearchRow >= 0 ? currentSearchRow : 0;
	final var startCol = currentSearchRow >= 0 ? currentSearchCol + 1 : 0;

	// Search from current position to end
	for (var i = startRow; i < model.getRowCount(); i++) {
	    final var row = model.getRow(i);
	    for (var j = (i == startRow ? startCol : 0); j < row.size(); j++) {
		if (matches(row.get(j), searchText, caseSensitive, useRegex)) {
		    currentSearchRow = i;
		    currentSearchCol = j;
		    selectCell(i);
		    return;
		}
	    }
	}

	// Wrap around search
	for (var i = 0; i <= startRow; i++) {
	    final var row = model.getRow(i);
	    for (var j = 0; j < (i == startRow ? startCol : row.size()); j++) {
		if (matches(row.get(j), searchText, caseSensitive, useRegex)) {
		    currentSearchRow = i;
		    currentSearchCol = j;
		    selectCell(i);
		    return;
		}
	    }
	}

	dialogHelper.showInfo("Not Found", "No more matches found.");
    }

    /**
     * Get the current search row
     */
    public int getCurrentSearchRow() {
	return currentSearchRow;
    }

    /**
     * Get the current search column
     */
    public int getCurrentSearchCol() {
	return currentSearchCol;
    }

    private static boolean matches(final String text, final String searchText, final boolean caseSensitive,
	    final boolean useRegex) {
	if (!useRegex) {
	    return caseSensitive ? text.contains(searchText) : text.toLowerCase().contains(searchText.toLowerCase());
	}
	try {
	    final var flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
	    final var pattern = Pattern.compile(searchText, flags);
	    final var matcher = pattern.matcher(text);
	    return matcher.find();
	} catch (final Exception e) {
	    return false;
	}
    }

    private void selectCell(final int row) {
	table.setFocus();
	table.setSelection(row);
	table.showSelection();
    }

    /**
     * Perform a replace operation on the current match and find next
     */
    public void performReplace(final String findTerm, final String replaceTerm, final boolean matchCase,
	    final boolean wholeWord, final Runnable onSearchNext, final Runnable onReplaceCallback) {
	// If we have a current search position, replace it
	if (currentSearchRow >= 0 && currentSearchCol >= 0) {
	    final var currentValue = model.getValue(currentSearchRow, currentSearchCol);
	    var search = findTerm;
	    var value = currentValue;

	    if (!matchCase) {
		search = search.toLowerCase();
		value = value.toLowerCase();
	    }

	    final var matches = wholeWord ? value.equals(search) : value.contains(search);
	    if (matches) {
		// Perform the replacement
		final String newValue;
		if (wholeWord || currentValue.equals(findTerm)) {
		    newValue = replaceTerm;
		} else // Replace all occurrences in the cell
		if (matchCase) {
		    newValue = currentValue.replace(findTerm, replaceTerm);
		} else {
		    newValue = currentValue.replaceAll("(?i)" + Pattern.quote(findTerm),
			    Matcher.quoteReplacement(replaceTerm));
		}

		model.setValue(currentSearchRow, currentSearchCol, newValue);
		if (undoRedoManager != null) {
		    undoRedoManager.recordAction(new UndoRedoManager.EditAction(UndoRedoManager.ActionType.CELL_EDIT,
			    currentSearchRow, currentSearchCol, currentValue, newValue));
		}
		if (onReplaceCallback != null) {
		    onReplaceCallback.run();
		}
	    }
	}

	// Find next occurrence
	if (onSearchNext != null) {
	    onSearchNext.run();
	}
    }

    /**
     * Replace all occurrences of the search term with the replacement term
     */
    public void performReplaceAll(final String findTerm, final String replaceTerm, final boolean matchCase,
	    final boolean wholeWord, final Runnable onReplaceCallback) {
	if (findTerm.isEmpty()) {
	    return;
	}

	var replacementCount = 0;
	final var rowCount = model.getRowCount();
	final var colCount = model.getColumnCount();

	// Iterate through all cells and replace matches
	for (var i = 0; i < rowCount; i++) {
	    for (var j = 0; j < colCount; j++) {
		final var currentValue = model.getValue(i, j);
		var search = findTerm;
		var value = currentValue;

		if (!matchCase) {
		    search = search.toLowerCase();
		    value = value.toLowerCase();
		}

		final var matches = wholeWord ? value.equals(search) : value.contains(search);
		if (matches) {
		    // Perform the replacement
		    final String newValue;
		    if (wholeWord || currentValue.equals(findTerm)) {
			newValue = replaceTerm;
		    } else // Replace all occurrences in the cell
		    if (matchCase) {
			newValue = currentValue.replace(findTerm, replaceTerm);
		    } else {
			newValue = currentValue.replaceAll("(?i)" + Pattern.quote(findTerm),
				Matcher.quoteReplacement(replaceTerm));
		    }

		    if (!currentValue.equals(newValue)) {
			model.setValue(i, j, newValue);
			if (undoRedoManager != null) {
			    undoRedoManager.recordAction(new UndoRedoManager.EditAction(
				    UndoRedoManager.ActionType.CELL_EDIT, i, j, currentValue, newValue));
			}
			replacementCount++;
		    }
		}
	    }
	}

	if (replacementCount > 0) {
	    if (onReplaceCallback != null) {
		onReplaceCallback.run();
	    }
	    dialogHelper.showInfo("Replace All", "Replaced %d occurrence%s."
		    .formatted(Integer.valueOf(replacementCount), replacementCount == 1 ? "" : "s"));
	} else {
	    dialogHelper.showInfo("Replace All", "No occurrences found.");
	}

	// Reset search position
	currentSearchRow = -1;
	currentSearchCol = -1;
    }
}
