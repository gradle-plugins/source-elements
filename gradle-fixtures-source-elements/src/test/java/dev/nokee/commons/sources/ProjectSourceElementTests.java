package dev.nokee.commons.sources;

import dev.gradleplugins.fixtures.sources.ProjectSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.hasRelativeDescendants;
import static org.hamcrest.MatcherAssert.assertThat;

class ProjectSourceElementTests {
	@Test
	void writesSourceElementToSubproject(@TempDir Path testDirectory) {
		SourceElement subject = new ProjectSourceElement("library", SourceElement.ofFiles(SourceFile.of("c/main.c", "...")));
		subject.writeToProject(testDirectory);
		assertThat(testDirectory, hasRelativeDescendants("library/src/main/c/main.c"));
	}

	@Test
	void conserveSubprojectOnSourceSetNameOverride(@TempDir Path testDirectory) {
		SourceElement subject = new ProjectSourceElement("library", SourceElement.ofFiles(SourceFile.of("c/main.c", "...")));
		subject.withSourceSetName("other").writeToProject(testDirectory);
		assertThat(testDirectory, hasRelativeDescendants("library/src/other/c/main.c"));
	}

	@Test
	void canOverrideSubproject(@TempDir Path testDirectory) {
		ProjectSourceElement subject = new ProjectSourceElement("library", SourceElement.ofFiles(SourceFile.of("c/main.c", "...")));
		subject.withSubproject("lib").writeToProject(testDirectory);
		assertThat(testDirectory, hasRelativeDescendants("lib/src/main/c/main.c"));
	}
}
