package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class SourceElementTests {
	@Nested
	class CustomElement {
		SourceElement subject = new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.singletonList(sourceFile("foo.cpp", "int foo() { return 42; }"));
			}
		};
	}

	@Nested
	class OfFilesFactory {
		SourceElement subject = SourceElement.ofFiles(SourceFile.of("foo.cpp", "int foo() { return 42; }"));

		@Test
		void hasSpecifiedSourceFiles() {
			assertThat(subject.getFiles(), contains(named("foo.cpp")));
		}
	}

	@Nested
	class EmptyElement {
		SourceElement subject = SourceElement.empty();

		@Test
		void hasNoSourceFiles() {
			assertThat(subject.getFiles(), emptyIterable());
		}
	}
}
