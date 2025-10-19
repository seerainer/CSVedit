package io.github.seerainer.csvedit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.seerainer.csvedit.io.FileOperations;
import io.github.seerainer.csvedit.model.CSVTableModel;

@Tag("integration")
class FileOperationsTest {

    @TempDir
    Path tempDir;

    private CSVTableModel model;
    private File testFile;

    @BeforeEach
    void setUp() {
	model = new CSVTableModel();
	testFile = tempDir.resolve("test.csv").toFile();
    }

    @AfterEach
    void tearDown() {
	model.clear();
    }

    @Test
    void testLoadCSVClearsExistingData() throws IOException {
	model.addRow(List.of("Old", "Data"));
	Files.writeString(testFile.toPath(), "New,Data\nValue1,Value2");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	assertThat(model.getRowCount()).isEqualTo(1);
	assertThat(model.getHeaders()).containsExactly("New", "Data");
	assertThat(model.getRow(0)).containsExactly("Value1", "Value2");
    }

    @Test
    void testLoadCSVWithEmbeddedQuotes() throws IOException {
	Files.writeString(testFile.toPath(), "Quote,Text\nTest,\"She said \"\"Hello\"\"\"");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	assertThat(model.getValue(0, 1)).isEqualTo("She said \"Hello\"");
    }

    @Test
    void testLoadCSVWithIrregularRows() throws IOException {
	Files.writeString(testFile.toPath(), "A,B,C\nD,E\nF,G,H,I");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	// First line is headers, so we have 2 data rows
	assertThat(model.getRowCount()).isEqualTo(2);
	assertThat(model.getColumnCount()).isEqualTo(4);
	// The model will auto-generate a header for the 4th column since the header row
	// only has 3 columns
	assertThat(model.getHeaders()).hasSize(4);
	assertThat(model.getHeader(0)).isEqualTo("A");
	assertThat(model.getHeader(1)).isEqualTo("B");
	assertThat(model.getHeader(2)).isEqualTo("C");
	assertThat(model.getRow(0)).containsExactly("D", "E", "", "");
    }

    @Test
    void testLoadCSVWithOnlyHeaders() throws IOException {
	Files.writeString(testFile.toPath(), "Name,Age,City");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	// Only headers, no data rows
	assertThat(model.getRowCount()).isEqualTo(0);
	assertThat(model.getHeaders()).containsExactly("Name", "Age", "City");
    }

    @Test
    void testLoadCSVWithQuotes() throws IOException {
	Files.writeString(testFile.toPath(), "Name,Address\nJohn,\"123 Main St, Apt 4\"");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	assertThat(model.getValue(0, 1)).isEqualTo("123 Main St, Apt 4");
    }

    @Test
    void testLoadEmptyCSV() throws IOException {
	Files.writeString(testFile.toPath(), "");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	assertThat(model.getRowCount()).isZero();
    }

    @Test
    void testLoadNonExistentFile() {
	final var nonExistent = new File(tempDir.toFile(), "nonexistent.csv");

	assertThatThrownBy(() -> FileOperations.loadCSV(nonExistent.getAbsolutePath(), model))
		.isInstanceOf(IOException.class);
    }

    @Test
    void testLoadSimpleCSV() throws Exception {
	Files.writeString(testFile.toPath(), "A,B,C\n1,2,3\n4,5,6");

	FileOperations.loadCSV(testFile.getAbsolutePath(), model);

	// First line is now headers, so we have 2 data rows
	assertThat(model.getRowCount()).isEqualTo(2);
	assertThat(model.getColumnCount()).isEqualTo(3);
	assertThat(model.getHeaders()).containsExactly("A", "B", "C");
	assertThat(model.getRow(0)).containsExactly("1", "2", "3");
	assertThat(model.getRow(1)).containsExactly("4", "5", "6");
    }

