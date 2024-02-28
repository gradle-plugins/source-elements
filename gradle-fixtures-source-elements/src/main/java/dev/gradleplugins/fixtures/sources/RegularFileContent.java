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

import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class RegularFileContent extends SourceFileElement {
	protected final Map<String, String> properties = new LinkedHashMap<>();

	@Override
	public SourceFile getSourceFile() {
		String[] tokens = this.getClass().getAnnotation(SourceFileLocation.class).file().split("/");
		String name = tokens[tokens.length - 1];
		return sourceFile("", name, SourceFileElement.fromResource(this.getClass(), it -> {
			it.putAll(properties);
		}));
	}

	public SourceFileElement withPath(String path) {
		String[] tokens = this.getClass().getAnnotation(SourceFileLocation.class).file().split("/");
		String name = tokens[tokens.length - 1];
		return SourceFileElement.ofFile(Element.sourceFile(path, name, SourceFileElement.fromResource(this.getClass(), it -> {
			it.putAll(properties);
		})));
	}

	public SourceFileElement withPath(String path, String name) {
		return SourceFileElement.ofFile(Element.sourceFile(path, name, SourceFileElement.fromResource(this.getClass(), it -> {
			it.putAll(properties);
		})));
	}
}
