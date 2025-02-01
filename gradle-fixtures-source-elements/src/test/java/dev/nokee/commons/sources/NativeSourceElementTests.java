package dev.nokee.commons.sources;

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.hasRelativeDescendants;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NativeSourceElementTests {
	@Test
	void defaultHeadersHasElementSourceSetName() {
		NativeSourceElement subject = new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return SourceFileElement.ofFile(SourceFile.of("c/main.c", "int main() { return 0; }"));
			}

			@Override
			public String getSourceSetName() {
				return "test";
			}
		};

		assertThat(subject.getHeaders().getSourceSetName(), equalTo("test"));
	}

	@Test
	void ignoresSourcesAndHeadersSourceSetName(@TempDir Path testDirectory) {
		NativeSourceElement subject = new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return SourceFileElement.ofFile(SourceFile.of("headers/foo.h", "int foo();")).withSourceSetName("other");
			}

			@Override
			public SourceElement getSources() {
				return SourceFileElement.ofFile(SourceFile.of("c/main.c", "int main() { return 0; }")).withSourceSetName("another");
			}

			@Override
			public String getSourceSetName() {
				return "test";
			}
		};
		subject.writeToProject(testDirectory);

		assertThat(testDirectory, hasRelativeDescendants(
			"src/test/c/main.c",
			"src/test/headers/foo.h"
		));
	}

	@Test
	void overrideSourcesAndHeadersSourceSetName(@TempDir Path testDirectory) {
		NativeSourceElement subject = new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return SourceFileElement.ofFile(SourceFile.of("headers/foo.h", "int foo();")).withSourceSetName("other");
			}

			@Override
			public SourceElement getSources() {
				return SourceFileElement.ofFile(SourceFile.of("c/main.c", "int main() { return 0; }")).withSourceSetName("another");
			}
		}.withSourceSetName("integTest");

		assertThat(subject.getHeaders().getSourceSetName(), equalTo("integTest"));
		assertThat(subject.getSources().getSourceSetName(), equalTo("integTest"));

		subject.writeToProject(testDirectory);

		assertThat(testDirectory, hasRelativeDescendants(
			"src/integTest/c/main.c",
			"src/integTest/headers/foo.h"
		));
	}

	@Test
	void canCreateElementWithSourcesOnly() {
		NativeSourceElement subject = NativeSourceElement.ofSources(SourceFileElement.ofFile(SourceFile.of("c/main.c", "int main() { return 0; }")).withSourceSetName("other"));

		assertThat(subject.getSourceSetName(), equalTo("other"));
		assertThat(subject.getHeaders().getSourceSetName(), equalTo("other"));
		assertThat(subject.getSources().getSourceSetName(), equalTo("other"));
		assertThat(subject.getFiles(), contains(SourceFile.of("c/main.c", "int main() { return 0; }")));
		assertThat(subject.getSourceFileNames(), containsInAnyOrder("main.c"));
		assertThat(subject.getSourceFileNamesWithoutHeaders(), contains("main.c"));
	}

	@Test
	void createCreateElementViaImplementation() {
		NativeSourceElement subject = new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return SourceFileElement.ofFile(SourceFile.of("headers/foo.h", "int foo();")).withSourceSetName("other");
			}

			@Override
			public SourceElement getSources() {
				return SourceFileElement.ofFile(SourceFile.of("c/main.c", "int main() { return 0; }")).withSourceSetName("another");
			}

			@Override
			public String getSourceSetName() {
				return "test";
			}
		};

		assertThat(subject.getSourceSetName(), equalTo("test"));
		assertThat(subject.getHeaders().getSourceSetName(), equalTo("other")); // can do much
		assertThat(subject.getSources().getSourceSetName(), equalTo("another")); // can do much
		assertThat(subject.getFiles(), containsInAnyOrder(SourceFile.of("c/main.c", "int main() { return 0; }"), SourceFile.of("headers/foo.h", "int foo();")));
		assertThat(subject.getSourceFileNames(), containsInAnyOrder("main.c", "foo.h"));
		assertThat(subject.getSourceFileNamesWithoutHeaders(), containsInAnyOrder("main.c"));
	}
}
