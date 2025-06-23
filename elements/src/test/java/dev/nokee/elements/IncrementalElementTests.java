package dev.nokee.elements;

import dev.nokee.elements.core.*;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.*;
import static dev.nokee.elements.core.IncrementalElement.allChanges;
import static dev.nokee.elements.core.SourceFileElement.ofFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class IncrementalElementTests {
	@Nested
	class BaseIncrementalTest {
		IncrementalElement subject = new IncrementalElement() {
			@Override
			protected List<Transform> getIncrementalChanges() {
				return Collections.singletonList(modify(ofFile(sourceFile("foo.cpp", "int foo() { return 42 }")), ofFile(sourceFile("foo.cpp", "int foo() { return 42; }"))));
			}
		};

		@Test
		void canWriteAsSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);

			assertThat(testDirectory.resolve("foo.cpp"), aFile(withTextContent(not(containsString(";")))));
		}

		@Test
		void canApplyIncrementalChangeSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory).apply(allChanges());

			assertThat(testDirectory.resolve("foo.cpp"), aFile(withTextContent(containsString(";"))));
		}

		@Test
		void modifiedFileSystemElementCanBeRewrittenToAnotherLocation(@TempDir Path testDirectory) {
			FileSystemElement element = subject.writeToDirectory(testDirectory.resolve("first")).apply(allChanges());
			assertThat(testDirectory.resolve("first").resolve("foo.cpp"), aFile(withTextContent(containsString(";"))));

			element.writeToDirectory(testDirectory.resolve("second"));
			assertThat(testDirectory.resolve("second").resolve("foo.cpp"), aFile(withTextContent(containsString(";"))));
		}
	}

	@Nested
	class AddIncrementalTest {
		IncrementalElement subject = new IncrementalElement() {
			@Override
			protected List<Transform> getIncrementalChanges() {
				return Collections.singletonList(add(ofFile(sourceFile("foo.cpp", "int foo() { return 42; }"))));
			}
		};

		@Test
		void canWriteAsSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);

			assertThat(testDirectory.resolve("foo.cpp"), not(anExistingFile()));
		}

		@Test
		void canApplyIncrementalChangeSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory).apply(allChanges());

			assertThat(testDirectory.resolve("foo.cpp"), anExistingFile());
		}

		@Test
		void modifiedFileSystemElementCanBeRewrittenToAnotherLocation(@TempDir Path testDirectory) {
			FileSystemElement element = subject.writeToDirectory(testDirectory.resolve("first")).apply(allChanges());
			assertThat(testDirectory.resolve("first").resolve("foo.cpp"), anExistingFile());

			element.writeToDirectory(testDirectory.resolve("second"));
			assertThat(testDirectory.resolve("second").resolve("foo.cpp"), anExistingFile());
		}
	}

	@Nested
	class DeleteIncrementalTest {
		IncrementalElement subject = new IncrementalElement() {
			@Override
			protected List<Transform> getIncrementalChanges() {
				return Collections.singletonList(delete(ofFile(sourceFile("foo.cpp", "int foo() { return 42; }"))));
			}
		};

		@Test
		void canWriteAsSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);

			assertThat(testDirectory.resolve("foo.cpp"), anExistingFile());
		}

		@Test
		void canApplyIncrementalChangeSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory).apply(allChanges());

			assertThat(testDirectory.resolve("foo.cpp"), not(anExistingFile()));
		}

		@Test
		void modifiedFileSystemElementCanBeRewrittenToAnotherLocation(@TempDir Path testDirectory) {
			FileSystemElement element = subject.writeToDirectory(testDirectory.resolve("first")).apply(allChanges());
			assertThat(testDirectory.resolve("first").resolve("foo.cpp"), not(anExistingFile()));

			element.writeToDirectory(testDirectory.resolve("second"));
			assertThat(testDirectory.resolve("second").resolve("foo.cpp"), not(anExistingFile()));
		}
	}

	@Nested
	class RenameIncrementalTest {
		IncrementalElement subject = new IncrementalElement() {
			@Override
			protected List<Transform> getIncrementalChanges() {
				return Collections.singletonList(rename(ofFile(sourceFile("foo.cpp", "int foo() { return 42; }"))));
			}
		};

		@Test
		void canWriteAsSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);

			assertThat(testDirectory, hasRelativeDescendants("foo.cpp"));
		}

		@Test
		void canApplyIncrementalChangeSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory).apply(allChanges());

			assertThat(testDirectory, hasRelativeDescendants("renamed-foo.cpp"));
		}

		@Test
		void modifiedFileSystemElementCanBeRewrittenToAnotherLocation(@TempDir Path testDirectory) {
			FileSystemElement element = subject.writeToDirectory(testDirectory.resolve("first")).apply(allChanges());
			assertThat(testDirectory.resolve("first"), hasRelativeDescendants("renamed-foo.cpp"));

			element.writeToDirectory(testDirectory.resolve("second"));
			assertThat(testDirectory.resolve("second"), hasRelativeDescendants("renamed-foo.cpp"));
		}
	}

	@Nested
	class RelocateCppSourceTest {
		ProjectElement subject = ProjectElement.ofMain(new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return SourceElement.ofElements(ofFiles(
					sourceFile("main.cpp", "#include \"foo.h\""),
					sourceFile("foo.h", "...")
				), new IncrementalElement() {
					@Override
					protected List<Transform> getIncrementalChanges() {
						return Arrays.asList(move(ofFile(sourceFile("app.cpp", "#include \"foo.h\"")), "dir"));
					}
				});
			}
		});

		@Test
		void applyTransform(@TempDir Path testDirectory) {
			FileSystemElement element = new GradleLayoutElement().applyTo(subject).writeToDirectory(testDirectory);

			assertThat(testDirectory, hasRelativeDescendants(
				"src/main/cpp/main.cpp",
				"src/main/cpp/foo.h",
				"src/main/cpp/app.cpp"
			));

			element.apply(allChanges());

			assertThat(testDirectory, hasRelativeDescendants(
				"src/main/cpp/main.cpp",
				"src/main/cpp/foo.h",
				"src/main/cpp/dir/app.cpp"
			));
		}
	}
}
