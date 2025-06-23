package dev.nokee.elements.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent source elements mapped to file system locations.
 */
public class FileSystemElement extends Element implements WritableElement {
	private final List<Node> nodes;

	public FileSystemElement(Path location, SourceElement sources) {
		this(Collections.singletonList(new Node(Paths.get(""), location, sources)));
	}

	FileSystemElement(List<Node> nodes) {
		this.nodes = nodes;
	}

	static final class Node {
		private final Path base;
		private final Path location;
		private final SourceElement sources;

		public Node(Path base, Path location, SourceElement sources) {
			this.base = base;
			this.location = location;
			this.sources = sources;
		}

		public Node writeToDirectory(Path directory) {
			for (SourceFile file : sources.getFiles()) {
				file.writeToDirectory(directory.resolve(location));
			}
			return new Node(directory, location, sources);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileSystemElement writeToDirectory(Path directory) {
		return new FileSystemElement(nodes.stream().map(it -> it.writeToDirectory(directory)).collect(Collectors.toList()));
	}

	public FileSystemElement apply(IncrementalElement.ChangeVisitor transform) {
		return new FileSystemElement(nodes.stream().map(it -> new Node(it.base, it.location, transform.visit(it.base.resolve(it.location), it.sources))).collect(Collectors.toList()));
	}

	@Override
	public void accept(Visitor visitor) {
		throw new UnsupportedOperationException();
	}
}
