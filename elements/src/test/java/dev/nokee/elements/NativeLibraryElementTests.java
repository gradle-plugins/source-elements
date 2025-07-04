package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeElement;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.ElementTestUtils.visited;
import static dev.nokee.elements.core.SourceFile.of;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NativeLibraryElementTests {
	static SourceElement nonEmptySources = new SourceElement() {
		@Override
		public List<SourceFile> getFiles() {
			return Collections.singletonList(sourceFile("foo.cpp", "int foo() { return 42; }"));
		}
	};

	@Nested
	class WithDefaultPrivateHeaders implements Tester {
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

	private interface Tester {
		NativeLibraryElement subject();

		@Test
		default void canConvertToImplementationElement() {
			NativeElement newSubject = subject().asImplementation();
			assertThat(newSubject.getSources(), is(subject().getSources()));
			assertThat(newSubject.getHeaders(), is(subject().getHeaders()));
		}

		@Test
		default void canRemovePublicHeaders() {
			NativeLibraryElement newSubject = subject().withoutPublicHeaders();
			assertThat(newSubject.getPublicHeaders().getFiles(), emptyIterable());
			assertThat(newSubject.getPrivateHeaders(), is(subject().getPrivateHeaders()));
			assertThat(newSubject.getSources(), is(subject().getSources()));
		}

		@Test
		default void canRemovePrivateHeaders() {
			NativeLibraryElement newSubject = subject().withoutPrivateHeaders();
			assertThat(newSubject.getPublicHeaders(), is(subject().getPublicHeaders()));
			assertThat(newSubject.getPrivateHeaders().getFiles(), emptyIterable());
			assertThat(newSubject.getSources(), is(subject().getSources()));
		}

		@Test
		default void canRemoveAllHeaders() {
			NativeLibraryElement newSubject = subject().withoutHeaders();
			assertThat(newSubject.getPublicHeaders().getFiles(), emptyIterable());
			assertThat(newSubject.getPrivateHeaders().getFiles(), emptyIterable());
			assertThat(newSubject.getSources(), is(subject().getSources()));
		}

		@Test
		default void canRemoveSources() {
			NativeLibraryElement newSubject = subject().withoutSources();
			assertThat(newSubject.getPublicHeaders(), is(subject().getPublicHeaders()));
			assertThat(newSubject.getPrivateHeaders(), is(subject().getPrivateHeaders()));
			assertThat(newSubject.getSources().getFiles(), emptyIterable());
		}

		@Test
		default void canReplaceSources() {
			NativeLibraryElement newSubject = subject().withSources(SourceElement.ofFiles(singletonList(of("my-other-source.cpp", "int foobar() { return 52; }"))));
			assertThat(newSubject.getPublicHeaders(), is(subject().getPublicHeaders()));
			assertThat(newSubject.getPrivateHeaders(), is(subject().getPrivateHeaders()));
			assertThat(newSubject.getSources().getFiles(), contains(named("my-other-source.cpp")));
		}
	}
}
