package dev.nokee.elements;

import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Test;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static dev.nokee.elements.core.Element.ofFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

interface NativeSourceElementTester {
	NativeSourceElement subject();

	@Test
	default void canConvertToLibraryElement() {
		NativeLibraryElement library = subject().withPublicHeaders(ofFiles(SourceFile.of("foo.hpp", "int foo();")));
		assertThat(library.getSources(), is(subject().getSources()));
		assertThat(library.getPrivateHeaders(), is(subject().getHeaders()));
		assertThat(library.getPublicHeaders().getFiles(), contains(named("foo.hpp")));
	}
}
