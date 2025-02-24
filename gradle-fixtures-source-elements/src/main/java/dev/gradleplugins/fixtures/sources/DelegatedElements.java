package dev.gradleplugins.fixtures.sources;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

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
	 */
	public static SourceElement sourceOf(String resourcePath) {
		InputStream inStream = Objects.requireNonNull(DelegatedElements.class.getClassLoader().getResourceAsStream(resourcePath));
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
		} catch (XMLStreamException | RuntimeException e) {
			throw new RuntimeException(String.format("error loading '%s'", resourcePath), e);
		}
		throw new UnsupportedOperationException();
	}

	public static ResourceElement<SourceFileElement> sourceFileOf(Class<?> type) {
		StringBuilder filename = new StringBuilder();
		filename.append(type.getSimpleName());
		Class<?> c = type;
		while ((c = c.getEnclosingClass()) != null) {
			filename.insert(0, c.getSimpleName() + "$");
		}
		return sourceFileOf(type.getPackage().getName().replace('.', '/') + "/" + filename + ".xml");
	}

	public static ResourceElement<SourceFileElement> sourceFileOf(String resourcePath) {
		InputStream inStream = Objects.requireNonNull(DelegatedElements.class.getClassLoader().getResourceAsStream(resourcePath));
		try {
			XMLStreamReader delegate = XML_FACTORY.createXMLStreamReader(inStream);
			while (delegate.hasNext()) {
				switch (delegate.next()) {
					case XMLStreamReader.START_ELEMENT:
						switch (delegate.getLocalName()) {
							case "SourceElement":
								return zzprocessSourceElement(delegate);
							case "Element":
								throw new UnsupportedOperationException();
						}
						break;
				}
			}
		} catch (XMLStreamException | RuntimeException e) {
			throw new RuntimeException(String.format("error loading '%s'", "none"), e);
		}
		throw new UnsupportedOperationException();
	}

	private static ResourceElement<SourceFileElement> zzprocessSourceElement(XMLStreamReader reader) throws XMLStreamException {
		List<SourceFile> sourceFiles = new ArrayList<>();
		List<Property> properties = new ArrayList<>();
		String sourceSetName = reader.getAttributeValue(null, "name");
		while (reader.hasNext()) {
			switch (reader.next()) {
				case XMLStreamReader.START_ELEMENT:
					switch (reader.getLocalName()) {
						case "SourceFile":
							if (true) {
								String name = reader.getAttributeValue(null, "name");
								String path = reader.getAttributeValue(null, "path");
								String content = reader.getElementText();
								sourceFiles.add(new SourceFile(path, name, content));
							}
							break;
						case "Property":
							if (true) {
								String name = reader.getAttributeValue(null, "name");
								String regex = reader.getAttributeValue(null, "regex");
								properties.add(new Property(name, Pattern.compile(regex)));
							}
							break;
					}
				case XMLStreamReader.END_ELEMENT:
					if (reader.getLocalName().equals("SourceElement")) {
						SourceElement result = SourceElement.ofFiles(sourceFiles);
						if (sourceSetName != null) {
							result = result.withSourceSetName(sourceSetName);
						}
						return ResourceElement.from(result, properties);
					}
			}
		}

		throw new UnsupportedOperationException();
	}

	public static final class Property {
		private final String name;
		private final Pattern pattern;

		private Property(String name, Pattern pattern) {
			this.name = name;
			this.pattern = pattern;
		}

		public String getName() {
			return name;
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

	private static SourceElement processSourceElement(XMLStreamReader delegate) throws XMLStreamException {
		List<SourceFile> sourceFiles = new ArrayList<>();
		String sourceSetName = delegate.getAttributeValue(null, "name");
		while (delegate.hasNext()) {
			switch (delegate.next()) {
				case XMLStreamReader.START_ELEMENT:
					switch (delegate.getLocalName()) {
						case "SourceFile":
							String name = delegate.getAttributeValue(null, "name");
							String path = delegate.getAttributeValue(null, "path");
							String content = delegate.getElementText();
							sourceFiles.add(new SourceFile(path, name, content));
							break;
					}
				case XMLStreamReader.END_ELEMENT:
					if (delegate.getLocalName().equals("SourceElement")) {
						SourceElement result = SourceElement.ofFiles(sourceFiles);
						if (sourceSetName != null) {
							result = result.withSourceSetName(sourceSetName);
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
