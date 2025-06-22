package dev.nokee.elements;

import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;

public interface NativeLibraryElementTester {
	NativeLibraryElement subject();

	@Test
	default void canConvertToImplementationElement() {
		NativeSourceElement impl = subject().asImplementation();
		assertThat(impl.getSources(), is(subject().getSources()));
		assertThat(impl.getHeaders(), is(subject().getHeaders()));
	}

	@Test
	default void canRemovePublicHeaders() {
		NativeLibraryElement library = subject().withoutPublicHeaders();
		assertThat(library.getPublicHeaders().getFiles(), emptyIterable());
		assertThat(library.getPrivateHeaders(), is(subject().getPrivateHeaders()));
		assertThat(library.getSources(), is(subject().getSources()));
	}

	@Test
	default void canRemovePrivateHeaders() {
		NativeLibraryElement library = subject().withoutPrivateHeaders();
		assertThat(library.getPublicHeaders(), is(subject().getPublicHeaders()));
		assertThat(library.getPrivateHeaders().getFiles(), emptyIterable());
		assertThat(library.getSources(), is(subject().getSources()));
	}

	@Test
	default void canRemoveAllHeaders() {
		NativeLibraryElement library = subject().withoutHeaders();
		assertThat(library.getPublicHeaders().getFiles(), emptyIterable());
		assertThat(library.getPrivateHeaders().getFiles(), emptyIterable());
		assertThat(library.getSources(), is(subject().getSources()));
	}
}
