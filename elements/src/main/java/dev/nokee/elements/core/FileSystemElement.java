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
	private final Path base;
	private final List<Node> nodes;

	public FileSystemElement(Path location, SourceElement sources) {
		this(location, Collections.singletonList(new Node(Paths.get(""), sources)));
	}

	FileSystemElement(Path base, List<Node> nodes) {
		this.base = base;
		this.nodes = nodes;
	}

	static final class Node {
		private final Path location;
		private final SourceElement sources;

		public Node(Path location, SourceElement sources) {
			this.location = location;
			this.sources = sources;
		}

		public void writeToDirectory(Path directory) {
			sources.writeToDirectory(directory.resolve(location.toString()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileSystemElement writeToDirectory(Path directory) {
		nodes.forEach(it -> it.writeToDirectory(directory));
		return new FileSystemElement(directory, nodes);
	}

	public FileSystemElement apply(IncrementalElement.ChangeVisitor transform) {
		return new FileSystemElement(base, nodes.stream().map(it -> new Node(it.location, transform.visit(base.resolve(it.location), it.sources))).collect(Collectors.toList()));
	}

	@Override
	public void accept(Visitor visitor) {
		throw new UnsupportedOperationException();
	}
}
