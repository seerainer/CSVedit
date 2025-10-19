package io.github.seerainer.csvedit.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import io.github.seerainer.csvedit.model.CSVTableModel;

/**
 * Utility class for loading and saving XML files using standard Java StAX API.
 */
public class XMLOperations {

    private XMLOperations() {
	throw new IllegalStateException("Utility class");
    }

    /**
     * Loads an XML file into the model using StAX parser. Expected format: <csv>
     * <headers> <header>Col1</header> <header>Col2</header> </headers> <rows> <row>
     * <cell>val1</cell> <cell>val2</cell> </row> </rows> </csv>
     */
    public static void loadXML(final String filePath, final CSVTableModel model) throws IOException {
	final List<List<String>> data = new ArrayList<>();
	final List<String> headers = new ArrayList<>();

	try (var inputStream = Files.newInputStream(Paths.get(filePath))) {
	    final var factory = XMLInputFactory.newInstance();

	    // Security: disable external entities to prevent XXE attacks
	    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
	    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

	    final var reader = factory.createXMLStreamReader(inputStream);

	    try {
		parseXMLStream(reader, headers, data);
	    } finally {
		reader.close();
	    }
	} catch (final XMLStreamException e) {
	    throw new IOException("Failed to parse XML file: " + e.getMessage(), e);
	}

	model.clear();
	model.setHeaders(headers);
	model.setData(data);
	model.normalize();
    }

    /**
     * Parse XML stream using StAX event-based parsing
     */
    private static void parseXMLStream(final XMLStreamReader reader, final List<String> headers,
	    final List<List<String>> data) throws XMLStreamException {
	List<String> currentRow = null;
	final var textBuilder = new StringBuilder();

	while (reader.hasNext()) {
	    final var event = reader.next();

	    switch (event) {
	    case XMLStreamConstants.START_ELEMENT -> {
		final var elementName = reader.getLocalName();
		textBuilder.setLength(0); // Clear text buffer
		if ("row".equals(elementName)) {
		    currentRow = new ArrayList<>();
		}
	    }
	    case XMLStreamConstants.CHARACTERS -> {
		if (!reader.isWhiteSpace()) {
		    textBuilder.append(reader.getText());
		}
	    }
	    case XMLStreamConstants.END_ELEMENT -> {
		final var endElementName = reader.getLocalName();
		if ("header".equals(endElementName)) {
		    headers.add(textBuilder.toString());
		} else if ("cell".equals(endElementName) && currentRow != null) {
		    currentRow.add(textBuilder.toString());
		} else if ("row".equals(endElementName) && currentRow != null) {
		    data.add(currentRow);
		    currentRow = null;
		}
		textBuilder.setLength(0); // Clear text buffer
	    }
	    default -> { /* Ignore other events */
	    }
	    }
	}
    }

    /**
     * Saves the model data to an XML file using StAX writer. Format: <csv>
     * <headers> <header>Col1</header> <header>Col2</header> </headers> <rows> <row>
     * <cell>val1</cell> <cell>val2</cell> </row> </rows> </csv>
     */
    public static void saveXML(final String filePath, final CSVTableModel model) throws IOException {
	try (var outputStream = Files.newOutputStream(Paths.get(filePath))) {
	    final var factory = XMLOutputFactory.newInstance();
	    final var writer = factory.createXMLStreamWriter(outputStream, "UTF-8");

	    try {
		writeXMLStream(writer, model);
	    } finally {
		writer.close();
	    }
	} catch (final XMLStreamException e) {
	    throw new IOException("Failed to write XML file: " + e.getMessage(), e);
	}
    }

    /**
     * Write XML stream using StAX writer
     */
    private static void writeXMLStream(final XMLStreamWriter writer, final CSVTableModel model)
	    throws XMLStreamException {
	// Write XML declaration
	writer.writeStartDocument("UTF-8", "1.0");
	writer.writeCharacters("\n");

	// Root element
	writer.writeStartElement("csv");
	writer.writeCharacters("\n");

	// Write headers section
	writer.writeCharacters("  ");
	writer.writeStartElement("headers");
	writer.writeCharacters("\n");

	final var headers = model.getHeaders();
	for (final var header : headers) {
	    writer.writeCharacters("    ");
	    writer.writeStartElement("header");
	    writer.writeCharacters(header);
	    writer.writeEndElement();
	    writer.writeCharacters("\n");
	}

	writer.writeCharacters("  ");
	writer.writeEndElement(); // </headers>
	writer.writeCharacters("\n");

	// Write rows section
	writer.writeCharacters("  ");
	writer.writeStartElement("rows");
	writer.writeCharacters("\n");

	final var rowCount = model.getRowCount();
	for (var i = 0; i < rowCount; i++) {
	    writer.writeCharacters("    ");
	    writer.writeStartElement("row");
	    writer.writeCharacters("\n");

	    final var row = model.getRow(i);
	    for (final var cell : row) {
		writer.writeCharacters("      ");
		writer.writeStartElement("cell");
		writer.writeCharacters(cell != null ? cell : "");
		writer.writeEndElement();
		writer.writeCharacters("\n");
	    }

	    writer.writeCharacters("    ");
	    writer.writeEndElement(); // </row>
	    writer.writeCharacters("\n");
	}

	writer.writeCharacters("  ");
	writer.writeEndElement(); // </rows>
	writer.writeCharacters("\n");

	writer.writeEndElement(); // </csv>
	writer.writeCharacters("\n");
	writer.writeEndDocument();
    }
}