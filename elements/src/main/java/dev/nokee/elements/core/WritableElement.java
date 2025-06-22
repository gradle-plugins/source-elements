package dev.nokee.elements.core;

import java.nio.file.Path;

/**
 * Represent an element that can be written to disk.
 */
public interface WritableElement {
	/**
	 * Write this element to the specified directory.
	 *
	 * @param directory  the directory to write this element
	 */
	void writeToDirectory(Path directory);
}
