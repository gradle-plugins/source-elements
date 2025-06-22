package dev.nokee.elements;

import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Test;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.core.Element.ofFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

interface NativeSourceElementTester {
	NativeSourceElement subject();

	@Test
	default void canConvertToLibraryElement() {
		NativeLibraryElement newSubject = subject().withPublicHeaders(ofFiles(SourceFile.of("foo.hpp", "int foo();")));
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
}
