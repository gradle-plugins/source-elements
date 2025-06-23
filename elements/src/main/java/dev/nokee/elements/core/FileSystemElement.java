package dev.nokee.elements.core;

import java.nio.file.Path;

public class FileSystemElement extends Element implements WritableElement {
	private final Path location;
	private final SourceElement sources;

	public FileSystemElement(Path location, SourceElement sources) {
		this.location = location;
		this.sources = sources;
	}

	@Override
	public FileSystemElement writeToDirectory(Path directory) {
		for (SourceFile file : sources.getFiles()) {
			file.writeToDirectory(directory);
		}
		return new FileSystemElement(directory, sources);
	}

	public FileSystemElement apply(IncrementalElement.ChangeVisitor transform) {
		return new FileSystemElement(location, transform.visit(location, sources));
	}

	@Override
	public void accept(Visitor visitor) {
		throw new UnsupportedOperationException();
	}
}
