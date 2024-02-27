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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public abstract class DirectoryContent {
	protected final Map<String, String> properties = new LinkedHashMap<>();

	public SourceElement withPath(String path) {
		String[] tokens = this.getClass().getAnnotation(SourceFileLocation.class).file().split("/");
		String content = fromResource(tokens[0] + "/" + tokens[tokens.length - 1] + ".sample");

		int pathIndex = path.lastIndexOf('/');
		String p = path.substring(0, pathIndex);
		String f = path.substring(pathIndex + 1);


		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Arrays.stream(content.split("\n")).map(it -> {
					String name = it.substring(it.indexOf(f));
					return sourceFile(p, name, readAll(it));
				}).collect(Collectors.toList());
			}
		};
	}

	private static String readAll(String path) {
		try (InputStream inStream = DirectoryContent.class.getResourceAsStream("/META-INF/templates/" + path)) {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = inStream.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return result.toString(StandardCharsets.UTF_8.name());
		} catch (
			IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
