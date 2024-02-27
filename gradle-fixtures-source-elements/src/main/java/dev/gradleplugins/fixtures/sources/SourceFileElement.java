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

import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single source file.
 */
public abstract class SourceFileElement extends SourceElement {
	public abstract SourceFile getSourceFile();

	@Override
	public List<SourceFile> getFiles() {
		return Collections.singletonList(getSourceFile());
	}

	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	public static SourceFileElement ofFile(final SourceFile file) {
		return new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return file;
			}
		};
	}

	private static String fromResource(String path) {
		return new Scanner(Objects.requireNonNull(SourceFileElement.class.getClassLoader().getResourceAsStream("META-INF/templates/" + path), "path '" + path + "' not found"), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
	}

	public static String fromResource(Class<?> contentType) {
		String[] tokens = contentType.getAnnotation(SourceFileLocation.class).file().split("/");
		return fromResource(tokens[0] + "/" + tokens[tokens.length - 1]);
	}

	public static String fromResource(Class<?> contentType, Consumer<? super Map<String, String>> action) {
		Map<String, String> props = new LinkedHashMap<>();
		action.accept(props);

		String content = fromResource(contentType);
		for (SourceFileProperty property : contentType.getAnnotation(SourceFileLocation.class).properties()) {
			Matcher m = Pattern.compile(property.regex(), Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
			StringBuffer builder = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(builder, m.group(0).replace(m.group(1), props.getOrDefault(property.name(), m.group(1))));
			}
			m.appendTail(builder);

			content = builder.toString();
		}

		return content;
	}
}
