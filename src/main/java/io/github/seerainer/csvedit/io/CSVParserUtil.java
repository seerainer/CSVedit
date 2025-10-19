package io.github.seerainer.csvedit.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import io.github.seerainer.csv.CSVParseException;
import io.github.seerainer.csv.CSVParser;
import io.github.seerainer.csv.CSVRecord;

/**
 * Utility class for common CSV parsing operations
 */
public class CSVParserUtil {

    private CSVParserUtil() {
	throw new IllegalStateException("Utility class");
    }

    /**
     * Extract a row of strings from a CSV record
     */
    public static List<String> extractRow(final CSVRecord record) {
	final List<String> row = new ArrayList<>();
	for (var i = 0; i < record.getFieldCount(); i++) {
	    final var value = record.getField(i);
	    row.add(value != null ? value : "");
	}
	return row;
    }

    /**
     * Check if a row contains only empty fields
     */
    public static boolean isRowEmpty(final List<String> row) {
	return row.stream().allMatch(String::isEmpty);
    }

    /**
     * Read file bytes and ensure proper newline termination for CSV parsing
     */
    public static byte[] readFileBytes(final String filePath) throws IOException {
	try (var raf = new RandomAccessFile(filePath, "r")) {
	    final var fileSize = Files.size(Paths.get(filePath));
	    final var fileBytes = new byte[(int) fileSize];
	    raf.readFully(fileBytes);
	    return ensureNewlineTermination(fileBytes);
	}
    }

    /**
     * Read a specific number of bytes from a file and ensure proper newline
     * termination
     */
    public static byte[] readFileBytes(final String filePath, final int maxBytes) throws IOException {
	try (var raf = new RandomAccessFile(filePath, "r")) {
	    final var bytesToRead = Math.min(raf.length(), maxBytes);
	    final var buffer = new byte[(int) bytesToRead];
	    raf.readFully(buffer);
	    return ensureNewlineTermination(buffer);
	}
    }

    /**
     * Ensure byte array ends with newline for proper CSV parsing
     */
    private static byte[] ensureNewlineTermination(final byte[] fileBytes) {
	if (fileBytes.length == 0) {
	    return fileBytes;
	}

	final var lastByte = fileBytes[fileBytes.length - 1];
	if (lastByte == '\n' || lastByte == '\r') {
	    return fileBytes;
	}
	// File doesn't end with newline - add one to ensure last record is parsed
	final var bytesToParse = new byte[fileBytes.length + 1];
	System.arraycopy(fileBytes, 0, bytesToParse, 0, fileBytes.length);
	bytesToParse[fileBytes.length] = '\n';
	return bytesToParse;
    }

    /**
     * Parse CSV bytes using configured parser
     */
    public static Iterable<CSVRecord> parseCSV(final byte[] bytes) throws IOException {
	final var config = CSVConfigurationFactory.createConfiguration();
	final var options = CSVConfigurationFactory.createParsingOptions();
	final var parser = new CSVParser(config, options);

	try {
	    return parser.parseByteArray(bytes);
	} catch (final CSVParseException e) {
	    throw new IOException("Failed to parse CSV content: " + e.getMessage(), e);
	}
    }

    /**
     * Parse CSV file and extract headers and data rows
     */
    public static ParsedCSV parseCSVFile(final String filePath) throws IOException {
	final var bytes = readFileBytes(filePath);
	return parseCSVBytes(bytes);
    }

    /**
     * Parse gzip compressed CSV file and extract headers and data rows
     */
    public static ParsedCSV parseGzipCSVFile(final String filePath) throws IOException {
	final var bytes = readGzipFileBytes(filePath);
	return parseCSVBytes(bytes);
    }

    /**
     * Read gzip compressed file bytes and ensure proper newline termination
     */
    public static byte[] readGzipFileBytes(final String filePath) throws IOException {
	try (var fis = Files.newInputStream(Paths.get(filePath));
		var bis = new BufferedInputStream(fis);
		var gzis = new GZIPInputStream(bis);
		var baos = new ByteArrayOutputStream()) {

	    final var buffer = new byte[8192];
	    int bytesRead;
	    while ((bytesRead = gzis.read(buffer)) != -1) {
		baos.write(buffer, 0, bytesRead);
	    }

	    return ensureNewlineTermination(baos.toByteArray());
	}
    }

    /**
     * Read gzip compressed file bytes with a maximum limit
     */
    public static byte[] readGzipFileBytes(final String filePath, final int maxBytes) throws IOException {
	try (var fis = Files.newInputStream(Paths.get(filePath));
		var bis = new BufferedInputStream(fis);
		var gzis = new GZIPInputStream(bis);
		var baos = new ByteArrayOutputStream()) {

	    final var buffer = new byte[8192];
	    int bytesRead;
	    var totalBytesRead = 0;

	    while ((bytesRead = gzis.read(buffer)) != -1 && totalBytesRead < maxBytes) {
		final var bytesToWrite = Math.min(bytesRead, maxBytes - totalBytesRead);
		baos.write(buffer, 0, bytesToWrite);
		totalBytesRead += bytesToWrite;
	    }

	    return ensureNewlineTermination(baos.toByteArray());
	}
    }