    @Test
    void testSaveAndLoadRoundTrip() throws IOException {
	model.setHeaders(List.of("Name", "Age", "City"));
	model.addRow(List.of("Alice", "30", "New York"));
	model.addRow(List.of("Bob", "25", "Los Angeles"));
	model.addRow(List.of("Charlie", "35", "Chicago"));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var loadedModel = new CSVTableModel();
	FileOperations.loadCSV(testFile.getAbsolutePath(), loadedModel);

	assertThat(loadedModel.getRowCount()).isEqualTo(3);
	assertThat(loadedModel.getColumnCount()).isEqualTo(3);
	assertThat(loadedModel.getHeaders()).containsExactly("Name", "Age", "City");
	assertThat(loadedModel.getRow(1)).containsExactly("Bob", "25", "Los Angeles");
    }

    @Test
    void testSaveCSVNormalizesRows() throws IOException {
	model.setHeaders(List.of("Column 1", "Column 2", "Column 3"));
	model.addRow(List.of("A", "B", "C"));
	model.addRow(List.of("D"));
	model.addRow(List.of("E", "F"));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var content = Files.readString(testFile.toPath());
	final var lines = content.split("\\r?\\n");
	assertThat(lines).hasSize(4); // 1 header + 3 data rows
	assertThat(lines[1].split(",", -1)).hasSize(3);
    }

    @Test
    void testSaveCSVWithEmbeddedQuotes() throws IOException {
	model.setHeaders(List.of("Quote", "Text"));
	model.addRow(List.of("Test", "She said \"Hello\""));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var content = Files.readString(testFile.toPath());
	assertThat(content).contains("\"She said \"\"Hello\"\"\"");
    }

    @Test
    void testSaveCSVWithEmptyFields() throws IOException {
	model.setHeaders(List.of("Column 1", "Column 2", "Column 3"));
	model.addRow(List.of("A", "", "C"));
	model.addRow(List.of("", "E", ""));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var content = Files.readString(testFile.toPath());
	final var lines = content.split("\\r?\\n");
	assertThat(lines).containsExactly("Column 1,Column 2,Column 3", "A,,C", ",E,");
    }

    @Test
    void testSaveCSVWithNewlines() throws IOException {
	model.setHeaders(List.of("Field1", "Field2"));
	model.addRow(List.of("Value", "Line1\nLine2"));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var content = Files.readString(testFile.toPath());
	assertThat(content).contains("\"Line1\nLine2\"");
    }

    @Test
    void testSaveCSVWithQuotes() throws IOException {
	model.setHeaders(List.of("Name", "Address"));
	model.addRow(List.of("John Doe", "123 Main St, Apt 4"));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var content = Files.readString(testFile.toPath());
	assertThat(content).contains("\"123 Main St, Apt 4\"");
    }

    @Test
    void testSaveEmptyCSV() throws IOException {
	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	assertThat(testFile).exists();
	final var content = Files.readString(testFile.toPath());
	// Empty model with no headers or data should just produce an empty header line
	assertThat(content.trim()).isEmpty();
    }

    @Test
    void testSaveSimpleCSV() throws IOException {
	// Set headers
	model.setHeaders(List.of("A", "B", "C"));
	model.addRow(List.of("1", "2", "3"));

	FileOperations.saveCSV(testFile.getAbsolutePath(), model);

	final var content = Files.readString(testFile.toPath());
	final var lines = content.split("\\r?\\n");
	assertThat(lines).containsExactly("A,B,C", "1,2,3");
    }

    @Test
    void testSaveToInvalidPath() {
	final var invalidPath = tempDir.resolve("nonexistent/folder/test.csv").toString();
	model.addRow(List.of("A", "B"));

	assertThatThrownBy(() -> FileOperations.saveCSV(invalidPath, model)).isInstanceOf(IOException.class);
    }

    @SuppressWarnings("static-method")
    @Test
    void testIsGzipFile() {
	assertThat(FileOperations.isGzipFile("test.csv.gz")).isTrue();
	assertThat(FileOperations.isGzipFile("TEST.CSV.GZ")).isTrue();
	assertThat(FileOperations.isGzipFile("test.csv")).isFalse();
	assertThat(FileOperations.isGzipFile("test.gz")).isTrue();
	assertThat(FileOperations.isGzipFile(null)).isFalse();
    }

