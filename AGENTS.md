# CSVedit - Agent Development Guide

This guide provides coding agents with essential information about the CSVedit project structure, build commands, and code style guidelines.

## Project Overview

**Language**: Java 25  
**Build System**: Gradle (see `gradle/wrapper/gradle-wrapper.properties` for exact version)  
**UI Framework**: Eclipse SWT  
**Package**: `io.github.seerainer.csvedit`
**Architecture**: Desktop CSV/JSON/XML editor with native UI

## Build & Development Commands

### Essential Commands

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Run the application
./gradlew run

# Create native executable (GraalVM required)
./gradlew nativeCompile
```

### Testing Commands

```bash
# Run all tests
./gradlew test

# Run only unit tests
./gradlew unitTest

# Run only integration tests
./gradlew integrationTest

# Run tests with verbose output
./gradlew test --info

# Run a specific test class
./gradlew test --tests "io.github.seerainer.csvedit.model.CSVTableModelTest"

# Run a specific test method
./gradlew test --tests "io.github.seerainer.csvedit.model.CSVTableModelTest.testAddRow"

# Run tests matching a pattern
./gradlew test --tests "*CSV*Test"
```

### Linting & Quality

```bash
# Check code quality (uses Eclipse JDT rules)
./gradlew check

# View dependencies
./gradlew dependencies
```

## Project Structure

```
src/main/java/io/github/seerainer/csvedit/
├── CSVEdit.java              # Main application entry point
├── io/                       # File I/O (CSV, JSON, XML parsers)
├── model/                    # Data models (CSVTableModel, UndoRedoManager)
├── table/                    # Table UI components
├── theme/                    # Theme management
├── ui/                       # UI components and handlers
│   └── dialog/               # Dialog windows
└── util/                     # Utilities (Settings, PrintHandler)

src/test/java/                # Mirrors main structure with tests
```

## Code Style Guidelines

### Import Organization

1. Static imports first (alphabetically)
2. Standard imports grouped by package (alphabetically)
3. No wildcard imports (`import java.util.*` ❌)

```java
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.swt.widgets.Display;
```

### Naming Conventions

- **Classes**: PascalCase (`CSVTableModel`, `MainWindow`)
- **Methods**: camelCase (`loadCSV`, `getRowCount`)
- **Variables**: camelCase (`currentFilePath`, `isDirty`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_WINDOW_WIDTH`, `MAX_UNDO_STACK_SIZE`)
- **Packages**: lowercase with dots (`io.github.seerainer.csvedit.model`)

### Type Usage

- Use `final` for parameters and local variables that don't change
- Use `var` for type inference when the type is obvious from the right-hand side
- Prefer primitive types (`int`, `boolean`) over wrappers when possible

```java
public void processData(final String input) {
	final var result = parser.parse(input);  // Type is clear
	final int count = result.size();          // Explicit for primitives
}
```

### Formatting

- **Indentation**: Use tabs (not spaces)
- **Braces**: Opening brace on same line
- **Line length**: Keep under ~120 characters
- **Strings**: Use double quotes
- **No trailing whitespace**
- **Blank line between methods**

```java
public class Example {
	public void method() {
		if (condition) {
			doSomething();
		}
	}
	
	public void anotherMethod() {
		// Implementation
	}
}
```

### Error Handling

- Declare checked exceptions in method signatures
- Wrap lower-level exceptions with context
- Use try-with-resources for resource management
- Comment when intentionally ignoring exceptions

```java
public void loadFile(final Path path) throws IOException {
	try (final var reader = Files.newBufferedReader(path)) {
		// Process file
	} catch (final IOException e) {
		throw new IOException("Failed to load file: " + path, e);
	}
}
```

### Testing Patterns

- Use JUnit Jupiter assertions
- Tag tests: `@Tag("unit")` or `@Tag("integration")`
- Use AssertJ for fluent assertions
- Test class name: `<ClassName>Test`
- Test method name: `test<MethodName>_<Scenario>_<ExpectedResult>`

```java
@Tag("unit")
class CSVTableModelTest {
	
	@Test
	void testAddRow_validData_increasesRowCount() {
		final var model = new CSVTableModel();
		final int initialCount = model.getRowCount();
		
		model.addRow(List.of("value1", "value2"));
		
		assertThat(model.getRowCount()).isEqualTo(initialCount + 1);
	}
}
```

### Class Design Patterns

- **Utility classes**: Private constructor throwing `UnsupportedOperationException`
- **Immutability**: Use defensive copying when returning internal collections
- **Access modifiers**: Use package-private when appropriate, avoid unnecessary `public`
- **Singletons**: Use static factory methods or holder pattern

```java
public final class Utilities {
	private Utilities() {
		throw new UnsupportedOperationException("Utility class");
	}
	
	public static String format(final String input) {
		// Implementation
	}
}
```

### Modern Java Features

- Use lambdas and method references extensively
- Use Stream API for collection operations
- Use records for immutable data carriers (when appropriate)
- Use `var` for local variable type inference

```java
// Lambda expressions
button.addListener(SWT.Selection, event -> handleSelection());

// Method references
list.forEach(System.out::println);

// Streams
final var filtered = items.stream()
	.filter(item -> item.isValid())
	.collect(Collectors.toList());
```

## Dependencies

Key libraries used in this project:

- `com.github.seerainer:CSVparser:0.2.1` - CSV parsing
- `com.grack:nanojson:1.10` - JSON processing
- `org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:3.132.0` - SWT UI
- `org.junit.jupiter:junit-jupiter:6.0.3` - Testing
- `org.assertj:assertj-core:3.27.7` - Fluent assertions

## Important Notes

- This is a native desktop application using SWT (not web-based)
- Code is optimized for GraalVM native compilation
- Parallel test execution is enabled
- No Lombok or annotation processing used
- Eclipse JDT cleanup rules are enforced (see `.settings/org.eclipse.jdt.ui.prefs`)

## When Making Changes

1. Run tests before committing: `./gradlew test`
2. Ensure build passes: `./gradlew build`
3. Follow existing code patterns in the module you're editing
4. Add tests for new functionality (maintain coverage)
5. Update documentation if adding public APIs
