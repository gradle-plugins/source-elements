package dev.nokee.elements;

import dev.nokee.elements.core.Element;
import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.ElementTestUtils.visited;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class NativeLibraryElementTests {
	static SourceElement nonEmptySources = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return Collections.singletonList(sourceFile("foo.cpp", "int foo() { return 42; }"));
		}
	};

	@Nested
	class WithDefaultPrivateHeaders implements NativeLibraryElementTester {
		NativeLibraryElement subject = new NativeLibraryElement() {
			@Override
			public SourceElement getSources() {
				return nonEmptySources;
			}

			@Override
			public SourceElement getPublicHeaders() {
				return ofFiles(SourceFile.of("foo.hpp", "int foo();"));
			}
		};

		@Override
		public NativeLibraryElement subject() {
			return subject;
		}

		@Test
		void defaultToEmptyPrivateHeaders() {
			assertThat(subject.getPrivateHeaders().getFiles(), emptyIterable());
		}

		@Test
		void hasPublicHeadersInLibraryHeaders() {
			assertThat(subject.getHeaders().getFiles(), contains(named("foo.hpp")));
		}

		@Test
		void visitThisElement() {
			assertThat(visited(subject), contains(subject));
		}
	}
}
