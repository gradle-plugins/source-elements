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
import static dev.nokee.elements.core.Element.ofFiles;
import static dev.nokee.elements.nativebase.NativeSourceElement.ofSources;
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
	}
}
