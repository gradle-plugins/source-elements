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
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.fixtures.sources.DelegatedElements.sourceSetNameOf;

/**
 * Represent an element containing zero or more source files.
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

	public SourceElement withSourceSetName(String sourceSetName) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return SourceElement.this.getFiles();
			}

			@Override
			public String getSourceSetName() {
				return sourceSetName;
			}
		};
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
	 * {@inheritDoc}
	 */
	public SourceElement writeToDirectory(Path directory) {
		writeToProject(directory);
		return this; // FIXME: Should return an element that represent the files written
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
	public static CompositeSourceElement ofElements(final SourceElement... elements) {
		return new CompositeSourceElement(Arrays.asList(elements));
	}

	public static CompositeSourceElement ofElements(Iterable<SourceElement> elements) {
		return new CompositeSourceElement(StreamSupport.stream(elements.spliterator(), false).collect(Collectors.toList()));
	}

	/**
	 * Returns a source element that contains the given files
	 */
	public static SourceElement ofFiles(final SourceFile... files) {
		return ofFiles(Arrays.asList(files));
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

	public final Set<String> getSourceFileNames() {
		Set<String> result = new LinkedHashSet<>();
		for (SourceFile file : getFiles()) {
			result.add(file.getName());
		}
		return result;
	}

	public static SourceElement fromResource(String resourcePath) {
		return DelegatedElements.sourceOf(resourcePath);
	}

	public static <T extends SourceElement & ResourceElementEx> SourceElement fromResource(Class<T> type) {
		StringBuilder filename = new StringBuilder();
		filename.append(type.getSimpleName());
		Class<?> c = type;
		while ((c = c.getEnclosingClass()) != null) {
			filename.insert(0, c.getSimpleName() + "$");
		}
		return fromResource(type.getPackage().getName().replace('.', '/') + "/" + filename + ".xml");
	}

	public abstract static class FromResource extends SourceElement implements ResourceElementEx {
		private final SourceElement delegate;

		protected FromResource() {
			SourceElement delegate = fromResource(getClass());
			String sourceSetName = sourceSetNameOf(this, FromResource.class).orElse(null);
			if (sourceSetName != null) {
				delegate = delegate.withSourceSetName(sourceSetName);
			}
			this.delegate = delegate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final SourceElement withSourceSetName(String sourceSetName) {
			return super.withSourceSetName(sourceSetName);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final List<SourceFile> getFiles() {
			return delegate.getFiles();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override // allow override
		public String getSourceSetName() {
			return delegate.getSourceSetName();
		}
	}
}
