package io.github.seerainer.csvedit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.seerainer.csvedit.io.CSVParserUtil;

@Tag("unit")
class CSVParserCallbackTest {

    @TempDir
    Path tempDir;

    @Test
    void testParseFileWithCallbackBasic() throws IOException {
	final var testFile = tempDir.resolve("test.csv");
	Files.writeString(testFile, "Name,Age,City\nJohn,25,NYC\nJane,30,LA\nBob,35,SF");

	final List<List<String>> records = new ArrayList<>();
	CSVParserUtil.parseFileWithCallback(testFile, record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(4);
	assertThat(records.get(0)).containsExactly("Name", "Age", "City");
	assertThat(records.get(1)).containsExactly("John", "25", "NYC");
	assertThat(records.get(2)).containsExactly("Jane", "30", "LA");
	assertThat(records.get(3)).containsExactly("Bob", "35", "SF");
    }

    @Test
    void testParseFileWithCallbackLargeFile() throws IOException {
	final var testFile = tempDir.resolve("large.csv");
	final var sb = new StringBuilder("ID,Name,Value\n");
	final var rowCount = 10000;

	for (var i = 0; i < rowCount; i++) {
	    sb.append(i).append(",Name").append(i).append(",Value").append(i).append("\n");
	}
	Files.writeString(testFile, sb.toString());

	final var counter = new AtomicInteger(0);
	CSVParserUtil.parseFileWithCallback(testFile, _ -> counter.incrementAndGet());

	// Header + data rows
	assertThat(counter.get()).isEqualTo(rowCount + 1);
    }

    @Test
    void testParseFileWithCallbackQuotedFields() throws IOException {
	final var testFile = tempDir.resolve("quoted.csv");
	Files.writeString(testFile, "Name,Address\nJohn,\"123 Main St, Apt 4\"\nJane,\"456 Oak Ave\"");

	final List<List<String>> records = new ArrayList<>();
	CSVParserUtil.parseFileWithCallback(testFile, record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(3);
	assertThat(records.get(1).get(1)).isEqualTo("123 Main St, Apt 4");
	assertThat(records.get(2).get(1)).isEqualTo("456 Oak Ave");
    }

    @Test
    void testParseFileWithCallbackEmptyFields() throws IOException {
	final var testFile = tempDir.resolve("empty.csv");
	Files.writeString(testFile, "A,B,C\n1,,3\n,5,\n7,8,9");

	final List<List<String>> records = new ArrayList<>();
	CSVParserUtil.parseFileWithCallback(testFile, record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(4);
	assertThat(records.get(1)).containsExactly("1", "", "3");
	assertThat(records.get(2)).containsExactly("", "5", "");
	assertThat(records.get(3)).containsExactly("7", "8", "9");
    }

    @Test
    void testParseFileWithCallbackNullCallback() {
	final var testFile = tempDir.resolve("test.csv");

	assertThatThrownBy(() -> CSVParserUtil.parseFileWithCallback(testFile, null))
		.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Callback cannot be null");
    }

    @Test
    void testParseFileWithCallbackChunkedBasic() throws IOException {
	final var testFile = tempDir.resolve("test.csv");
	Files.writeString(testFile, "Name,Age\nAlice,28\nBob,32");

	final List<List<String>> records = new ArrayList<>();
	CSVParserUtil.parseFileWithCallbackChunked(testFile, 1024,
		record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(3);
	assertThat(records.get(0)).containsExactly("Name", "Age");
	assertThat(records.get(1)).containsExactly("Alice", "28");
	assertThat(records.get(2)).containsExactly("Bob", "32");
    }

    @Test
    void testParseFileWithCallbackChunkedSmallChunks() throws IOException {
	final var testFile = tempDir.resolve("test.csv");
	Files.writeString(testFile, "A,B,C\nD,E,F\nG,H,I");

	final List<List<String>> records = new ArrayList<>();
	// Use very small chunks to test boundary handling
	CSVParserUtil.parseFileWithCallbackChunked(testFile, 10,
		record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(3);
	assertThat(records.get(0)).containsExactly("A", "B", "C");
	assertThat(records.get(1)).containsExactly("D", "E", "F");
	assertThat(records.get(2)).containsExactly("G", "H", "I");
    }

    @Test
    void testParseFileWithCallbackChunkedLargeFile() throws IOException {
	final var testFile = tempDir.resolve("large.csv");
	final var sb = new StringBuilder("ID,Data\n");
	final var rowCount = 50000;

	for (var i = 0; i < rowCount; i++) {
	    sb.append(i).append(",Data").append(i).append("\n");
	}
	Files.writeString(testFile, sb.toString());

	final var counter = new AtomicInteger(0);
	CSVParserUtil.parseFileWithCallbackChunked(testFile, 8192, _ -> counter.incrementAndGet());

	// Header + data rows
	assertThat(counter.get()).isEqualTo(rowCount + 1);
    }

    @Test
    void testParseFileWithCallbackChunkedInvalidChunkSize() {
	final var testFile = tempDir.resolve("test.csv");

	assertThatThrownBy(() -> CSVParserUtil.parseFileWithCallbackChunked(testFile, 0, _ -> { /* no-op */
	})).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Chunk size must be positive");

	assertThatThrownBy(() -> CSVParserUtil.parseFileWithCallbackChunked(testFile, -100, _ -> { /* no-op */
	})).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Chunk size must be positive");
    }

    @Test
    void testParseFileWithCallbackChunkedWithQuotesSpanningChunks() throws IOException {
	final var testFile = tempDir.resolve("quotes.csv");
	final var longQuotedValue = "This is a very long quoted value that might span across chunk boundaries "
		+ "when reading the file in small chunks. It contains commas, like this: a, b, c, d.";
	Files.writeString(testFile, new StringBuilder().append("Name,Description\nItem1,\"").append(longQuotedValue)
		.append("\"\nItem2,Short").toString());

	final List<List<String>> records = new ArrayList<>();
	// Use small chunk size to force spanning
	CSVParserUtil.parseFileWithCallbackChunked(testFile, 50,
		record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(3);
	assertThat(records.get(1).get(1)).isEqualTo(longQuotedValue);
	assertThat(records.get(2)).containsExactly("Item2", "Short");
    }

    @Test
    void testParseFileWithCallbackRecordWithoutTrailingNewline() throws IOException {
	final var testFile = tempDir.resolve("no-trailing-newline.csv");
	// Write file without trailing newline
	Files.writeString(testFile, "A,B\nC,D");

	final List<List<String>> records = new ArrayList<>();
	CSVParserUtil.parseFileWithCallback(testFile, record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(2);
	assertThat(records.get(0)).containsExactly("A", "B");
	assertThat(records.get(1)).containsExactly("C", "D");
    }

    @Test
    void testParseFileWithCallbackEmbeddedNewlines() throws IOException {
	final var testFile = tempDir.resolve("embedded-newlines.csv");
	Files.writeString(testFile, "Name,Note\nJohn,\"Line1\nLine2\nLine3\"\nJane,Simple");

	final List<List<String>> records = new ArrayList<>();
	CSVParserUtil.parseFileWithCallback(testFile, record -> records.add(CSVParserUtil.extractRow(record)));

	assertThat(records).hasSize(3);
	assertThat(records.get(0)).containsExactly("Name", "Note");
	assertThat(records.get(1).get(1)).isEqualTo("Line1\nLine2\nLine3");
	assertThat(records.get(2)).containsExactly("Jane", "Simple");
    }
}
