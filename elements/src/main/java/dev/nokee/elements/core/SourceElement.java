package dev.nokee.elements.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represent an element containing zero or more source files.
 */
public abstract class SourceElement extends Element implements WritableElement {
	/**
	 * {@return the source files associated with this element, possibly none.}
	 */
	public abstract List<SourceFile> getFiles();

	/**
	 * {@inheritDoc}
	 */
	public FileSystemElement writeToDirectory(Path directory) {
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToDirectory(directory);
		}
		return new FileSystemElement() {
			// FIXME: implements
		};
	}

	/**
	 * {@return empty source element.}
	 */
	public static SourceElement empty() {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.emptyList();
			}
		};
	}

	public static SourceElement ofElements(SourceElement... elements) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Stream.of(elements).flatMap(it -> it.getFiles().stream()).collect(Collectors.toList());
			}
		};
	}

	public static SourceElement ofElements(List<SourceElement> elements) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return elements.stream().flatMap(it -> it.getFiles().stream()).collect(Collectors.toList());
			}

			@Override
			public void accept(Visitor visitor) {
				for (SourceElement element : elements) {
					visitor.visit(element);
				}
			}
		};
	}

	/**
	 * Returns a source element that contains the given files.
	 *
	 * @param sourceFiles  the source files of the element
	 * @return a new source element containing the specified source files.
	 */
	public static SourceElement ofFiles(List<SourceFile> sourceFiles) {
		final List<SourceFile> files = Collections.unmodifiableList(new ArrayList<>(sourceFiles));
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return files;
			}
		};
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getFiles());
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SourceElement)) return false;

		return getFiles().equals(((SourceElement) obj).getFiles());
	}
}
