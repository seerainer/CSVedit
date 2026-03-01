# 📊 CSV Editor

<div align="center">

**A modern, feature-rich CSV editor built with Java and SWT**

![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)
![Tests](https://img.shields.io/badge/Tests-83%20Passing-success?style=flat-square)
![Version](https://img.shields.io/badge/Version-0.2.0-green?style=flat-square)

[Features](#-features) • [Installation](#-installation) • [Usage](#-usage) • [Architecture](#%EF%B8%8F-architecture) • [Documentation](#-documentation)

</div>

---

## ✨ Features

### 🎯 Core Capabilities

<table>
<tr>
<td width="50%">

#### 📝 **Editing & Navigation**
- ✏️ Double-click cell editing
- 🏷️ Editable column headers
- ⌨️ Keyboard shortcuts
- 🖱️ Context menu support
- 🎨 Dark theme support

</td>
<td width="50%">

#### 💾 **File Operations**
- 📂 Open/Save CSV files
- 📥 Import JSON/XML
- 📤 Export JSON/XML
- 🎯 Drag & drop files
- 🔄 Auto-save (30s)

</td>
</tr>
<tr>
<td width="50%">

#### 🔍 **Data Management**
- ↩️ Undo/Redo (100 actions)
- 🔎 Search and replace
- 🔢 Smart column sorting
- ➕ Add/Delete rows & columns
- 📊 Large file support (>10MB)
- 🔄 Duplicate row detection

</td>
<td width="50%">

#### ⚙️ **Customization**
- 🎨 Theme management
- 🔧 Comprehensive settings
- 🖋️ Font customization
- 📐 Configurable CSV parsing
- 💾 Persistent preferences
- 🖼️ Optimized icon loading

</td>
</tr>
</table>

---

## 🚀 Installation

### Prerequisites

- ☕ **Java 25+** (with GraalVM support for native compilation)
- 📦 **Gradle** (wrapper included)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/yourusername/CSVedit.git
cd CSVedit

# Build the project
./gradlew build

# Run the application
./gradlew run
```

### Build Options

```bash
# Create executable JAR
./gradlew jar

# Create distribution packages
./gradlew distZip distTar

# Build native image (GraalVM required)
./gradlew nativeCompile

# Run tests
./gradlew test
```

## 📦 Dependencies

The project declares its dependencies in `build.gradle`. Below are the main runtime, build-time (native), and test dependencies with the versions currently used in this repository.

- Runtime / Implementation
  - Java: Java 25 (configured via Gradle toolchain)
  - CSV parsing library: `com.github.seerainer:CSVparser:0.2.1`
  - JSON utilities: `com.grack:nanojson:1.10`
  - SWT (GUI): `org.eclipse.platform:org.eclipse.swt.<platform>:3.132.0` (platform-specific artifact is selected dynamically in `build.gradle` via `detectSwtArtifact()`; the artifact suffix depends on OS and architecture)

- Test
  - JUnit Jupiter: `org.junit.jupiter:junit-jupiter:6.0.3`
  - AssertJ (assertions): `org.assertj:assertj-core:3.27.7`

---

## 📖 Usage

### 🎬 Getting Started

1. **Launch** the editor: `./gradlew run`
2. **Open** a CSV file: `Ctrl+O` or drag & drop
3. **Edit** cells: Double-click any cell
4. **Save** changes: `Ctrl+S`

### ⌨️ Keyboard Shortcuts

| Action | Shortcut | Description |
|--------|----------|-------------|
| 📄 New | `Ctrl+N` | Create new CSV file |
| 📂 Open | `Ctrl+O` | Open existing file |
| 💾 Save | `Ctrl+S` | Save current file |
| 💾 Save As | `Ctrl+Shift+S` | Save with new name |
| ↩️ Undo | `Ctrl+Z` | Undo last action |
| ↪️ Redo | `Ctrl+Y` | Redo last undone action |
| ➕ Add Row | `Ctrl+R` | Insert new row |
| ➕ Add Column | `Ctrl+L` | Insert new column |
| ❌ Delete Row | `Ctrl+D` | Delete selected row |
| ⬆️ Move Row Up | `Ctrl+Up` | Move selected row up |
| ⬇️ Move Row Down | `Ctrl+Down` | Move selected row down |
| 🔍 Find | `Ctrl+F` | Open search dialog |
| 📝 Text Editor | `Ctrl+T` | Edit as plain text |
| 🔄 Refresh | `F5` | Refresh display |

---

## 🗜️ Gzip compression support

CSVedit can open and save gzip-compressed CSV files using the `.csv.gz` extension. This lets you work with compressed CSVs directly (open/edit/save) without manually decompressing or recompressing files.

---

## 🎛️ Menu Bar

### 📁 File Menu
- **New** (`Ctrl+N`) - Create a new CSV file
- **Open** (`Ctrl+O`) - Open an existing CSV file
- **Save** (`Ctrl+S`) - Save the current file
- **Save As** (`Ctrl+Shift+S`) - Save with a new filename
- **Import** → From JSON/XML - Import data from various formats
- **Export** → To JSON/XML - Export data to various formats
- **Exit** - Close the application

### ✏️ Edit Menu
- **Undo** (`Ctrl+Z`) - Undo the last action
- **Redo** (`Ctrl+Y`) - Redo the previously undone action
- **Add Row** (`Ctrl+R`) - Add a new row to the table
- **Add Column** (`Ctrl+L`) - Add a new column to the table
- **Delete Row** (`Ctrl+D`) - Delete the selected row
- **Delete Column** - Remove the last column
- **Move Row Up** (`Ctrl+Up`) - Move selected row up one position
- **Move Row Down** (`Ctrl+Down`) - Move selected row down one position
- **Find** (`Ctrl+F`) - Search for text in the table

### 👁️ View Menu
- **Refresh** (`F5`) - Refresh the table display
- **Text Editor** (`Ctrl+T`) - Edit CSV data as plain text
- **Settings** - Configure application preferences

### ❓ Help Menu
- **About** - Display application information

---

## 🔧 Settings & Configuration

### ⚙️ Settings Dialog (6 Tabs)

<details>
<summary><b>📋 CSV Configuration</b></summary>

- **Field Delimiter** - Character to separate fields (default: comma)
- **Quote Character** - Character for quoting special fields (default: `"`)
- **Escape Character** - Character to escape quotes (default: `"`)
- **Trim Whitespace** - Remove leading/trailing spaces
- **Detect BOM** - Handle UTF-8 BOM detection
- **Performance Settings** - Buffer size, max field size

</details>

<details>
<summary><b>🔍 Parsing Options</b></summary>

- **Preserve Empty Fields** - Keep empty fields in data
- **Skip Empty Lines** - Ignore empty data lines
- **Skip Blank Lines** - Ignore completely blank lines
- **Null Value Handling** - Custom null representation

</details>

<details>
<summary><b>🚀 Advanced Parsing</b></summary>

- **Quoting Options** - Strict quoting rules
- **Line Ending Options** - Normalize line endings
- **Record Validation** - Max record length, error handling
- **Debugging** - Track field positions

</details>

<details>
<summary><b>🖥️ UI Options</b></summary>

- **Default Table Size** - Rows (1-1000), Columns (1-100)
- **Column Width** - 50-1000 pixels (default: 150)
- **Display Options** - Show grid lines
- **Behavior** - Auto-save, delete confirmations

</details>

<details>
<summary><b>📂 File Options</b></summary>

- **Character Encoding** - UTF-8, UTF-16, ISO-8859-1, Windows-1252, US-ASCII
- **Line Ending** - System, Windows (CRLF), Unix (LF), Mac (CR)

</details>

<details>
<summary><b>🖋️ Font Options</b></summary>

- **Font Selection** - Family, size, style
- **Live Preview** - See changes in real-time
- **Custom Fonts** - Choose any system font

</details>

---

## 🏗️ Architecture

### 📦 Core Components

```
┌─────────────────────────────────────────────────────────┐
│                     Main.java                           │
│              (Application Entry Point)                  │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                  MainWindow.java                        │
│         (UI Manager & Event Coordinator)                │
└─┬─────────┬─────────┬─────────┬─────────┬───────────────┘
  │         │         │         │         │
  ▼         ▼         ▼         ▼         ▼
┌────┐  ┌──────┐  ┌──────┐  ┌──────┐  ┌────────┐
│Data│  │Table │  │File  │  │Theme │  │Settings│
│Mgmt│  │Mgmt  │  │Ops   │  │Mgr   │  │Dialog  │
└────┘  └──────┘  └──────┘  └──────┘  └────────┘
```

### 🧩 Key Classes

| Component | Responsibility |
|-----------|---------------|
| **CSVTableModel** | Data storage & manipulation |
| **TableManager** | Table UI operations |
| **FileOperations** | CSV file I/O with CSVParser |
| **JSONOperations** | JSON import/export |
| **XMLOperations** | XML import/export |
| **UndoRedoManager** | Command pattern for undo/redo |
| **FindReplaceDialog** | Advanced search & replace |
| **LazyCSVLoader** | Large file handling with async loading |
| **ThemeManager** | Dark/light theme support |
| **SettingsDialog** | Configuration UI with 6 tabs |
| **Icons** | Centralized icon management (singleton) |
| **DuplicateRowsDialog** | Find and remove duplicate rows |

## 🧪 Testing

### ✅ Test Coverage

```
📊 Total Tests: 83
✅ Passing: 83 (100%)
❌ Failing: 0
```

### 🧪 Running Tests

```bash
# All tests
./gradlew test

# Unit tests only
./gradlew unitTest

# Integration tests only
./gradlew integrationTest

# With detailed output
./gradlew test --info
```

---

## 📚 Documentation

### 🎨 Supported Formats

#### CSV Format
- ✅ Standard comma-separated values
- ✅ Custom delimiters
- ✅ Quoted fields with commas
- ✅ Escaped quotes (`""`)
- ✅ Multi-line values
- ✅ Empty fields
- ✅ UTF-8 BOM detection

#### JSON Format
```json
{
  "headers": ["Name", "Age", "City"],
  "rows": [
    ["Alice", "30", "New York"],
    ["Bob", "25", "London"]
  ]
}
```

#### XML Format
```xml
<csv>
  <headers>
    <header>Name</header>
    <header>Age</header>
  </headers>
  <rows>
    <row>
      <cell>Alice</cell>
      <cell>30</cell>
    </row>
  </rows>
</csv>
```

---

## 💡 Advanced Features

### 🔍 Find & Replace Dialog
- 🔤 Case-sensitive/insensitive matching
- 📝 Whole word matching
- 🔄 Find Next / Replace / Replace All
- 🎯 Cell-by-cell navigation

### 🔄 Duplicate Row Detection
- 📊 Scan entire dataset for duplicates
- 👁️ Visual preview of duplicate rows
- 🎯 Selective or batch removal
- 📈 Real-time statistics
- ✅ Undo support for removals

### ⚡ Performance
- 📦 Lazy loading for large files (>10MB)
- 📊 Progress dialog with cancel button
- 💾 Efficient memory usage with resource pooling
- 🚀 Chunk-based async processing
- ⏱️ Background loading with progress updates
- 🧹 Automatic resource cleanup

### 🎨 User Experience
- 🌓 Automatic dark theme detection
- 💾 Dirty state tracking with visual indicator (*)
- ⚠️ Smart confirmation dialogs
- 📊 Status bar with file statistics
- 🖱️ Intuitive right-click context menus
- 🔄 Auto-save every 30 seconds
- ⌨️ Comprehensive keyboard shortcuts
- 🎯 Drag & drop support for CSV/JSON/XML

---

## 🛠️ Technical Stack

| Technology | Purpose |
|------------|---------|
| **Java 25** | Core language with modern features |
| **SWT** | Native UI framework (Eclipse Standard Widget Toolkit) |
| **CSVParser v0.2.1** | Robust, configurable CSV parsing |
| **JUnit Jupiter 6.0.3** | Modern testing framework |
| **AssertJ 3.27.7** | Fluent test assertions |
| **GraalVM** | Native image compilation support |
| **Gradle 9.3.1** | Build automation & dependency management |

---

## 🎯 Design Principles

1. **🏗️ Separation of Concerns** - Clear Model-View separation
2. **🧪 Test-Driven Development** - Comprehensive unit & integration tests
3. **⚡ Performance First** - Optimized for large datasets
4. **🎨 User-Centric Design** - Intuitive UI with helpful feedback
5. **🔧 Highly Configurable** - Extensive customization options
6. **📦 Zero Configuration** - Works out of the box with sensible defaults
7. **🌍 Cross-Platform** - Windows, macOS, Linux support
8. **🧹 Resource Management** - Proper lifecycle management following SWT best practices
9. **♿ Accessibility** - Keyboard navigation and screen reader support
10. **🔒 Data Integrity** - Undo/redo with data validation

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to fork and submit a Pull Request.

---

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

<div align="center">

**Made with ☕ and ❤️**

⭐ Star this repo if you find it useful!

</div>
