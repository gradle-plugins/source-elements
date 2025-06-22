package dev.nokee.elements.core;

import java.nio.file.Path;

// TODO: Maybe do not extends from Element as from this point forward, we are talking in terms of file on disk as opposed to some logical grouping of files
public abstract class LayoutElement {
	public WritableElement applyTo(Element element) {
		return new WritableElement() {
			@Override
			public void writeToDirectory(Path directory) {
				new Context(directory).visit(element);
			}
		};
	}

	protected abstract void visit(Element element, Context context);

	protected final void visit(SourceFile file, Context context) {
		System.out.println("==> " + context.location.resolve(file.getPath()).resolve(file.getName()));
		file.writeToDirectory(context.location);
	}

	public final class Context {
		private final Path location;

		private Context(Path location) {
			this.location = location;
		}

		public Context dir(String path) {
			return new Context(location.resolve(path));
		}

		public void visit(Element element) {
			LayoutElement.this.visit(element, this);
		}

		public void visit(SourceFile sourceFile) {
			LayoutElement.this.visit(sourceFile, this);
		}
	}
}
