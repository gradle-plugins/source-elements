package dev.nokee.elements.core;

import java.util.Collections;
import java.util.List;

/**
 * Represent a single source file.
 */
public abstract class SourceFileElement extends SourceElement {
	/**
	 * {@return the source file of this element}
	 */
	public abstract SourceFile getSourceFile();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<SourceFile> getFiles() {
		return Collections.singletonList(getSourceFile());
	}

	/**
	 * {@return a new source element for the specified source file}
	 */
	public static SourceFileElement ofFile(SourceFile file) {
		return new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return file;
			}
		};
	}
}
