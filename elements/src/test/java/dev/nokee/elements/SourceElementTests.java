package dev.nokee.elements;

import dev.nokee.commons.hamcrest.gradle.FileSystemMatchers;
import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.core.SourceFileElement;
import org.hamcrest.io.FileMatchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.anEmptyDirectory;
import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.hasRelativeDescendants;
import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class SourceElementTests {
	static SourceFile sourceFile = SourceFile.of("foo.cpp", "int foo() { return 42; }");

	@Nested
	class CustomElement {
		SourceElement subject = new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.singletonList(sourceFile);
			}
		};

		@Test
		void canWriteToDirectory(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);
			assertThat(testDirectory, hasRelativeDescendants("foo.cpp"));
		}
	}

	@Nested
	class OfFilesFactory {
		SourceElement subject = SourceElement.ofFiles(Collections.singletonList(sourceFile));

		@Test
		void hasSpecifiedSourceFiles() {
			assertThat(subject.getFiles(), contains(named("foo.cpp")));
		}

		@Test
		void canWriteToDirectory(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);
			assertThat(testDirectory, hasRelativeDescendants("foo.cpp"));
		}
	}

	@Nested
	class EmptyElement {
		SourceElement subject = SourceElement.empty();

		@Test
		void hasNoSourceFiles() {
			assertThat(subject.getFiles(), emptyIterable());
		}

		@Test
		void canWriteToDirectory(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);
			assertThat(testDirectory, anEmptyDirectory());
		}
	}

	@Nested
	class SingleFileElement {
		SourceElement subject = new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return sourceFile;
			}
		};

		@Test
		void hasSpecifiedSourceFile() {
			assertThat(subject.getFiles(), contains(named("foo.cpp")));
		}

		@Test
		void canWriteToDirectory(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);
			assertThat(testDirectory, hasRelativeDescendants("foo.cpp"));
		}
	}

	@Nested
	class OfFileFactory {
		SourceElement subject = SourceFileElement.ofFile(sourceFile);

		@Test
		void hasSpecifiedSourceFile() {
			assertThat(subject.getFiles(), contains(named("foo.cpp")));
		}

		@Test
		void canWriteToDirectory(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);
			assertThat(testDirectory, hasRelativeDescendants("foo.cpp"));
		}
	}
}
