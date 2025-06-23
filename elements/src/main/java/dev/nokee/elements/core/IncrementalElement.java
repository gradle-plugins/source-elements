package dev.nokee.elements.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Represent a source element with transformation changes.
 * Use {@link IncrementalElement#allChanges()} with {@link FileSystemElement#apply(ChangeVisitor)} to apply all incremental changes.
 */
public abstract class IncrementalElement extends SourceElement {
	private final OriginalElement original = new OriginalElement();
	private final AlternateElement alternate = new AlternateElement();

	public final OriginalElement getOriginalElement() {
		return original;
	}

	public final AlternateElement getAlternateElement() {
		return alternate;
	}

	protected abstract List<Transform> getIncrementalChanges();

	@Override
	public final List<SourceFile> getFiles() {
		return getOriginalElement().getFiles();
	}

	public interface Transform {
		void applyChangesTo(Path directory);

		List<SourceFile> getBeforeFiles();

		List<SourceFile> getAfterFiles();
	}

	/**
	 * Returns a transform that will replace the before element with the after element.
	 *
	 * @param beforeElement  the sources to replace
	 * @param afterElement  the replaced sources
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	public static Transform replace(SourceElement beforeElement, SourceElement afterElement) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				try {
					for (SourceFile beforeFile : getBeforeFiles()) {
						Files.delete(directory.resolve(beforeFile.getPath()).resolve(beforeFile.getName()));
					}

					afterElement.writeToDirectory(directory);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return beforeElement.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return afterElement.getFiles();
			}
		};
	}

	/**
	 * Returns a transform that keep the before element intact.
	 *
	 * @param element  the sources to preserve
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform preserve(final SourceElement element) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return element.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return element.getFiles();
			}
		};
	}

	/**
	 * Returns a transform that replace the content of the before element with the content of the after element.
	 * Both elements must have the same location.
	 *
	 * @param beforeElement  the original sources
	 * @param afterElement  the modified sources
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform modify(final SourceElement beforeElement, final SourceElement afterElement) {
		assert hasSameFiles(beforeElement.getFiles(), afterElement.getFiles());

		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				afterElement.writeToDirectory(directory);
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return beforeElement.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return afterElement.getFiles();
			}
		};
	}

	private static boolean hasSameFiles(Collection<SourceFile> beforeFiles, Collection<SourceFile> afterFiles)  {
		if (beforeFiles.size() != afterFiles.size()) {
			return false;
		}

		for (SourceFile beforeFile : beforeFiles) {
			boolean found = false;
			for (SourceFile afterFile : afterFiles) {
				if (beforeFile.getName().equals(afterFile.getName()) && beforeFile.getPath().equals(afterFile.getPath())) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns a transform that delete the before element.
	 *
	 * @param beforeElement  the sources to delete
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform delete(final SourceElement beforeElement) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				for (SourceFile file : beforeElement.getFiles()) {
					Path path = directory.resolve(file.getPath()).resolve(file.getName());
					if (!Files.exists(path)) {
						throw new IllegalStateException();
					}

					try {
						Files.delete(path);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return beforeElement.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return Collections.emptyList();
			}
		};
	}

	/**
	 * Returns a transform that add the after element.
	 *
	 * @param afterElement  the sources to add
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform add(final SourceElement afterElement) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				afterElement.writeToDirectory(directory);
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return Collections.emptyList();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return afterElement.getFiles();
			}
		};
	}

	/**
	 * Represent a transform that moves the before element to the destination path.
	 * The sources must move to a new directory.
	 *
	 * @param beforeElement  the sources to move
	 * @param destinationPath  the destination path
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform move(SourceElement beforeElement, String destinationPath) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				try {
					Path destPath = Files.createDirectories(directory.resolve(destinationPath));
					for (SourceFile file : beforeElement.getFiles()) {
						assert !file.getPath().equals(destinationPath);
						Path src = directory.resolve(file.getPath()).resolve(file.getName());
						Path dst = destPath.resolve(file.getName());

						Files.move(src, dst);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return beforeElement.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return beforeElement.getFiles().stream().map(it -> it.withPath(path -> Paths.get(destinationPath).resolve(path.getFileName()))).collect(Collectors.toList());
			}
		};
	}

	/**
	 * Returns a transform that rename the before element to {@code renamed-} followed by the original name.
	 *
	 * @param beforeElement  the sources to rename
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform rename(SourceElement beforeElement) {
		return rename(beforeElement, name -> "renamed-" + name);
	}

	/**
	 * Returns a transform that rename the element using the specified filename operation.
	 * The renamed source files cannot overwrite another source file.
	 *
	 * @param beforeElement  the sources to rename
	 * @param renameOperation  the rename operation
	 * @return a transform to use in {@link IncrementalElement#getIncrementalChanges()}.
	 */
	protected static Transform rename(SourceElement beforeElement, UnaryOperator<String> renameOperation) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				for (SourceFile file : beforeElement.getFiles()) {
					Path path = directory.resolve(file.getPath()).resolve(file.getName());
					if (!Files.exists(path)) {
						throw new IllegalStateException();
					}

					try {
						Files.move(path, path.getParent().resolve(renameOperation.apply(file.getName())));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return beforeElement.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return beforeElement.getFiles().stream().map(it -> it.withName(renameOperation)).collect(Collectors.toList());
			}
		};
	}

	/**
	 * Represent the source element before applying the changes
	 */
	public final class OriginalElement extends SourceElement {
		@Override
		public List<SourceFile> getFiles() {
			return getIncrementalChanges().stream().flatMap(it -> it.getBeforeFiles().stream()).collect(Collectors.toList());
		}
	}

	/**
	 * Represent the source element after applying the changes.
	 */
	public final class AlternateElement extends SourceElement {
		@Override
		public List<SourceFile> getFiles() {
			return getIncrementalChanges().stream().flatMap(it -> it.getAfterFiles().stream()).collect(Collectors.toList());
		}
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public static ChangeVisitor allChanges() {
		return new ChangeVisitor() {
			@Override
			public SourceElement visit(Path location, SourceElement element) {
				if (element instanceof IncrementalElement) {
					for (Transform change : ((IncrementalElement) element).getIncrementalChanges()) {
						change.applyChangesTo(location);
					}
					// TODO: Would have to keep the identifier
					return ((IncrementalElement) element).getAlternateElement();
				}
				return element;
			}
		};
	}

	// TODO: Move outside of this class
	//   Not sure where it should be moved
	public interface ChangeVisitor {
		SourceElement visit(Path location, SourceElement element);
	}
}
