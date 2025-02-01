/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.fixtures.sources;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class CompositeSourceElement extends SourceElement implements Iterable<SourceElement> {
	private final List<SourceElement> elements;
	private final String sourceSetName;

	CompositeSourceElement(List<SourceElement> elements) {
		this.elements = new ArrayList<>(elements);

		String sourceSetName = null;
		for (SourceElement element : elements) {
			if (sourceSetName == null) sourceSetName = element.getSourceSetName();
			else if (!sourceSetName.equals(element.getSourceSetName())) {
				throw new UnsupportedOperationException("elements must have the same source set name");
			}
		}

		this.sourceSetName = sourceSetName;
	}

	public List<SourceElement> getElements() {
		return Collections.unmodifiableList(elements);
	}

	@Override
	public List<SourceFile> getFiles() {
		List<SourceFile> files = new ArrayList<SourceFile>();
		for (SourceElement element : elements) {
			files.addAll(element.getFiles());
		}
		return files;
	}

	@Override
	// very important to avoid overriding the elements' sourceSetName
	public void writeToProject(Path projectDir) {
		for (SourceElement element : elements) {
			element.writeToProject(projectDir);
		}
	}

	@Override
	public String getSourceSetName() {
		return sourceSetName;
	}

	@Override
	public CompositeSourceElement withSourceSetName(String sourceSetName) {
		return new CompositeSourceElement(elements.stream().map(it -> it.withSourceSetName(sourceSetName)).collect(Collectors.toList()));
	}

	@Override
	public Iterator<SourceElement> iterator() {
		return elements.iterator();
	}
}
