package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.ElementTestUtils.visited;
import static dev.nokee.elements.core.SourceElement.ofFiles;
import static dev.nokee.elements.core.SourceFile.of;
import static dev.nokee.elements.nativebase.NativeSourceElement.ofSources;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NativeSourceElementTests {
	static SourceElement nonEmptySources = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return Collections.singletonList(sourceFile("main.cpp", "int main() { return 0; }"));
		}
	};



	@Nested
	class OfSourcesFactory implements Tester {
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
	class WithDefaultHeaders implements Tester {
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
	class WithHeaders implements Tester {
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

	private interface Tester {
		NativeSourceElement subject();

		@Test
		default void canConvertToLibraryElement() {
			NativeLibraryElement newSubject = subject().withPublicHeaders(ofFiles(singletonList(of("foo.hpp", "int foo();"))));
			assertThat(newSubject.getSources(), is(subject().getSources()));
			assertThat(newSubject.getPrivateHeaders(), is(subject().getHeaders()));
			assertThat(newSubject.getPublicHeaders().getFiles(), contains(named("foo.hpp")));
		}

		@Test
		default void canRemoveSources() {
			NativeSourceElement newSubject = subject().withoutSources();
			assertThat(newSubject.getHeaders(), is(subject().getHeaders()));
			assertThat(newSubject.getSources().getFiles(), emptyIterable());
		}

		@Test
		default void canReplaceSources() {
			NativeSourceElement newSubject = subject().withSources(ofFiles(singletonList(of("my-other-source.cpp", "int foobar() { return 52; }"))));
			assertThat(newSubject.getHeaders(), is(subject().getHeaders()));
			assertThat(newSubject.getSources().getFiles(), contains(named("my-other-source.cpp")));
		}
	}
}