    @Test
    void testSaveAndLoadGzipCSV() throws IOException {
	final var gzipFile = tempDir.resolve("test.csv.gz").toFile();

	model.setHeaders(List.of("Name", "Age", "City"));
	model.addRow(List.of("Alice", "30", "New York"));
	model.addRow(List.of("Bob", "25", "Los Angeles"));
	model.addRow(List.of("Charlie", "35", "Chicago"));

	FileOperations.saveCSV(gzipFile.getAbsolutePath(), model);

	// Verify file is actually gzip compressed
	assertThat(gzipFile).exists();
	final var bytes = Files.readAllBytes(gzipFile.toPath());
	// Gzip files start with magic bytes 0x1f 0x8b
	assertThat(bytes[0]).isEqualTo((byte) 0x1f);
	assertThat(bytes[1]).isEqualTo((byte) 0x8b);

	final var loadedModel = new CSVTableModel();
	FileOperations.loadCSV(gzipFile.getAbsolutePath(), loadedModel);

	assertThat(loadedModel.getRowCount()).isEqualTo(3);
	assertThat(loadedModel.getColumnCount()).isEqualTo(3);
	assertThat(loadedModel.getHeaders()).containsExactly("Name", "Age", "City");
	assertThat(loadedModel.getRow(0)).containsExactly("Alice", "30", "New York");
	assertThat(loadedModel.getRow(1)).containsExactly("Bob", "25", "Los Angeles");
	assertThat(loadedModel.getRow(2)).containsExactly("Charlie", "35", "Chicago");
    }

    @Test
    void testLoadGzipCSVWithSpecialCharacters() throws IOException {
	final var gzipFile = tempDir.resolve("special.csv.gz").toFile();

	model.setHeaders(List.of("Quote", "Comma", "Newline"));
	model.addRow(List.of("Say \"Hi\"", "A, B, C", "Line1\nLine2"));

	FileOperations.saveCSV(gzipFile.getAbsolutePath(), model);

	final var loadedModel = new CSVTableModel();
	FileOperations.loadCSV(gzipFile.getAbsolutePath(), loadedModel);

	assertThat(loadedModel.getRowCount()).isEqualTo(1);
	assertThat(loadedModel.getValue(0, 0)).isEqualTo("Say \"Hi\"");
	assertThat(loadedModel.getValue(0, 1)).isEqualTo("A, B, C");
	assertThat(loadedModel.getValue(0, 2)).isEqualTo("Line1\nLine2");
    }

    @Test
    void testSaveGzipCSVIsCompressed() throws IOException {
	final var regularFile = tempDir.resolve("regular.csv").toFile();
	final var gzipFile = tempDir.resolve("compressed.csv.gz").toFile();

	// Create a model with repetitive data that compresses well
	model.setHeaders(List.of("Col1", "Col2", "Col3"));
	for (var i = 0; i < 100; i++) {
	    model.addRow(List.of("Same", "Data", "Repeated"));
	}

	FileOperations.saveCSV(regularFile.getAbsolutePath(), model);
	FileOperations.saveCSV(gzipFile.getAbsolutePath(), model);

	// Gzip file should be smaller than regular file
	final var regularSize = Files.size(regularFile.toPath());
	final var gzipSize = Files.size(gzipFile.toPath());

	assertThat(gzipSize).isLessThan(regularSize);
    }

    @Test
    void testLoadGzipEmptyCSV() throws IOException {
	final var gzipFile = tempDir.resolve("empty.csv.gz").toFile();

	FileOperations.saveCSV(gzipFile.getAbsolutePath(), model);

	final var loadedModel = new CSVTableModel();
	FileOperations.loadCSV(gzipFile.getAbsolutePath(), loadedModel);

	assertThat(loadedModel.getRowCount()).isZero();
    }
}