package dev.nokee.elements.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// TODO: Maybe do not extends from Element as from this point forward, we are talking in terms of file on disk as opposed to some logical grouping of files
public abstract class LayoutElement {
	public WritableElement applyTo(Element element) {
		return new WritableElement() {
			@Override
			public FileSystemElement writeToDirectory(Path directory) {
				Context context = new Context();
				context.visit(element);
				return new FileSystemElement(Paths.get(""), context.allFiles).writeToDirectory(directory);
			}
		};
	}

	protected abstract void visit(Element element, Context context);

	public final class Context {
		private final Path location;
		private final List<SourceFile> allFiles;

		private Context() {
			this(Paths.get(""), new ArrayList<>());
		}

		private Context(Path location, List<SourceFile> allFiles) {
			this.location = location;
			this.allFiles = allFiles;
		}

		public Context dir(String path) {
			return new Context(location.resolve(path), allFiles);
		}

		public void visit(Element element) {
			LayoutElement.this.visit(element, this);
		}

		public void visit(SourceFile sourceFile) {
			allFiles.add(sourceFile.withPath(location::resolve));
		}
	}
}
