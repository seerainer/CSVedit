package io.github.seerainer.csvedit.io;

import java.nio.charset.Charset;

import io.github.seerainer.csv.CSVConfiguration;
import io.github.seerainer.csv.CSVParsingOptions;
import io.github.seerainer.csvedit.util.Settings;

/**
 * Factory class for creating CSV configuration and parsing options from
 * settings
 */
public class CSVConfigurationFactory {

    private CSVConfigurationFactory() {
	throw new IllegalStateException("Utility class");
    }

    // @formatter:off

    /**
     * Creates a CSV configuration from current settings
     */
    public static CSVConfiguration createConfiguration() {
	return CSVConfiguration.builder()
		.delimiter(Settings.getDelimiter())
		.quote(Settings.getQuote())
		.escape(Settings.getEscape())
		.trimWhitespace(Settings.getTrimWhitespace())
		.detectBOM(Settings.getDetectBOM())
		.initialBufferSize(Settings.getInitialBufferSize())
		.maxFieldSize(Settings.getMaxFieldSize())
		.encoding(Charset.forName(Settings.getEncoding()))
		.build();
    }

    /**
     * Creates CSV parsing options from current settings
     */
    public static CSVParsingOptions createParsingOptions() {
	final var nullValueRep = Settings.getNullValueRepresentation();
	return CSVParsingOptions.builder()
		.preserveEmptyFields(Settings.getPreserveEmptyFields())
		.treatConsecutiveDelimitersAsEmpty(Settings.getPreserveEmptyFields())
		.skipEmptyLines(Settings.getSkipEmptyLines())
		.skipBlankLines(Settings.getSkipBlankLines())
		.nullValueRepresentation(nullValueRep.isEmpty() ? null : nullValueRep)
		.convertEmptyToNull(Settings.getConvertEmptyToNull())
		.strictQuoting(Settings.getStrictQuoting())
		.allowUnescapedQuotesInFields(Settings.getAllowUnescapedQuotes())
		.normalizeLineEndings(Settings.getNormalizeLineEndings())
		.maxRecordLength(Settings.getMaxRecordLength())
		.failOnMalformedRecord(Settings.getFailOnMalformedRecord())
		.trackFieldPositions(Settings.getTrackFieldPositions())
		.build();
    }

    // @formatter:on

    /**
     * Escapes a CSV value by adding quotes and escaping internal quotes
     */
    public static String escapeValue(final String value) {
	if (value == null || value.isEmpty()) {
	    return "";
	}

	if (!needsQuoting(value)) {
	    return value;
	}
	final var quote = "\"";
	return new StringBuilder().append(quote).append(value.replace(quote, "\"\"")).append(quote).toString();
    }

    /**
     * Checks if a value needs to be quoted in CSV format
     */
    private static boolean needsQuoting(final String value) {
	if (value == null || value.isEmpty()) {
	    return false;
	}
	return value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
    }
}