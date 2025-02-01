package dev.gradleplugins.fixtures.sources;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Represent a source element in a subproject.
 */
public final class ProjectSourceElement extends SourceElement {
	private final String subproject;
	private final SourceElement delegate;

	public ProjectSourceElement(String subproject, SourceElement delegate) {
		this.subproject = subproject;
		this.delegate = delegate;
	}

	public ProjectSourceElement withSubproject(String subproject) {
		return new ProjectSourceElement(subproject, delegate);
	}

	public ProjectSourceElement withElement(Function<? super SourceElement, ? extends SourceElement> mapper) {
		return new ProjectSourceElement(subproject, mapper.apply(delegate));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeToProject(Path projectDir) {
		delegate.writeToProject(projectDir.resolve(subproject));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SourceFile> getFiles() {
		return delegate.getFiles();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSourceSetName() {
		return delegate.getSourceSetName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SourceElement withSourceSetName(String sourceSetName) {
		return new ProjectSourceElement(subproject, delegate.withSourceSetName(sourceSetName));
	}
}
