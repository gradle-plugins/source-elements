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

	public static IncrementalElement transform(SourceElement beforeElement, SourceElement afterElement) {
		return new IncrementalElement() {
			@Override
			protected List<Transform> getIncrementalChanges() {
				return Collections.singletonList(new Transform() {
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
				});
			}
		};
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
	 */
	protected static Transform rename(SourceElement beforeElement) {
		return rename(beforeElement, name -> "renamed-" + name);
	}

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
