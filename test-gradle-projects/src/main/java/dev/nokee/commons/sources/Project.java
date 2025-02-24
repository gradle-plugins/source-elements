package dev.nokee.commons.sources;

import java.nio.file.Path;

public interface Project {
	Path getLocation();
	Project writeToDirectory(Path directory);
	Path file(String path);
}
