package io.github.seerainer.csvedit.ui;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import io.github.seerainer.csvedit.io.FileOperations;
import io.github.seerainer.csvedit.io.JSONOperations;
import io.github.seerainer.csvedit.io.LazyCSVLoader;
import io.github.seerainer.csvedit.io.XMLOperations;
import io.github.seerainer.csvedit.model.CSVTableModel;
import io.github.seerainer.csvedit.ui.dialog.DialogHelper;
import io.github.seerainer.csvedit.ui.dialog.ProgressDialog;
import io.github.seerainer.csvedit.util.Settings;

/**
 * Handles file operations including opening, importing, and drag-and-drop
 * functionality.
 */
public class FileOperationsHandler {

    private final Shell shell;
    private final Display display;
    private final Table table;
    private final CSVTableModel model;
    private final DialogHelper dialogHelper;

    private long lastLoadTimeMs = 0;

    public FileOperationsHandler(final Shell shell, final Display display, final Table table, final CSVTableModel model,
	    final DialogHelper dialogHelper) {
	this.shell = shell;
	this.display = display;
	this.table = table;
	this.model = model;
	this.dialogHelper = dialogHelper;
    }

    /**
     * Enables drag-and-drop file functionality on the table.
     */
    public void enableFileDrop(final ConfirmCallback confirmCallback, final FileLoadedCallback fileLoadedCallback) {
	final var dropTarget = createDropTarget();
	configureDropTargetTransfer(dropTarget);
	addDropListener(dropTarget, confirmCallback, fileLoadedCallback);
    }

    /**
     * Opens a file, automatically choosing lazy or regular loading based on file
     * size.
     */
    public void openFile(final File file, final FileLoadedCallback callback) {
	final var filePath = file.getAbsolutePath();

	if (shouldUseLazyLoading(filePath)) {
	    openLargeFile(filePath, callback);
	} else {
	    openSmallFile(filePath, callback);
	}
    }

    /**
     * Imports data from a JSON file.
     */
    public void importJSONFile(final String path, final FileLoadedCallback callback) {
	try {
	    JSONOperations.loadJSON(path, model);
	    lastLoadTimeMs = 0;
	    notifyFileLoaded(callback, null, true);
	} catch (final Exception e) {
	    dialogHelper.showError("Error importing from JSON", e.getMessage());
	}
    }

    /**
     * Imports data from an XML file.
     */
    public void importXMLFile(final String path, final FileLoadedCallback callback) {
	try {
	    XMLOperations.loadXML(path, model);
	    lastLoadTimeMs = 0;
	    notifyFileLoaded(callback, null, true);
	} catch (final Exception e) {
	    dialogHelper.showError("Error importing from XML", e.getMessage());
	}
    }

    /**
     * Returns the time taken to load the last file in milliseconds.
     */
    public long getLastLoadTimeMs() {
	return lastLoadTimeMs;
    }

    private DropTarget createDropTarget() {
	return new DropTarget(table, DND.DROP_COPY | DND.DROP_DEFAULT);
    }

    private static void configureDropTargetTransfer(final DropTarget dropTarget) {
	dropTarget.setTransfer(FileTransfer.getInstance());
    }

    private void addDropListener(final DropTarget dropTarget, final ConfirmCallback confirmCallback,
	    final FileLoadedCallback fileLoadedCallback) {
	dropTarget.addDropListener(new DropTargetAdapter() {
	    @Override
	    public void dragEnter(final DropTargetEvent event) {
		setDropDetailIfDefault(event);
	    }

	    @Override
	    public void dragOperationChanged(final DropTargetEvent event) {
		setDropDetailIfDefault(event);
	    }

	    @Override
	    public void dragOver(final DropTargetEvent event) {
		setDropDetailIfDefault(event);
	    }

	    @Override
	    public void drop(final DropTargetEvent event) {
		handleFileDrop(event, confirmCallback, fileLoadedCallback);
	    }
	});
    }

