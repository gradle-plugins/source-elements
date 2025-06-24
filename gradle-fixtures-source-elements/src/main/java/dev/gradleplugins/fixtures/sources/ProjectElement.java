package dev.gradleplugins.fixtures.sources;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ProjectElement extends Element {
	public abstract Path getLocation();

	public abstract ProjectElement writeToDirectory(Path directory);

	public Path file(String path) {
		final Path result = getLocation().resolve(path);
		try {
			Files.createDirectories(result.getParent());
			if (Files.exists(result)) {
				assert Files.isRegularFile(result);
			} else {
				Files.createFile(result);
			}
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public Path dir(String path) {
		final Path result = getLocation().resolve(path);
		try {
			if (Files.exists(result)) {
				assert Files.isDirectory(result);
			} else {
				Files.createDirectories(result);
			}
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
