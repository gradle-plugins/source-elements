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
	public List<SourceFile> getFiles() {
		return Collections.singletonList(getSourceFile());
	}
}
