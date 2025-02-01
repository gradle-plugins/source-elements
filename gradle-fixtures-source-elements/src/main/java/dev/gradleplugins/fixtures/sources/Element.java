/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.fixtures.sources;

import java.nio.file.Path;

/**
 * Represents an element that can be written to a directory.
 */
public abstract class Element {
	/**
	 * Write this element to the specified directory.
	 * @param directory  the directory to write this element
	 * @return the element as written
	 */
	public abstract Element writeToDirectory(Path directory);

	/**
	 * Creates a source file represented by the specified path, name and content.
	 *
	 * @param path  the path to the file (relative to the source set directory)
	 * @param name  the name of the file
	 * @param content  the file content
	 * @return a new source file
	 */
	protected static SourceFile sourceFile(String path, String name, String content) {
		return new SourceFile(path, name, content);
	}

	/**
	 * Creates a source file represented by the specified source path and content.
	 *
	 * @param sourcePath  the file path (relative to the source set directory)
	 * @param content  the file content
	 * @return a new source file
	 */
	protected static SourceFile sourceFile(String sourcePath, String content) {
		return SourceFile.of(sourcePath, content);
	}
}
