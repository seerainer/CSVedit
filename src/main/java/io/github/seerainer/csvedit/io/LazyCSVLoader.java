package io.github.seerainer.csvedit.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.ui.UIConstants;

/**
 * Lazy loader for CSV files that loads data in chunks to handle large files
 * efficiently
 */
public class LazyCSVLoader {

    private final String filePath;
    private final CSVTableModel model;
    private final ExecutorService executor;
    private List<String> headers;
    private volatile boolean cancelled = false;

    public LazyCSVLoader(final String filePath, final CSVTableModel model) {
	this.filePath = filePath;
	this.model = model;
	this.executor = Executors.newSingleThreadExecutor(r -> {
	    final var thread = new Thread(r, "CSV-Loader");
	    thread.setDaemon(true);
	    return thread;
	});
    }

    /**
     * Check if a file should be loaded lazily based on its size
     */
    public static boolean shouldUseLazyLoading(final String filePath) {
	try {
	    final var fileSize = Files.size(Paths.get(filePath));
	    return fileSize > UIConstants.LARGE_FILE_THRESHOLD_BYTES;
	} catch (final IOException e) {
	    return false;
	}
    }

    /**
     * Cancel the loading operation
     */
    public void cancel() {
	cancelled = true;
	executor.shutdownNow();
    }

    /**
     * Load the file asynchronously in the background
     */
    public CompletableFuture<Void> loadAsync(final ProgressCallback callback) {
	return CompletableFuture.runAsync(() -> {
	    try {
		loadFile(callback);
	    } catch (final Exception e) {
		if (!cancelled) {
		    callback.onError(e);
		}
	    }
	}, executor);
    }

    private void loadFile(final ProgressCallback callback) throws IOException {
	final List<List<String>> allData = new ArrayList<>(50000);
	headers = new ArrayList<>();

	final var lastProgressUpdate = new int[] { 0 }; // Use array to allow modification in lambda
	final var isFirstRowArray = new boolean[] { true }; // Use array to allow modification in lambda

	// Use callback-based streaming to avoid loading entire file into memory
	CSVParserUtil.parseFileWithCallback(Paths.get(filePath), record -> {
	    if (cancelled) {
		return;
	    }

	    final var row = CSVParserUtil.extractRow(record);
	    final var allEmpty = CSVParserUtil.isRowEmpty(row);

	    if (!allEmpty || !row.isEmpty()) {
		if (isFirstRowArray[0]) {
		    headers.addAll(row);
		    isFirstRowArray[0] = false;
		} else {
		    allData.add(row);
		    final var currentRowCount = allData.size();

		    // Update progress periodically
		    if (currentRowCount - lastProgressUpdate[0] >= UIConstants.PROGRESS_UPDATE_INTERVAL) {
			callback.onProgress(currentRowCount, -1, false);
			lastProgressUpdate[0] = currentRowCount;
		    }
		}
	    }
	});

	if (cancelled) {
	    return;
	}
	// Update model with all data
	model.clear();
	model.setHeaders(headers);
	model.setData(allData);
	model.normalize();
	callback.onProgress(allData.size(), allData.size(), true);
    }

    /**
     * Load just a preview of the file (first N rows)
     */
    public void loadPreview(final ProgressCallback callback) throws IOException {
	final List<List<String>> data = new ArrayList<>(UIConstants.CSV_PREVIEW_ROWS);
	headers = new ArrayList<>();

	// Read enough bytes for preview - estimate ~100 bytes per row
	final var previewBytes = (UIConstants.CSV_PREVIEW_ROWS + 1) * 100;
	final var bytes = FileOperations.isGzipFile(filePath) ? CSVParserUtil.readGzipFileBytes(filePath, previewBytes)
		: CSVParserUtil.readFileBytes(filePath, previewBytes);
	final var records = CSVParserUtil.parseCSV(bytes);

	var isFirstRow = true;
	var rowCount = 0;
	for (final var record : records) {
	    if (cancelled) {
		break;
	    }

	    final var row = CSVParserUtil.extractRow(record);

	    if (isFirstRow) {
		headers.addAll(row);
		isFirstRow = false;
	    } else {
		data.add(row);
		rowCount++;
		if (rowCount >= UIConstants.CSV_PREVIEW_ROWS) {
		    break;
		}
	    }
	}

	model.clear();
	model.setHeaders(headers);
	model.setData(data);
	model.normalize();

	callback.onProgress(data.size(), -1, false);
    }

    /**
     * Shutdown the executor
     */
    public void shutdown() {
	executor.shutdown();
    }

    public interface ProgressCallback {
	void onError(Exception e);

	void onProgress(int rowsLoaded, long totalRows, boolean isComplete);
    }
}