package dev.nokee.elements.core;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represent an element containing zero or more source files.
 */
// TODO: Maybe this should extends from FileSystemElement
//   this could means we could do IncrementalElement.apply(allChanges()).writeToDirectory(...)
//   IncrementalElement should probably have a shortcut IncrementalElement.applyAllChanges()
// TODO: We should probably rename this to SourceSetElement and use SourceElement as the general interface representing source
//   We need to prevent nesting project element AKA project element should contains only SourceElement (the general concept of a source element)
public abstract class SourceElement extends Element implements WritableElement {
	/**
	 * {@return the source files associated with this element, possibly none.}
	 */
	public abstract List<SourceFile> getFiles();

	/**
	 * {@inheritDoc}
	 */
	public final FileSystemElement writeToDirectory(Path directory) {
		getFiles().forEach(it -> it.writeToDirectory(directory));
		return new FileSystemElement(directory, this);
	}

	public final Object asZip(String path) {
		return new Object() {
			public void writeToDirectory(Path directory) throws IOException {
				URI uri = URI.create("jar:file:" + directory.resolve(path));
				try (FileSystem zipfs = FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"))) {
					SourceElement.this.writeToDirectory(zipfs.getPath("/"));
					Files.walkFileTree(zipfs.getPath("/"), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path sourcePath, BasicFileAttributes attrs) throws IOException {
							BasicFileAttributeView view = Files.getFileAttributeView(sourcePath, BasicFileAttributeView.class);
							view.setTimes(FileTime.fromMillis(0), FileTime.fromMillis(0), FileTime.fromMillis(0));
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}
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
		return ofElements(Arrays.asList(elements));
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
					element.accept(visitor);
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
