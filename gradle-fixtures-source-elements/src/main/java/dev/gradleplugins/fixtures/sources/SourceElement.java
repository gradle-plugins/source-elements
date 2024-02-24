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

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An element containing zero or more source files.
 */
public abstract class SourceElement extends Element {
	/**
	 * Returns the files associated with this element, possibly none.
	 */
	public abstract List<SourceFile> getFiles();

	/**
	 * Returns the source set name to write the source into, using the Gradle convention for source layout.
	 */
	public String getSourceSetName() {
		return "main";
	}

	/**
	 * Writes the source files of this element to the given project, using the Gradle convention for source layout.
	 */
	public void writeToProject(Path projectDir) {
		final Path srcDir = projectDir.resolve("src/" + getSourceSetName());
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToDirectory(srcDir);
		}
	}

	// Essentially deprecated
	// Cannot be final because of toolbox
	public /*final*/ void writeToProject(File projectDir) {
		writeToProject(projectDir.toPath());
	}

	/**
	 * Writes the source files of this element to the given source directory.
	 */
	public void writeToSourceDir(Path sourceDir) {
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToFile(sourceDir.resolve(sourceFile.getName()));
		}
	}

	// Essentially deprecated
	public final void writeToSourceDir(File sourceDir) {
		writeToSourceDir(sourceDir.toPath());
	}

	/**
	 * Convert this element using the specified transform.
	 * Ex: {@code element.as(lib())} to convert any native elements into a library.
	 *
	 * @param transformer the transform to apply
	 * @return a source element, never null
	 */
	public final SourceElement as(ConvertOperation transformer) {
		return Objects.requireNonNull(transformer.apply(this));
	}

	public interface ConvertOperation extends UnaryOperator<SourceElement> {
	}

	/**
	 * Query a sub-element of this element using the specified transform.
	 * Ex: {@code element.get(headers())} to query all native headers from any native elements.
	 *
	 * @param transformer the transform to apply, must not be null
	 * @return a source element, never null
	 */
	public final SourceElement get(QueryOperation transformer) {
		return Objects.requireNonNull(transformer.apply(this));
	}

	public interface QueryOperation extends UnaryOperator<SourceElement> {
	}

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	public interface Visitor<R> {
		R visit(SourceElement element);

		R visit(SourceFileElement element);

		R visit(NativeSourceElement element);

		R visit(NativeLibraryElement element);

		R visit(NativeSourceFileElement element);

		R visit(SourceElements element);
	}

	public static SourceElement empty() {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.emptyList();
			}
		};
	}

	/**
	 * Returns a source element that contains the union of the given elements.
	 * Each element will be written individually.
	 */
	public static SourceElement ofElements(final SourceElement... elements) {
		return ofElements(Arrays.asList(elements));
	}

	public static SourceElement ofElements(Iterable<SourceElement> elements) {
		return new SourceElements() {
			@Override
			public List<SourceElement> getElements() {
				return StreamSupport.stream(elements.spliterator(), false)
					.collect(Collectors.toList());
			}
		};
	}

	/**
	 * Returns a source element that contains the given files
	 */
	public static SourceElement ofFiles(final SourceFile... files) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Arrays.asList(files);
			}
		};
	}

	/**
	 * Returns a source element that contains the given files
	 */
	public static SourceElement ofFiles(final List<SourceFile> files) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return files;
			}
		};
	}

	public final List<String> getSourceFileNames() {
		return getFiles().stream().map(SourceFile::getName).collect(Collectors.toList());
	}
}
