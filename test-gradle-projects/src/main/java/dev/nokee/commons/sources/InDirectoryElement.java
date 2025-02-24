package dev.nokee.commons.sources;

import dev.gradleplugins.fixtures.sources.Element;
import dev.gradleplugins.fixtures.sources.ProjectElement;

import java.nio.file.Path;

// TODO: Template param should extends from Element
public class InDirectoryElement<T extends ProjectElement> extends Element {
	private final Path path;
	private final T element;

	public InDirectoryElement(Path path, T element) {
		this.path = path;
		this.element = element;
	}

	public Path getPath() {
		return path;
	}

	public T writeToDirectory(Path directory) {
		return (T) element.writeToDirectory(directory.resolve(getPath()));
	}
}
