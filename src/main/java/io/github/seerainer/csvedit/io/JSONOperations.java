package io.github.seerainer.csvedit.io;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import io.github.seerainer.csvedit.model.CSVTableModel;

/**
 * Utility class for loading and saving JSON files using NanoJSON library.
 */
public class JSONOperations {

    private JSONOperations() {
	throw new IllegalStateException("Utility class");
    }

    /**
     * Loads a JSON file into the model using NanoJSON parser. Expected format: {
     * "headers": ["Col1", "Col2", ...], "rows": [ ["val1", "val2", ...], ["val3",
     * "val4", ...], ... ] }
     */
    public static void loadJSON(final String filePath, final CSVTableModel model) throws IOException {
	final List<List<String>> data = new ArrayList<>();
	final List<String> headers = new ArrayList<>();

	try {
	    // Read the entire file content
	    final var jsonContent = Files.readString(Paths.get(filePath));

	    // Parse JSON using NanoJSON
	    final var jsonObject = JsonParser.object().from(jsonContent);

	    // Parse headers array
	    if (jsonObject.has("headers")) {
		final var headersArray = jsonObject.getArray("headers");
		for (var i = 0; i < headersArray.size(); i++) {
		    headers.add(headersArray.getString(i));
		}
	    }

	    // Parse rows array
	    if (jsonObject.has("rows")) {
		final var rowsArray = jsonObject.getArray("rows");
		for (var i = 0; i < rowsArray.size(); i++) {
		    final var rowArray = rowsArray.getArray(i);
		    final List<String> row = new ArrayList<>();
		    for (var j = 0; j < rowArray.size(); j++) {
			final var value = rowArray.isNull(j) ? "" : rowArray.getString(j);
			row.add(value);
		    }
		    data.add(row);
		}
	    }

	} catch (final JsonParserException e) {
	    throw new IOException("Failed to parse JSON file: " + e.getMessage(), e);
	}

	model.clear();
	model.setHeaders(headers);
	model.setData(data);
	model.normalize();
    }

    /**
     * Saves the model data to a JSON file using NanoJSON writer. Format: {
     * "headers": ["Col1", "Col2", ...], "rows": [ ["val1", "val2", ...], ["val3",
     * "val4", ...], ... ] }
     */
    public static void saveJSON(final String filePath, final CSVTableModel model) throws IOException {
	try {
	    final var writer = new StringWriter();
	    final var jsonWriter = JsonWriter.indent("  ").on(writer);

	    // Start JSON object
	    jsonWriter.object();

	    // Write headers array
	    jsonWriter.array("headers");
	    final var headers = model.getHeaders();
	    headers.forEach(jsonWriter::value);
	    jsonWriter.end(); // end headers array

	    // Write rows array
	    jsonWriter.array("rows");
	    final var rowCount = model.getRowCount();
	    for (var i = 0; i < rowCount; i++) {
		jsonWriter.array(); // start row array
		final var row = model.getRow(i);
		row.forEach(jsonWriter::value);
		jsonWriter.end(); // end row array
	    }
	    jsonWriter.end(); // end rows array

	    jsonWriter.end(); // end root object
	    jsonWriter.done();

	    // Write to file
	    Files.writeString(Paths.get(filePath), writer.toString());

	} catch (final Exception e) {
	    throw new IOException("Failed to write JSON file: " + e.getMessage(), e);
	}
    }
}