package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.ElementTestUtils.visited;
import static dev.nokee.elements.nativebase.NativeSourceElement.ofSources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class NativeSourceElementTests {
	static SourceElement nonEmptySources = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return Collections.singletonList(sourceFile("main.cpp", "int main() { return 0; }"));
		}
	};



	@Nested
	class OfSourcesFactory implements NativeSourceElementTester {
		NativeSourceElement subject = ofSources(nonEmptySources);

		@Override
		public NativeSourceElement subject() {
			return subject;
		}

		@Test
		void defaultToEmptyHeaders() {
			assertThat(subject.getHeaders().getFiles(), emptyIterable());
		}

		@Test
		void hasSpecifiedSources() {
			assertThat(subject.getSources().getFiles(), contains(named("main.cpp")));
		}

		@Test
		void visitThisElement() {
			assertThat(visited(subject), contains(subject));
		}
	}

	@Nested
	class WithDefaultHeaders implements NativeSourceElementTester {
		NativeSourceElement subject = new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return ofFiles(sourceFile("main.cpp", "int main() { return 0; }"));
			}
		};

		public NativeSourceElement subject() {
			return subject;
		}

		@Test
		void defaultToEmptyHeaders() {
			assertThat(subject.getHeaders().getFiles(), emptyIterable());
		}

		@Test
		void visitThisElement() {
			assertThat(visited(subject), contains(subject));
		}
	}

	@Nested
	class WithHeaders implements NativeSourceElementTester {
		NativeSourceElement subject = new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return ofFiles(sourceFile("bar.hpp", "int bar();"));
			}

			@Override
			public SourceElement getSources() {
				return ofFiles(sourceFile("main.cpp", "int main() { return 0; }"));
			}
		};

		@Override
		public NativeSourceElement subject() {
			return subject;
		}

		@Test
		void visitThisElement() {
			assertThat(visited(subject), contains(subject));
		}
	}
}
