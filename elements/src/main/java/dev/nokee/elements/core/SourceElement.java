package dev.nokee.elements.core;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represent an element containing zero or more source files.
 */
public abstract class SourceElement extends Element implements WritableElement {
	/**
	 * {@return the source files associated with this element, possibly none.}
	 */
	public abstract List<SourceFile> getFiles();

	/**
	 * {@inheritDoc}
	 */
	public void writeToDirectory(Path directory) {
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToDirectory(directory);
		}
	}

	public static SourceElement empty() {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.emptyList();
			}
		};
	}

	public static SourceElement ofElements(SourceElement... elements) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Stream.of(elements).flatMap(it -> it.getFiles().stream()).collect(Collectors.toList());
			}
		};
	}

	public static SourceElement ofElements(List<SourceElement> elements) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return elements.stream().flatMap(it -> it.getFiles().stream()).collect(Collectors.toList());
			}

			@Override
			public void accept(Visitor visitor) {
				for (SourceElement element : elements) {
					visitor.visit(element);
				}
			}
		};
	}

	/**
	 * Returns a source element that contains the given files.
	 *
	 * @param sourceFiles  the source files of the element
	 * @return a new source element containing the specified source files.
	 */
	public static SourceElement ofFiles(List<SourceFile> sourceFiles) {
		final List<SourceFile> files = Collections.unmodifiableList(new ArrayList<>(sourceFiles));
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return files;
			}
		};
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFiles());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SourceElement)) return false;

		return getFiles().equals(((SourceElement) obj).getFiles());
	}

	@Deprecated
	public static SourceElement load(String resourcePath) {
		InputStream inStream = Objects.requireNonNull(SourceElement.class.getClassLoader().getResourceAsStream("META-INF/elements/" + resourcePath + ".xml"));
		try {
			XMLStreamReader delegate = XML_FACTORY.createXMLStreamReader(inStream);
			while (delegate.hasNext()) {
				switch (delegate.next()) {
					case XMLStreamReader.START_ELEMENT:
						switch (delegate.getLocalName()) {
							case "SourceElement":
								return processSourceElement(delegate);
						}
						break;
				}
			}
		} catch (XMLStreamException | RuntimeException e) {
			throw new RuntimeException(String.format("error loading '%s'", resourcePath), e);
		}
		throw new UnsupportedOperationException();
	}


	private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newFactory();

	private static SourceElement processSourceElement(XMLStreamReader delegate) throws XMLStreamException {
		List<SourceFile> sourceFiles = new ArrayList<>();
		while (delegate.hasNext()) {
			switch (delegate.next()) {
				case XMLStreamReader.START_ELEMENT:
					switch (delegate.getLocalName()) {
						case "SourceFile":
							// TODO: MERGE NAME AND PATH TOGETHER
//							String name = delegate.getAttributeValue(null, "name");
							String path = delegate.getAttributeValue(null, "path");
							String content = delegate.getElementText();
							sourceFiles.add(new SourceFile("", path, content));
							break;
					}
				case XMLStreamReader.END_ELEMENT:
					if (delegate.getLocalName().equals("SourceElement")) {
						SourceElement result = SourceElement.ofFiles(sourceFiles);
						return result;
					}
			}
		}

		throw new UnsupportedOperationException();
	}
}
