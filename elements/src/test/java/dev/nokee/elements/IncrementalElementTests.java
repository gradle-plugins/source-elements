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
import static org.hamcrest.Matchers.*;

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
						return Collections.singletonList(move(ofFile(sourceFile("app.cpp", "#include \"foo.h\"")), "dir"));
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

	@Nested
	class GenericTransform {
		IncrementalElement subject = new IncrementalElement() {
			@Override
			protected List<Transform> getIncrementalChanges() {
				return Collections.singletonList(replace(
					new SourceElement() {
						@Override
						public List<SourceFile> getFiles() {
							return Arrays.asList(
								sourceFile("a.cpp", "void a() {}"),
								sourceFile("b.cpp", "void b() {}"),
								sourceFile("c.cpp", "void c() {}"),
								sourceFile("d.cpp", "void d() {}"),
								sourceFile("e.cpp", "void e() {}")
							);
						}
					},
					new SourceElement() {
						@Override
						public List<SourceFile> getFiles() {
							return Arrays.asList(
								sourceFile("a.cpp", "void a() {}"), // preseved
								sourceFile("renamed-b.cpp", "void b() {}"), //renamed
								// c.cpp is removed
								sourceFile("dir/d.cpp", "void d() {}"), // moved
								sourceFile("e.cpp", "int e() { return 42; }") // modified
							);
						}
					}
				));
			}
		};

		@Test
		void canWriteAsSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory);

			assertThat(testDirectory, hasRelativeDescendants(
				"a.cpp",
				"b.cpp",
				"c.cpp",
				"d.cpp",
				"e.cpp"
			));
		}

		@Test
		void canApplyIncrementalChangeSourceElement(@TempDir Path testDirectory) {
			subject.writeToDirectory(testDirectory).apply(allChanges());

			assertThat(testDirectory, hasDescendants(
				allOf(withRelativePath("a.cpp"), aFile(withTextContent(equalTo("void a() {}")))),
				allOf(withRelativePath("renamed-b.cpp"), aFile(withTextContent(equalTo("void b() {}")))),
				allOf(withRelativePath("dir/d.cpp"), aFile(withTextContent(equalTo("void d() {}")))),
				allOf(withRelativePath("e.cpp"), aFile(withTextContent(equalTo("int e() { return 42; }"))))
			));
		}

		@Test
		void modifiedFileSystemElementCanBeRewrittenToAnotherLocation(@TempDir Path testDirectory) {
			FileSystemElement element = subject.writeToDirectory(testDirectory.resolve("first")).apply(allChanges());
			assertThat(testDirectory.resolve("first"), hasRelativeDescendants(
				"a.cpp",
				"renamed-b.cpp",
				"dir/d.cpp",
				"e.cpp"
			));

			element.writeToDirectory(testDirectory.resolve("second"));
			assertThat(testDirectory.resolve("second"), hasRelativeDescendants(
				"a.cpp",
				"renamed-b.cpp",
				"dir/d.cpp",
				"e.cpp"
			));
		}
	}
}
