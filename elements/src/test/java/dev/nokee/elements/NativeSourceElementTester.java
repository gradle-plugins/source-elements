package dev.nokee.elements;

import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Test;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.core.SourceElement.ofFiles;
import static dev.nokee.elements.core.SourceFile.of;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

interface NativeSourceElementTester {
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
