package dev.nokee.elements;

import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.core.SwiftSourceElement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SwiftSourceElementTests {
	SwiftSourceElement subject = new SwiftSourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return Arrays.asList(
				SourceFile.of("foo.swift", "public func foo() -> Int { return 42 }"),
				SourceFile.of("bar.swift", "public func bar() -> Int { return 42 }")
			);
		}
	};

	@Test
	void canImportModule() {
		SwiftSourceElement newSubject = subject.withImport("Math");
		assertThat(newSubject.getFiles(), contains(named("foo.swift"), named("bar.swift")));
		assertThat(newSubject.getFiles().stream().map(SourceFile::getContent).collect(Collectors.toList()), everyItem(startsWith("import Math\n")));
	}
}
