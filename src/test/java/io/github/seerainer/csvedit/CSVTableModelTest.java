package io.github.seerainer.csvedit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.seerainer.csvedit.model.CSVTableModel;

@Tag("unit")
class CSVTableModelTest {

    private CSVTableModel model;

    @BeforeEach
    void setUp() {
	model = new CSVTableModel();
    }

    @Test
    void testAddColumn() {
	model.addRow(List.of("A", "B"));
	model.addRow(List.of("C", "D"));

	model.addColumn("X");

	assertThat(model.getColumnCount()).isEqualTo(3);
	assertThat(model.getRow(0)).containsExactly("A", "B", "X");
	assertThat(model.getRow(1)).containsExactly("C", "D", "X");
    }

    @Test
    void testAddMultipleRows() {
	model.addRow(List.of("1", "2", "3"));
	model.addRow(List.of("4", "5", "6"));
	model.addRow(List.of("7", "8", "9"));

	assertThat(model.getRowCount()).isEqualTo(3);
	assertThat(model.getRow(1)).containsExactly("4", "5", "6");
    }

    @Test
    void testAddRow() {
	final List<String> row = List.of("A", "B", "C");
	model.addRow(row);

	assertThat(model.getRowCount()).isEqualTo(1);
	assertThat(model.getRow(0)).containsExactly("A", "B", "C");
    }

    @Test
    void testClear() {
	model.addRow(List.of("A", "B", "C"));
	model.addRow(List.of("D", "E", "F"));

	model.clear();

	assertThat(model.getRowCount()).isZero();
	assertThat(model.getColumnCount()).isZero();
    }

    @Test
    void testGetColumnCount() {
	model.addRow(List.of("A", "B", "C"));
	model.addRow(List.of("D", "E"));

	assertThat(model.getColumnCount()).isEqualTo(3);
    }

    @Test
    void testGetColumnCountEmptyModel() {
	assertThat(model.getColumnCount()).isZero();
    }

    @Test
    void testGetData() {
	model.addRow(List.of("A", "B"));
	model.addRow(List.of("C", "D"));

	final var data = model.getData();

	assertThat(data).hasSize(2);
	assertThat(data.get(0)).containsExactly("A", "B");
	assertThat(data.get(1)).containsExactly("C", "D");
    }

    @Test
    void testGetDataIsImmutable() {
	model.addRow(List.of("A", "B"));

	final var data = model.getData();
	data.get(0).set(0, "X");

	// Original model should not be affected
	assertThat(model.getValue(0, 0)).isEqualTo("A");
    }

    @Test
    void testGetRowCount() {
	assertThat(model.getRowCount()).isZero();

	model.addRow(List.of("A"));
	assertThat(model.getRowCount()).isEqualTo(1);

	model.addRow(List.of("B"));
	assertThat(model.getRowCount()).isEqualTo(2);
    }

    @Test
    void testGetRowOutOfBounds() {
	model.addRow(List.of("A", "B"));

	assertThat(model.getRow(5)).isEmpty();
	assertThat(model.getRow(-1)).isEmpty();
    }

    @Test
    void testGetValue() {
	model.addRow(List.of("X", "Y", "Z"));

	assertThat(model.getValue(0, 0)).isEqualTo("X");
	assertThat(model.getValue(0, 1)).isEqualTo("Y");
	assertThat(model.getValue(0, 2)).isEqualTo("Z");
    }

    @Test
    void testGetValueOutOfBounds() {
	model.addRow(List.of("A", "B"));

	assertThat(model.getValue(0, 5)).isEmpty();
	assertThat(model.getValue(5, 0)).isEmpty();
    }

    @Test
    void testNormalize() {
	model.addRow(List.of("A", "B", "C"));
	model.addRow(List.of("D"));
	model.addRow(List.of("E", "F"));

	model.normalize();

	assertThat(model.getRow(0)).hasSize(3);
	assertThat(model.getRow(1)).hasSize(3);
	assertThat(model.getRow(2)).hasSize(3);
	assertThat(model.getRow(1)).containsExactly("D", "", "");
    }

    @Test
    void testNormalizeEmptyModel() {
	model.normalize();

	assertThat(model.getRowCount()).isZero();
    }

    @Test
    void testRemoveColumn() {
	model.addRow(List.of("A", "B", "C"));
	model.addRow(List.of("D", "E", "F"));

	model.removeColumn(1);

	assertThat(model.getColumnCount()).isEqualTo(2);
	assertThat(model.getRow(0)).containsExactly("A", "C");
	assertThat(model.getRow(1)).containsExactly("D", "F");
    }

    @Test
    void testRemoveColumnFromEmptyRows() {
	model.addRow(new ArrayList<>());

	model.removeColumn(0);

	assertThat(model.getRowCount()).isEqualTo(1);
    }

    @Test
    void testRemoveRow() {
	model.addRow(List.of("1", "2"));
	model.addRow(List.of("3", "4"));
	model.addRow(List.of("5", "6"));

	model.removeRow(1);

	assertThat(model.getRowCount()).isEqualTo(2);
	assertThat(model.getRow(0)).containsExactly("1", "2");
	assertThat(model.getRow(1)).containsExactly("5", "6");
    }

    @Test
    void testRemoveRowOutOfBounds() {
	model.addRow(List.of("A", "B"));
	final var initialCount = model.getRowCount();

	model.removeRow(5);
	model.removeRow(-1);

	assertThat(model.getRowCount()).isEqualTo(initialCount);
    }

    @Test
    void testSetData() {
	final List<List<String>> data = new ArrayList<>();
	data.add(List.of("1", "2", "3"));
	data.add(List.of("4", "5", "6"));

	model.setData(data);

	assertThat(model.getRowCount()).isEqualTo(2);
	assertThat(model.getColumnCount()).isEqualTo(3);
	assertThat(model.getRow(0)).containsExactly("1", "2", "3");
    }

    @Test
    void testSetValue() {
	model.addRow(List.of("A", "B", "C"));
	model.setValue(0, 1, "X");

	assertThat(model.getValue(0, 1)).isEqualTo("X");
    }

    @Test
    void testSetValueExpandsRow() {
	model.addRow(List.of("A", "B"));
	model.setValue(0, 3, "D");

	assertThat(model.getRow(0)).hasSize(4);
	assertThat(model.getValue(0, 3)).isEqualTo("D");
	assertThat(model.getValue(0, 2)).isEmpty();
    }
}
