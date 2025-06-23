package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.nativebase.NativeElement;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static dev.nokee.elements.ElementTestUtils.visited;
import static dev.nokee.elements.core.SourceElement.ofFiles;
import static dev.nokee.elements.nativebase.NativeSourceElement.ofElements;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class CompositeNativeSourceElementTests {
	@Test
	void visitDeeplyNestedNativeElements() {
		NativeSourceElement first = new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return ofFiles(sourceFile("foo.cpp"));
			}
		};
		NativeSourceElement second = NativeSourceElement.empty();
		NativeLibraryElement third = NativeLibraryElement.empty();
		NativeSourceElement fourth = NativeSourceElement.ofSources(ofFiles(Collections.singletonList(SourceFile.of("bar.cpp", "..."))));
		NativeSourceElement fifth = new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return ofFiles(sourceFile("far.cpp", "..."));
			}
		};
		NativeLibraryElement sixth = new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return ofFiles(sourceFile("tar.hpp", "..."));
			}

			@Override
			public SourceElement getSources() {
				return ofFiles(sourceFile("tar.cpp", "..."));
			}
		};
		NativeElement seventh = sixth.asImplementation();


		assertThat(visited(ofElements(first, ofElements(second, ofElements(third), ofElements(fourth, fifth)), ofElements(sixth, seventh))), contains(first, second, third, fourth, fifth, sixth, seventh));
	}
}
