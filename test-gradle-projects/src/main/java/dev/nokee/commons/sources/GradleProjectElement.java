package dev.nokee.commons.sources;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.fixtures.sources.Element;
import dev.gradleplugins.fixtures.sources.ProjectElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class GradleProjectElement extends ProjectElement {
	private final List<Element> elements = new ArrayList<>();
	private final GradleBuildFile buildFile = null;
//	GradleProject inDirectory(Path directory);

	public GradleBuildFile getBuildFile() {
		return buildFile;
	}

	@Override
	public GradleProjectElement writeToDirectory(Path directory) {
		throw new UnsupportedOperationException();
	}


	static GradleProjectElement empty() {
		throw new UnsupportedOperationException();
	}
}
