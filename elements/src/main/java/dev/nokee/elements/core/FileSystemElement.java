package dev.nokee.elements.core;

import java.nio.file.Path;
import java.util.List;

public class FileSystemElement extends Element implements WritableElement {
	private final Path location;
	private final List<SourceFile> files;

	public FileSystemElement(Path location, List<SourceFile> files) {
		this.location = location;
		this.files = files;
	}

	@Override
	public FileSystemElement writeToDirectory(Path directory) {
		for (SourceFile file : files) {
			file.writeToDirectory(directory);
		}
		return new FileSystemElement(directory, files);
	}

	public FileSystemElement apply(Object transform) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void accept(Visitor visitor) {
		throw new UnsupportedOperationException();
	}
}
