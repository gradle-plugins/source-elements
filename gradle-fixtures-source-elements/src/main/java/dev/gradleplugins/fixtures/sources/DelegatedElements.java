package dev.gradleplugins.fixtures.sources;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DelegatedElements {
	public static final class Filename {
		private final Class<?> type;

		public Filename(Class<?> type) {
			this.type = type;
		}

		public String dot(String extension) {
			return this + "." + extension;
		}

		public String dotXml() {
			return dot("xml");
		}

		@Override
		public String toString() {
			StringBuilder filename = new StringBuilder();
			filename.append(type.getSimpleName());
			Class<?> c = type;
			while ((c = c.getEnclosingClass()) != null) {
				filename.insert(0, c.getSimpleName() + "$");
			}
			return filename.toString();
		}
	}

	private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newFactory();
	/**
	 * From a source project, return a SourceElement of everything under `src/...`.
	 * You can further transform it. The type passed must be annotated with SourceProject annotation.
	 *
	 * @param type
	 * @return
	 */
	public static SourceElement sourceOf(Class<?> type) {
		String f = "META-INF/elements/" + new Filename(type).dotXml();
		InputStream inStream = Objects.requireNonNull(type.getClassLoader().getResourceAsStream(f));
		try {
			XMLStreamReader delegate = XML_FACTORY.createXMLStreamReader(inStream);
			while (delegate.hasNext()) {
				switch (delegate.next()) {
					case XMLStreamReader.START_ELEMENT:
						switch (delegate.getLocalName()) {
							case "SourceElement":
								return processSourceElement(delegate);
							case "Element":
								return processElements(delegate);
						}
						break;
				}
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		throw new UnsupportedOperationException();
	}

	public static ResourceElement<SourceFileElement> sourceFileOf(Class<?> type) {
		throw new UnsupportedOperationException();
	}

	private static SourceElement processSourceElement(XMLStreamReader delegate) throws XMLStreamException {
		List<SourceFile> sourceFiles = new ArrayList<>();
		while (delegate.hasNext()) {
			switch (delegate.next()) {
				case XMLStreamReader.START_ELEMENT:
					switch (delegate.getLocalName()) {
						case "SourceFile":
							String name = delegate.getAttributeValue(null, "name");
							String path = delegate.getAttributeValue(null, "path");
							String content = delegate.getElementText();
							sourceFiles.add(new SourceFile(path, name, content));
					}
				case XMLStreamReader.END_ELEMENT:
					if (delegate.getLocalName().equals("SourceElement")) {
						SourceElement result = SourceElement.ofFiles(sourceFiles);
						String name = delegate.getAttributeValue(null, "name");
						if (name != null) {
							result = result.withSourceSetName(name);
						}
						return result;
					}
			}
		}

		throw new UnsupportedOperationException();
	}

	private static SourceElement processElements(XMLStreamReader delegate) throws XMLStreamException {
		List<SourceElement> result = new ArrayList<>();
		while (delegate.hasNext()) {
			switch (delegate.next()) {
				case XMLStreamReader.START_ELEMENT:
					switch (delegate.getLocalName()) {
						case "SourceElement":
							result.add(processSourceElement(delegate));
							break;
					}
				case XMLStreamReader.END_ELEMENT:
					if (delegate.getLocalName().equals("Element")) return SourceElement.ofElements(result);
			}
		}
		throw new UnsupportedOperationException();
	}

	static Optional<String> sourceSetNameOf(SourceElement obj, Class<?> notOverride) {
		try {
			if (obj.getClass().getMethod("getSourceSetName").getDeclaringClass().equals(notOverride)) {
				return Optional.empty(); // not overridden
			} else {
				return Optional.of(obj.getSourceSetName());
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