    private static void setDropDetailIfDefault(final DropTargetEvent event) {
	if (event.detail == DND.DROP_DEFAULT) {
	    event.detail = DND.DROP_COPY;
	}
    }

    private void handleFileDrop(final DropTargetEvent event, final ConfirmCallback confirmCallback,
	    final FileLoadedCallback fileLoadedCallback) {
	if (!isValidDropEvent(event)) {
	    return;
	}

	final var files = (String[]) event.data;
	if (!isValidFileData(files) || !confirmCallback.confirm()) {
	    return;
	}

	processDroppedFile(files[0], fileLoadedCallback);
    }

    private static boolean isValidDropEvent(final DropTargetEvent event) {
	return FileTransfer.getInstance().isSupportedType(event.currentDataType);
    }

    private static boolean isValidFileData(final String[] files) {
	return files != null && files.length > 0;
    }

    private void processDroppedFile(final String filePath, final FileLoadedCallback callback) {
	final var file = new File(filePath);
	final var fileName = file.getName().toLowerCase();

	if (fileName.endsWith(".json")) {
	    importJSONFile(filePath, callback);
	} else if (fileName.endsWith(".xml")) {
	    importXMLFile(filePath, callback);
	} else {
	    openFile(file, callback);
	}
    }

    private static boolean shouldUseLazyLoading(final String filePath) {
	return LazyCSVLoader.shouldUseLazyLoading(filePath);
    }

    private void openLargeFile(final String filePath, final FileLoadedCallback callback) {
	final var loader = new LazyCSVLoader(filePath, model);
	final var progressDialog = createProgressDialog("Loading Large File");

	configureProgressDialogForLoader(progressDialog, loader);
	progressDialog.open();
	progressDialog.updateStatus("Initializing...");

	final var startTime = System.currentTimeMillis();
	loadFileAsynchronously(loader, progressDialog, startTime, filePath, callback);
    }

    private ProgressDialog createProgressDialog(final String title) {
	return new ProgressDialog(shell, title);
    }

    private static void configureProgressDialogForLoader(final ProgressDialog progressDialog,
	    final LazyCSVLoader loader) {
	progressDialog.setCancelCallback(loader::cancel);
    }

    private void loadFileAsynchronously(final LazyCSVLoader loader, final ProgressDialog progressDialog,
	    final long startTime, final String filePath, final FileLoadedCallback callback) {
	CompletableFuture.runAsync(() -> {
	    try {
		loadPreviewPhase(loader, progressDialog);
		loadFullFilePhase(loader, progressDialog, startTime, filePath, callback);
	    } catch (final Exception e) {
		handleLoadError(progressDialog, loader, e);
	    }
	});
    }

    private void loadPreviewPhase(final LazyCSVLoader loader, final ProgressDialog progressDialog) {
	display.asyncExec(() -> progressDialog.updateStatus("Loading preview..."));
	try {
	    loader.loadPreview(createPreviewProgressCallback(progressDialog, loader));
	} catch (final Exception e) {
	    handleLoadError(progressDialog, loader, e);
	}
    }

    private LazyCSVLoader.ProgressCallback createPreviewProgressCallback(final ProgressDialog progressDialog,
	    final LazyCSVLoader loader) {
	return new LazyCSVLoader.ProgressCallback() {
	    @Override
	    public void onError(final Exception e) {
		display.asyncExec(() -> {
		    progressDialog.close();
		    loader.shutdown();
		    dialogHelper.showError("Failed to open file", e.getMessage());
		});
	    }

	    @Override
	    public void onProgress(final int rowsLoaded, final long totalRows, final boolean isComplete) {
		display.asyncExec(() -> {
		    recreateTableColumns();
		    progressDialog.updateProgress(rowsLoaded, totalRows, false);
		    progressDialog.updateStatus("Loading full file...");
		});
	    }
	};
    }

