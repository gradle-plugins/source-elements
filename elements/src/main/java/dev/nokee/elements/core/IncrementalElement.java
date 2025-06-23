package dev.nokee.elements.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class IncrementalElement extends SourceElement {
	private final OriginalElement original = new OriginalElement();
	private final AlternateElement alternate = new AlternateElement();

	public OriginalElement getOriginalElement() {
		return original;
	}

	public AlternateElement getAlternateElement() {
		return alternate;
	}

	protected abstract List<Transform> getIncrementalChanges();

	@Override
	public List<SourceFile> getFiles() {
		return getOriginalElement().getFiles();
	}

	public interface Transform {
		void applyChangesTo(Path directory);

		List<SourceFile> getBeforeFiles();

		List<SourceFile> getAfterFiles();
	}

	/**
	 * Returns a transform that keep the before element intact.
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
	 */
	protected static Transform delete(final SourceElement beforeElement) {
		return new Transform() {
			@Override
			public void applyChangesTo(Path directory) {
				for (SourceFile file : beforeElement.getFiles()) {
					if (!Files.exists(directory.resolve(file.getPath()).resolve(file.getName()))) {
						throw new IllegalStateException();
					}

					try {
						Files.delete(directory.resolve(file.getPath()).resolve(file.getName()));
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

	protected static Transform move(SourceElement beforeElement, String destinationPath) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Generic transform class that rename the source of the before element.
	 */
	protected static abstract class AbstractRenameTransform implements Transform {
		public static final String DEFAULT_RENAME_PREFIX = "renamed-";
		private final SourceFile sourceFile;
		private final SourceFile destinationFile;
		private final SourceElement beforeElement;

		AbstractRenameTransform(SourceFile sourceFile, SourceFile destinationFile, SourceElement beforeElement) {
			this.sourceFile = sourceFile;
			this.destinationFile = destinationFile;
			this.beforeElement = beforeElement;
		}

//		@Override
//		public void applyChangesToProject(TestFile projectDir) {
//			String sourceSetName = beforeElement.getSourceSetName();
//			TestFile file = projectDir.file(sourceFile.withPath("src/" + sourceSetName));
//
//			file.assertExists();
//
//			file.renameTo(projectDir.file(destinationFile.withPath("src/" + sourceSetName)));
//		}

		@Override
		public List<SourceFile> getBeforeFiles() {
			return beforeElement.getFiles();
		}
	}

	public final class OriginalElement extends SourceElement {
		@Override
		public List<SourceFile> getFiles() {
			return getIncrementalChanges().stream().flatMap(it -> it.getBeforeFiles().stream()).collect(Collectors.toList());
		}
	}

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

	public interface ChangeVisitor {
		SourceElement visit(Path location, SourceElement element);
	}
}
