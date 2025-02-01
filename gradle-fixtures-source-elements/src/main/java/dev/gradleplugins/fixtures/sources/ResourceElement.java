package dev.gradleplugins.fixtures.sources;

public class ResourceElement<T extends SourceElement> {
	public ResourceElement<T> with(String propName, String value) {
		throw new UnsupportedOperationException();
	}

	public SourceFile getSourceFile() {
		throw new UnsupportedOperationException();
	}
}