    /**
     * Parse CSV bytes and extract headers and data rows
     */
    public static ParsedCSV parseCSVBytes(final byte[] bytes) throws IOException {
	final var records = parseCSV(bytes);
	return extractHeadersAndData(records);
    }

    /**
     * Extract headers and data from CSV records
     */
    public static ParsedCSV extractHeadersAndData(final Iterable<CSVRecord> records) {
	final List<String> headers = new ArrayList<>();
	final List<List<String>> data = new ArrayList<>();

	var isFirstRow = true;
	for (final var record : records) {
	    final var row = extractRow(record);
	    final var allEmpty = isRowEmpty(row);

	    if (!allEmpty || !row.isEmpty()) {
		if (isFirstRow) {
		    headers.addAll(row);
		    isFirstRow = false;
		} else {
		    data.add(row);
		}
	    }
	}

	return new ParsedCSV(headers, data);
    }

    /**
     * Parse CSV file using callback-based approach for efficient memory usage with
     * large files. This method streams through the file and invokes the callback
     * for each parsed record, avoiding loading the entire file into memory. Uses a
     * default chunk size of 8MB for optimal performance.
     *
     * @param filePath the path to the CSV file
     * @param callback the consumer to invoke for each parsed CSV record
     * @throws IOException if an I/O error occurs or CSV parsing fails
     */
    public static void parseFileWithCallback(final Path filePath, final Consumer<CSVRecord> callback)
	    throws IOException {
	// Use default 8MB chunk size for optimal memory usage
	parseFileWithCallbackChunked(filePath, 8 * 1024 * 1024, callback);
    }

    /**
     * Parse CSV file using callback-based approach with chunked reading for
     * extremely large files. This method reads the file in chunks to minimize
     * memory usage.
     *
     * @param filePath  the path to the CSV file
     * @param chunkSize the size in bytes to read per chunk (default: 8MB)
     * @param callback  the consumer to invoke for each parsed CSV record
     * @throws IOException if an I/O error occurs or CSV parsing fails
     */
    public static void parseFileWithCallbackChunked(final Path filePath, final int chunkSize,
	    final Consumer<CSVRecord> callback) throws IOException {
	if (callback == null) {
	    throw new IllegalArgumentException("Callback cannot be null");
	}
	if (chunkSize <= 0) {
	    throw new IllegalArgumentException("Chunk size must be positive");
	}

	final var config = CSVConfigurationFactory.createConfiguration();
	final var options = CSVConfigurationFactory.createParsingOptions();
	final var parser = new CSVParser(config, options);

	// Determine if file is gzip compressed based on extension
	final var fileName = filePath.getFileName().toString().toLowerCase();
	final var isGzipped = fileName.endsWith(".gz");

	try (var inputStream = isGzipped ? new GZIPInputStream(new BufferedInputStream(Files.newInputStream(filePath)))
		: new BufferedInputStream(Files.newInputStream(filePath))) {

	    final var buffer = new byte[chunkSize];
	    final var overflow = new ByteArrayOutputStream();
	    int bytesRead;

	    while ((bytesRead = inputStream.read(buffer)) != -1) {
		// Combine overflow from previous chunk with current chunk
		final var chunkData = new ByteArrayOutputStream();
		if (overflow.size() > 0) {
		    overflow.writeTo(chunkData);
		    overflow.reset();
		}
		chunkData.write(buffer, 0, bytesRead);

		// Find the last newline to determine where to split
		final var chunkBytes = chunkData.toByteArray();
		var lastNewline = -1;
		for (var i = chunkBytes.length - 1; i >= 0; i--) {
		    if (chunkBytes[i] == '\n' || chunkBytes[i] == '\r') {
			lastNewline = i;
			break;
		    }
		}

		// If we found a newline, parse up to that point
		if (lastNewline >= 0) {
		    final var parseBytes = new byte[lastNewline + 1];
		    System.arraycopy(chunkBytes, 0, parseBytes, 0, lastNewline + 1);

		    // Save remainder for next iteration
		    if (lastNewline + 1 < chunkBytes.length) {
			overflow.write(chunkBytes, lastNewline + 1, chunkBytes.length - lastNewline - 1);
		    }

		    // Parse this chunk and invoke callback
		    final var records = parser.parseByteArray(parseBytes);
		    records.forEach(callback::accept);
		} else {
		    // No newline found, save entire chunk for next iteration
		    overflow.write(chunkBytes, 0, chunkBytes.length);
		}
	    }

	    // Process any remaining data
	    if (overflow.size() > 0) {
		final var remainingBytes = ensureNewlineTermination(overflow.toByteArray());
		final var records = parser.parseByteArray(remainingBytes);
		records.forEach(callback::accept);
	    }
	} catch (final CSVParseException e) {
	    throw new IOException("Failed to parse CSV content: " + e.getMessage(), e);
	}
    }

    /**
     * Result of parsing CSV data
     */
    public static class ParsedCSV {
	private final List<String> headers;
	private final List<List<String>> data;

	public ParsedCSV(final List<String> headers, final List<List<String>> data) {
	    this.headers = headers;
	    this.data = data;
	}

	public List<String> getHeaders() {
	    return headers;
	}

	public List<List<String>> getData() {
	    return data;
	}
    }
}
