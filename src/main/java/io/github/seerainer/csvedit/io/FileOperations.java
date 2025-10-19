package io.github.seerainer.csvedit.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.util.Settings;

/**
 * Utility class for loading and saving CSV files
 */
public class FileOperations {

    private FileOperations() {
	throw new IllegalStateException("Utility class");
    }

    /**
     * Loads a CSV file into the model (supports both regular and gzipped files)
     */
    public static void loadCSV(final String filePath, final CSVTableModel model) throws IOException {
	try {
	    final var parsed = isGzipFile(filePath) ? CSVParserUtil.parseGzipCSVFile(filePath)
		    : CSVParserUtil.parseCSVFile(filePath);

	    model.clear();
	    model.setHeaders(parsed.getHeaders());
	    model.setData(parsed.getData());
	    model.normalize(); // Ensure all rows have the same number of columns
	} catch (final IOException e) {
	    throw e;
	} catch (final Exception e) {
	    throw new IOException("An error occurred while loading the CSV file: " + e.getMessage(), e);
	}
    }

    /**
     * Check if a file is gzip compressed based on its extension
     */
    public static boolean isGzipFile(final String filePath) {
	return filePath != null && filePath.toLowerCase().endsWith(".gz");
    }

    /**
     * Saves the model data to a CSV file (supports both regular and gzipped files)
     */
    public static void saveCSV(final String filePath, final CSVTableModel model) throws IOException {
	if (isGzipFile(filePath)) {
	    saveGzipCSV(filePath, model);
	} else {
	    saveRegularCSV(filePath, model);
	}
    }

    /**
     * Saves the model data to a regular CSV file
     */
    private static void saveRegularCSV(final String filePath, final CSVTableModel model) throws IOException {
	final var charset = Charset.forName(Settings.getEncoding());
	try (var writer = Files.newBufferedWriter(Paths.get(filePath), charset)) {
	    writeCSVContent(writer, model);
	}
    }

    /**
     * Saves the model data to a gzip compressed CSV file
     */
    private static void saveGzipCSV(final String filePath, final CSVTableModel model) throws IOException {
	final var charset = Charset.forName(Settings.getEncoding());
	try (var fos = Files.newOutputStream(Paths.get(filePath));
		var gzos = new GZIPOutputStream(fos);
		var osw = new OutputStreamWriter(gzos, charset);
		var writer = new BufferedWriter(osw)) {
	    writeCSVContent(writer, model);
	}
    }

    /**
     * Writes CSV content to a writer
     */
    private static void writeCSVContent(final BufferedWriter writer, final CSVTableModel model) throws IOException {
	final var rowCount = model.getRowCount();
	final var colCount = model.getColumnCount();

	// Write headers as the first row
	final var headers = model.getHeaders();
	for (var j = 0; j < colCount; j++) {
	    final var value = j < headers.size() ? headers.get(j) : "Column " + (j + 1);
	    writer.write(CSVConfigurationFactory.escapeValue(value));
	    if (j < colCount - 1) {
		writer.write(",");
	    }
	}
	writer.newLine();

	// Write data rows
	for (var i = 0; i < rowCount; i++) {
	    final var row = model.getRow(i);

	    // Ensure row has enough columns
	    while (row.size() < colCount) {
		row.add("");
	    }

	    for (var j = 0; j < colCount; j++) {
		final var value = row.get(j);
		writer.write(CSVConfigurationFactory.escapeValue(value));
		if (j < colCount - 1) {
		    writer.write(",");
		}
	    }

	    if (i < rowCount - 1) {
		writer.newLine();
	    }
	}
    }
}