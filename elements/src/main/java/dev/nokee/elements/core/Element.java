package dev.nokee.elements.core;

import java.util.Arrays;

/**
 * Represents an logical source element.
 */
public abstract class Element {
	/**
	 * Returns a source element that contains the given files.
	 *
	 * @param sourceFiles  the source files of the element
	 * @return a new source element containing the specified source files.
	 */
	protected static SourceElement ofFiles(SourceFile... sourceFiles) {
		return SourceElement.ofFiles(Arrays.asList(sourceFiles));
	}

	public abstract void accept(Visitor visitor);

	public interface Visitor {
		void visit(Element element);
	}

	/**
	 * Creates a source file represented by the specified source path and content.
	 *
	 * @param sourcePath  the file path (relative to the source set directory)
	 * @param content  the file content
	 * @return a new source file
	 */
	protected static SourceFile sourceFile(String sourcePath, String content) {
		return SourceFile.of(sourcePath, content);
	}

	protected static SourceFile sourceFile(String sourcePath, String... lines) {
		return SourceFile.of(sourcePath, String.join("\n", lines));
	}
}
