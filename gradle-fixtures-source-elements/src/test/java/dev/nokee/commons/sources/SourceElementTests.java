package dev.nokee.commons.sources;

import dev.gradleplugins.fixtures.sources.CompositeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static dev.gradleplugins.fixtures.sources.SourceElement.ofElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;
import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.*;
import static dev.nokee.commons.hamcrest.gradle.ThrowableMatchers.message;
import static dev.nokee.commons.hamcrest.gradle.ThrowableMatchers.throwsException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SourceElementTests {
	@Test
	void canCreateEmptySourceElement(@TempDir Path testDirectory) {
		SourceElement subject = SourceElement.empty();
		assertThat(subject.getFiles(), emptyIterable());
		assertThat(subject.getSourceFileNames(), emptyIterable());
		assertThat("default source set is always 'main'", subject.getSourceSetName(), equalTo("main"));

		subject.writeToDirectory(testDirectory);
		assertThat("writes nothing to disk", testDirectory, anEmptyDirectory());

		assertThat("can override source set name", subject.withSourceSetName("other").getSourceSetName(), equalTo("other"));
	}

	@Test
	void canCreateSourceElementOfOnlySourceFiles(@TempDir Path testDirectory) {
		SourceElement subject = SourceElement.ofFiles(
			SourceFile.of("c/foo.c", "int foo() { return 42; }"),
			SourceFile.of("headers/foo.h", "int foo();"));
		assertThat(subject.getFiles(), iterableWithSize(2));
		assertThat(subject.getSourceFileNames(), containsInAnyOrder("foo.c", "foo.h"));
		assertThat("default source set is always 'main'", subject.getSourceSetName(), equalTo("main"));

		subject.writeToDirectory(testDirectory);
		assertThat(testDirectory, hasRelativeDescendants("src/main/c/foo.c", "src/main/headers/foo.h"));

		assertThat("can override source set name", subject.withSourceSetName("other").getSourceSetName(), equalTo("other"));
	}

	@Test
	void canOverrideSourceSetNameOnCustomSourceElement() {
		SourceElement subject = new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Arrays.asList(
					sourceFile("c/bar.c", "int bar() { return 42; }"),
					sourceFile("headers/bar.h", "int bar();")
				);
			}

			@Override
			public String getSourceSetName() {
				return "test";
			}
		};

		assertThat(subject.getSourceSetName(), equalTo("test"));
	}

	@Test
	void canOverrideSourceSetNameOnCustomSourceFileElement() {
		SourceElement subject = new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return sourceFile("java/main.java", "class Main { public static void main() { return 42; } }");
			}

			@Override
			public String getSourceSetName() {
				return "test";
			}
		};

		assertThat(subject.getSourceSetName(), equalTo("test"));
	}

	@Nested
	class CustomTests implements SourceElementTester {
		@Override
		public SourceElement subject() {
			return new SourceElement() {
				@Override
				public List<SourceFile> getFiles() {
					return Arrays.asList(
						sourceFile("c/bar.c", "int bar() { return 42; }"),
						sourceFile("headers/bar.h", "int bar();")
					);
				}
			};
		}
	}

	@Nested
	class CustomSingleFileTests implements SourceElementTester {
		@Override
		public SourceElement subject() {
			return new SourceFileElement() {
				@Override
				public SourceFile getSourceFile() {
					return sourceFile("java/main.java", "class Main { public static void main() { return 42; } }");
				}
			};
		}
	}

	@Nested
	class SingleFileTests implements SourceElementTester {
		@Override
		public SourceElement subject() {
			return ofFile(SourceFile.of("java/main.java", "class Main { public static void main() { return 42; } }"));
		}
	}

	@Nested
	class CompositeTests implements SourceElementTester {
		@Override
		public CompositeSourceElement subject() {
			return ofElements(
				ofFile(SourceFile.of("cpp/main.cpp", "int main() { return 0; }")),
				ofFile(SourceFile.of("headers/foo.h", "void sayHello();"))
			);
		}

		@Test
		void throwsExceptionOnSourceSetNameMismatch() {
			assertThat(() -> ofElements(
				ofFile(SourceFile.of("cpp/main.cpp", "int main() { return 0; }")).withSourceSetName("other"),
				ofFile(SourceFile.of("headers/foo.h", "void sayHello();")).withSourceSetName("test")
			), throwsException(message("elements must have the same source set name")));
		}

		@Test
		void overridesAllElementsSourceSetName() {
			List<SourceElement> subject = subject().withSourceSetName("other").getElements();
			assertThat(subject.get(0).getSourceSetName(), equalTo("other"));
			assertThat(subject.get(1).getSourceSetName(), equalTo("other"));
		}
	}

	@Test
	void doesNotShowDuplicatedSourceFileNamesFromDifferentDirectory() {
		SourceElement subject = SourceElement.ofFiles(
				SourceFile.of("java/Foo.java", "..."),
				SourceFile.of("java/com/example/Foo.java", "..."));
		assertThat(subject.getSourceFileNames(), containsInAnyOrder("Foo.java"));
	}
}
