package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class WriteToZipTests {
	@Test
	void canWriteSourceElementInDifferentFileSystem(@TempDir Path testDirectory) throws IOException {
		SourceElement subject = new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.singletonList(sourceFile("foo.cpp", "..."));
			}
		};

		URI uri = URI.create("jar:file:" + testDirectory.resolve("sources.zip"));
		try (FileSystem zipfs = FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"))) {
			subject.writeToDirectory(zipfs.getPath("/"));
		}
	}
}
