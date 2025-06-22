package dev.nokee.elements.core;

import java.nio.file.Path;

// TODO: Maybe do not extends from Element as from this point forward, we are talking in terms of file on disk as opposed to some logical grouping of files
public abstract class LayoutElement implements WritableElement {
	private final Element element;

	// TODO: Use something else then Element...
	public LayoutElement(Element element) {
		this.element = element;
	}

	/**
	 * {@inheritDoc}
	 */
	// SHOULD return a FileSystemElement that represent this layout applied to the element to the specified directory
	public void writeToDirectory(Path directory) {
		visit(element, new Context(directory));
	}

	protected abstract void visit(Element element, Context context);

	protected final void visit(SourceFile file, Context context) {
		System.out.println("==> " + context.location.resolve(file.getPath()).resolve(file.getName()));
		file.writeToDirectory(context.location);
	}

	public static final class Context {
		private final Path location;

		private Context(Path location) {
			this.location = location;
		}

		public Context dir(String path) {
			return new Context(location.resolve(path));
		}
	}
}
