package io.github.seerainer.csvedit.util;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Shell;

import io.github.seerainer.csvedit.model.CSVTableModel;

/**
 * Handles printing of CSV table data
 */
public class PrintHandler {

    private final Shell shell;
    private final CSVTableModel model;

    public PrintHandler(final Shell shell, final CSVTableModel model) {
	this.shell = shell;
	this.model = model;
    }

    /**
     * Print header row
     */
    private static int printHeaders(final GC gc, final int startX, final int startY, final int[] columnWidths,
	    final int lineHeight, final List<String> headers) {
	var x = startX;
	final var columnCount = columnWidths.length;

	// Draw header background (light gray)
	final var oldBackground = gc.getBackground();
	gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));

	var totalWidth = 0;
	for (final var width : columnWidths) {
	    totalWidth += width;
	}
	gc.fillRectangle(startX, startY, totalWidth, lineHeight);
	gc.setBackground(oldBackground);

	// Draw header text
	final var oldFontData = gc.getFont().getFontData()[0];
	final var boldFont = new Font(gc.getDevice(), oldFontData.getName(), oldFontData.getHeight(), SWT.BOLD);
	gc.setFont(boldFont);

	for (var i = 0; i < columnCount; i++) {
	    final var header = i < headers.size() ? headers.get(i) : "Column " + (i + 1);
	    final var text = truncateText(gc, header, columnWidths[i] - 4);
	    gc.drawText(text, x + 2, startY + 2, true);
	    gc.drawRectangle(x, startY, columnWidths[i], lineHeight);
	    x += columnWidths[i];
	}

	boldFont.dispose();

	return startY + lineHeight;
    }

    /**
     * Print a data row
     */
    private static int printRow(final GC gc, final int startX, final int startY, final int[] columnWidths,
	    final int lineHeight, final List<String> row, final int columnCount) {
	var x = startX;

	for (var i = 0; i < columnCount; i++) {
	    final var value = i < row.size() ? row.get(i) : "";
	    final var text = truncateText(gc, value, columnWidths[i] - 4);
	    gc.drawText(text, x + 2, startY + 2, true);
	    gc.drawRectangle(x, startY, columnWidths[i], lineHeight);
	    x += columnWidths[i];
	}

	return startY + lineHeight;
    }

    /**
     * Truncate text to fit within specified width
     */
    private static String truncateText(final GC gc, final String text, final int maxWidth) {
	if (text == null || text.isEmpty()) {
	    return "";
	}

	final var extent = gc.textExtent(text);
	if (extent.x <= maxWidth) {
	    return text;
	}

	// Truncate with ellipsis
	final var ellipsis = "...";
	final var ellipsisWidth = gc.textExtent(ellipsis).x;
	final var availableWidth = maxWidth - ellipsisWidth;

	if (availableWidth <= 0) {
	    return ellipsis;
	}

	var len = text.length();
	while (len > 0) {
	    final var truncated = text.substring(0, len);
	    if (gc.textExtent(truncated).x <= availableWidth) {
		return truncated + ellipsis;
	    }
	    len--;
	}

	return ellipsis;
    }

    /**
     * Calculate column widths based on content
     */
    private int[] calculateColumnWidths(final GC gc, final int availableWidth) {
	final var columnCount = model.getColumnCount();
	final var widths = new int[columnCount];
	final var headers = model.getHeaders();

	// Calculate minimum width needed for each column
	for (var i = 0; i < columnCount; i++) {
	    var maxWidth = 0;

	    // Check header width
	    if (i < headers.size()) {
		final var headerWidth = gc.textExtent(headers.get(i)).x + 8;
		maxWidth = Math.max(maxWidth, headerWidth);
	    }

	    // Check data widths (sample first 100 rows for performance)
	    final var sampleSize = Math.min(100, model.getRowCount());
	    for (var j = 0; j < sampleSize; j++) {
		final var row = model.getRow(j);
		if (i < row.size()) {
		    final var cellWidth = gc.textExtent(row.get(i)).x + 8;
		    maxWidth = Math.max(maxWidth, cellWidth);
		}
	    }

	    widths[i] = maxWidth;
	}

	// Scale widths to fit available width
	var totalWidth = 0;
	for (final var width : widths) {
	    totalWidth += width;
	}

	if (totalWidth > availableWidth) {
	    final var scale = (double) availableWidth / totalWidth;
	    for (var i = 0; i < widths.length; i++) {
		widths[i] = (int) (widths[i] * scale);
	    }
	}

	return widths;
    }

    /**
     * Show print dialog and print the table
     */
    public void print() {
	final var dialog = new PrintDialog(shell);
	final var printerData = dialog.open();

	if (printerData == null) {
	    return; // User cancelled
	}

	final var printer = new Printer(printerData);
	try {
	    if (printer.startJob("CSV Table Print")) {
		final var gc = new GC(printer);
		try {
		    printTable(printer, gc);
		} finally {
		    gc.dispose();
		}
		printer.endJob();
	    }
	} finally {
	    printer.dispose();
	}
    }

    /**
     * Print the table content
     */
    private void printTable(final Printer printer, final GC gc) {
	final var clientArea = printer.getClientArea();
	final var trim = printer.computeTrim(0, 0, 0, 0);
	final var pageWidth = clientArea.width - trim.width;
	final var pageHeight = clientArea.height - trim.height;

	final var dpi = printer.getDPI();
	final var margin = dpi.y / 2; // 0.5 inch margin

	final var printableWidth = pageWidth - 2 * margin;
	final var printableHeight = pageHeight - 2 * margin;

	final var headers = model.getHeaders();
	final var columnCount = model.getColumnCount();
	final var rowCount = model.getRowCount();

	if (columnCount == 0) {
	    return;
	}

	// Calculate column widths
	final var columnWidths = calculateColumnWidths(gc, printableWidth);

	// Calculate row height
	final var lineHeight = gc.getFontMetrics().getHeight() + 4;

	var currentY = margin;
	// Start first page
	if (!printer.startPage()) {
	    return;
	}
	// Print headers
	currentY = printHeaders(gc, margin, currentY, columnWidths, lineHeight, headers);
	// Print data rows
	for (var i = 0; i < rowCount; i++) {
	    // Check if we need a new page
	    if (currentY + lineHeight > margin + printableHeight) {
		printer.endPage();
		if (!printer.startPage()) {
		    break;
		}
		currentY = margin;
		// Reprint headers on new page
		currentY = printHeaders(gc, margin, currentY, columnWidths, lineHeight, headers);
	    }

	    final var row = model.getRow(i);
	    currentY = printRow(gc, margin, currentY, columnWidths, lineHeight, row, columnCount);
	}
	printer.endPage();
    }
}
