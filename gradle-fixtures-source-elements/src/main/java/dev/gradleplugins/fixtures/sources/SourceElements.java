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
import java.util.List;

public abstract class SourceElements extends SourceElement {
	public abstract List<SourceElement> getElements();

	@Override
	public List<SourceFile> getFiles() {
		List<SourceFile> files = new ArrayList<SourceFile>();
		for (SourceElement element : getElements()) {
			files.addAll(element.getFiles());
		}
		return files;
	}

	@Override // very important to avoid overriding the elements' sourceSetName
	public void writeToProject(Path projectDir) {
		for (SourceElement element : getElements()) {
			element.writeToProject(projectDir);
		}
	}

	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}
}