    private void recreateTableColumns() {
	table.setRedraw(false);
	try {
	    disposeExistingColumns();
	    createColumnsFromModel();
	} finally {
	    table.setRedraw(true);
	}
    }

    private void disposeExistingColumns() {
	for (final var col : table.getColumns()) {
	    col.dispose();
	}
    }

    private void createColumnsFromModel() {
	final var columnCount = model.getColumnCount();
	final var columnWidth = Settings.getColumnWidth();
	for (var i = 0; i < columnCount; i++) {
	    createTableColumn(i, columnWidth);
	}
    }

    private void createTableColumn(final int index, final int width) {
	final var column = new TableColumn(table, SWT.NONE);
	column.setText(model.getHeader(index));
	column.setWidth(width);
	column.setMoveable(true);
    }

    private void loadFullFilePhase(final LazyCSVLoader loader, final ProgressDialog progressDialog,
	    final long startTime, final String filePath, final FileLoadedCallback callback) {
	loader.loadAsync(createFullFileProgressCallback(progressDialog, loader, startTime, filePath, callback));
    }

    private LazyCSVLoader.ProgressCallback createFullFileProgressCallback(final ProgressDialog progressDialog,
	    final LazyCSVLoader loader, final long startTime, final String filePath,
	    final FileLoadedCallback callback) {
	return new LazyCSVLoader.ProgressCallback() {
	    @Override
	    public void onError(final Exception e) {
		display.asyncExec(() -> {
		    progressDialog.close();
		    loader.shutdown();
		    if (!progressDialog.isCancelled()) {
			dialogHelper.showError("Failed to load complete file", e.getMessage());
		    }
		});
	    }

	    @Override
	    public void onProgress(final int rowsLoaded, final long totalRows, final boolean isComplete) {
		if (progressDialog.isCancelled()) {
		    return;
		}
		display.asyncExec(() -> {
		    progressDialog.updateProgress(rowsLoaded, totalRows, isComplete);
		    if (isComplete) {
			finalizeFileLoad(progressDialog, loader, startTime, filePath, callback);
		    }
		});
	    }
	};
    }

    private void finalizeFileLoad(final ProgressDialog progressDialog, final LazyCSVLoader loader, final long startTime,
	    final String filePath, final FileLoadedCallback callback) {
	lastLoadTimeMs = System.currentTimeMillis() - startTime;
	progressDialog.close();
	loader.shutdown();
	notifyFileLoaded(callback, filePath, false);
    }

    private void handleLoadError(final ProgressDialog progressDialog, final LazyCSVLoader loader, final Exception e) {
	display.asyncExec(() -> {
	    progressDialog.close();
	    loader.shutdown();
	    dialogHelper.showError("Failed to open file", e.getMessage());
	});
    }

    private void openSmallFile(final String filePath, final FileLoadedCallback callback) {
	final var startTime = System.currentTimeMillis();
	try {
	    loadSmallFileData(filePath);
	    calculateLoadTime(startTime);
	    notifyFileLoaded(callback, filePath, false);
	} catch (final Exception ex) {
	    dialogHelper.showError("Failed to open file", ex.getMessage());
	}
    }

    private void loadSmallFileData(final String filePath) throws Exception {
	FileOperations.loadCSV(filePath, model);
    }

    private void calculateLoadTime(final long startTime) {
	lastLoadTimeMs = System.currentTimeMillis() - startTime;
    }

    private static void notifyFileLoaded(final FileLoadedCallback callback, final String filePath,
	    final boolean imported) {
	if (callback != null) {
	    callback.onFileLoaded(filePath, imported);
	}
    }

    /**
     * Callback interface to confirm if changes should be discarded.
     */
    @FunctionalInterface
    public interface ConfirmCallback {
	boolean confirm();
    }

    /**
     * Callback interface for when a file has been loaded.
     */
    @FunctionalInterface
    public interface FileLoadedCallback {
	void onFileLoaded(String filePath, boolean imported);
    }
}
