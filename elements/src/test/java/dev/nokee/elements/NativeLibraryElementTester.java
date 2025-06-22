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
		NativeSourceElement newSubject = subject().asImplementation();
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
}
