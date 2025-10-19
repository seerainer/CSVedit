package io.github.seerainer.csvedit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.github.seerainer.csvedit.model.CSVTableModel;

@Tag("unit")
class TextEditorDialogTest {

    private CSVTableModel model;

    @BeforeEach
    void setUp() {
	model = new CSVTableModel();
    }

    @Test
    void testEmptyModel() {
	// Empty model should have zero rows and columns
	assertThat(model.getRowCount()).isZero();
	assertThat(model.getColumnCount()).isZero();
    }

    @Test
    void testModelToTextConversion() {
	// Setup model with data
	model.setHeaders(List.of("Name", "Age", "City"));
	model.addRow(List.of("Alice", "30", "New York"));
	model.addRow(List.of("Bob", "25", "Los Angeles"));
	model.addRow(List.of("Charlie", "35", "Chicago"));

	// Verify model state
	assertThat(model.getRowCount()).isEqualTo(3);
	assertThat(model.getColumnCount()).isEqualTo(3);
	assertThat(model.getHeaders()).containsExactly("Name", "Age", "City");
	assertThat(model.getRow(0)).containsExactly("Alice", "30", "New York");
	assertThat(model.getRow(1)).containsExactly("Bob", "25", "Los Angeles");
	assertThat(model.getRow(2)).containsExactly("Charlie", "35", "Chicago");
    }

    @Test
    void testModelWithEmbeddedQuotes() {
	// Setup model with embedded quotes
	model.setHeaders(List.of("Quote", "Text"));
	model.addRow(List.of("Test", "She said \"Hello\""));

	// Verify model handles embedded quotes
	assertThat(model.getValue(0, 1)).isEqualTo("She said \"Hello\"");
    }

    @Test
    void testModelWithEmptyFields() {
	// Setup model with empty fields
	model.setHeaders(List.of("Column 1", "Column 2", "Column 3"));
	model.addRow(List.of("A", "", "C"));
	model.addRow(List.of("", "E", ""));

	// Verify empty fields are preserved
	assertThat(model.getValue(0, 1)).isEmpty();
	assertThat(model.getValue(1, 0)).isEmpty();
	assertThat(model.getValue(1, 2)).isEmpty();
    }

    @Test
    void testModelWithNewlines() {
	// Setup model with newlines in values
	model.setHeaders(List.of("Field1", "Field2"));
	model.addRow(List.of("Value", "Line1\nLine2"));

	// Verify model handles newlines
	assertThat(model.getValue(0, 1)).isEqualTo("Line1\nLine2");
    }

    @Test
    void testModelWithQuotedValues() {
	// Setup model with values that need quoting
	model.setHeaders(List.of("Name", "Address"));
	model.addRow(List.of("John Doe", "123 Main St, Apt 4"));
	model.addRow(List.of("Jane Smith", "456 Oak Ave"));

	// Verify model handles quoted values correctly
	assertThat(model.getRowCount()).isEqualTo(2);
	assertThat(model.getValue(0, 1)).isEqualTo("123 Main St, Apt 4");
    }
}
