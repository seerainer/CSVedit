package io.github.seerainer.csvedit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.model.UndoRedoManager;
import io.github.seerainer.csvedit.ui.UndoRedoHandler;

/**
 * Tests for row moving functionality in CSV Editor
 */
@Tag("unit")
class RowMoveTest {

    private CSVTableModel model;
    private UndoRedoManager undoRedoManager;
    private UndoRedoHandler handler;

    @BeforeEach
    void setUp() {
	model = new CSVTableModel();
	undoRedoManager = new UndoRedoManager();
	handler = new UndoRedoHandler(model);

	// Initialize with test data
	model.setHeaders(Arrays.asList("Name", "Age", "City"));
	model.addRow(Arrays.asList("Alice", "30", "New York"));
	model.addRow(Arrays.asList("Bob", "25", "London"));
	model.addRow(Arrays.asList("Charlie", "35", "Paris"));
	model.addRow(Arrays.asList("David", "28", "Tokyo"));
    }

    @Test
    void testMoveRowBasic() {
	// Move row from index 0 to index 2
	final var success = model.moveRow(0, 2);

	assertThat(success).isTrue();
	assertThat(model.getValue(0, 0)).isEqualTo("Bob");
	assertThat(model.getValue(1, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(2, 0)).isEqualTo("Alice");
	assertThat(model.getValue(3, 0)).isEqualTo("David");
    }

    @Test
    void testMoveRowUp() {
	// Move row 2 up to position 1
	final var success = model.moveRow(2, 1);

	assertThat(success).isTrue();
	assertThat(model.getValue(0, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(2, 0)).isEqualTo("Bob");
	assertThat(model.getValue(3, 0)).isEqualTo("David");
    }

    @Test
    void testMoveRowDown() {
	// Move row 1 down to position 2
	final var success = model.moveRow(1, 2);

	assertThat(success).isTrue();
	assertThat(model.getValue(0, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(2, 0)).isEqualTo("Bob");
	assertThat(model.getValue(3, 0)).isEqualTo("David");
    }

    @Test
    void testMoveRowToSamePosition() {
	// Moving to same position should return false
	final var success = model.moveRow(1, 1);

	assertThat(success).isFalse();
	assertThat(model.getValue(0, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 0)).isEqualTo("Bob");
	assertThat(model.getValue(2, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(3, 0)).isEqualTo("David");
    }

    @Test
    void testMoveRowInvalidIndex() {
	// Invalid from index
	var success = model.moveRow(-1, 2);
	assertThat(success).isFalse();

	// Invalid to index
	success = model.moveRow(1, 10);
	assertThat(success).isFalse();

	// Both invalid
	success = model.moveRow(-1, -1);
	assertThat(success).isFalse();

	// Data should remain unchanged
	assertThat(model.getValue(0, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 0)).isEqualTo("Bob");
	assertThat(model.getValue(2, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(3, 0)).isEqualTo("David");
    }

    @Test
    void testMoveRowToEnd() {
	// Move first row to the end
	final var success = model.moveRow(0, 3);

	assertThat(success).isTrue();
	assertThat(model.getValue(0, 0)).isEqualTo("Bob");
	assertThat(model.getValue(1, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(2, 0)).isEqualTo("David");
	assertThat(model.getValue(3, 0)).isEqualTo("Alice");
    }

    @Test
    void testMoveRowFromEnd() {
	// Move last row to the beginning
	final var success = model.moveRow(3, 0);

	assertThat(success).isTrue();
	assertThat(model.getValue(0, 0)).isEqualTo("David");
	assertThat(model.getValue(1, 0)).isEqualTo("Alice");
	assertThat(model.getValue(2, 0)).isEqualTo("Bob");
	assertThat(model.getValue(3, 0)).isEqualTo("Charlie");
    }

    @Test
    void testMoveRowWithAllColumns() {
	// Move row and verify all columns are moved correctly
	final var success = model.moveRow(1, 0);

	assertThat(success).isTrue();
	assertThat(model.getValue(0, 0)).isEqualTo("Bob");
	assertThat(model.getValue(0, 1)).isEqualTo("25");
	assertThat(model.getValue(0, 2)).isEqualTo("London");
	assertThat(model.getValue(1, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 1)).isEqualTo("30");
	assertThat(model.getValue(1, 2)).isEqualTo("New York");
    }

    @Test
    void testUndoRedoRowMove() {
	// Perform a row move
	model.moveRow(0, 2);
	final var action = new UndoRedoManager.EditAction(UndoRedoManager.ActionType.ROW_MOVE, 0, 2);
	undoRedoManager.recordAction(action);

	// Verify the move
	assertThat(model.getValue(0, 0)).isEqualTo("Bob");
	assertThat(model.getValue(2, 0)).isEqualTo("Alice");

	// Undo the move
	final var undoAction = undoRedoManager.undo();
	handler.applyAction(undoAction, false);

	// Verify undo restored original order
	assertThat(model.getValue(0, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 0)).isEqualTo("Bob");
	assertThat(model.getValue(2, 0)).isEqualTo("Charlie");

	// Redo the move
	final var redoAction = undoRedoManager.redo();
	handler.applyAction(redoAction, true);

	// Verify redo moved the row again
	assertThat(model.getValue(0, 0)).isEqualTo("Bob");
	assertThat(model.getValue(2, 0)).isEqualTo("Alice");
    }

    @Test
    void testMultipleMoveOperations() {
	// Initial state: Alice(0), Bob(1), Charlie(2), David(3)

	// Move Alice from 0 to 2
	model.moveRow(0, 2); // Bob(0), Charlie(1), Alice(2), David(3)

	// Move Bob (now at 0) to 3
	model.moveRow(0, 3); // Charlie(0), Alice(1), David(2), Bob(3)

	// Move Alice (now at 1) to 0
	model.moveRow(1, 0); // Alice(0), Charlie(1), David(2), Bob(3)

	// Verify final state
	assertThat(model.getValue(0, 0)).isEqualTo("Alice");
	assertThat(model.getValue(1, 0)).isEqualTo("Charlie");
	assertThat(model.getValue(2, 0)).isEqualTo("David");
	assertThat(model.getValue(3, 0)).isEqualTo("Bob");
    }

    @Test
    void testMoveRowPreservesData() {
	// Store original data
	final var originalRow = model.getRow(1);

	// Move the row
	model.moveRow(1, 3);

	// Verify data is preserved
	final var movedRow = model.getRow(3);
	assertThat(movedRow).isEqualTo(originalRow);
    }
}
