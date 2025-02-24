package dev.gradleplugins.fixtures.sources;

import java.util.List;

public abstract class ResourceElement<T extends SourceElement> {
	public abstract T delegate();

	public ResourceElement<T> with(String propName, String value) {
		throw new UnsupportedOperationException();
	}

	public SourceFile getSourceFile() {
		throw new UnsupportedOperationException();
	}

	public static ResourceElement<SourceFileElement> from(SourceElement source, List<DelegatedElements.Property> properties) {
		assert source.getFiles().size() == 1;
		SourceFile file = source.getFiles().get(0);
		return new ResourceElement<SourceFileElement>() {
			@Override
			public SourceFileElement delegate() {
				return SourceFileElement.ofFile(file).withSourceSetName(source.getSourceSetName());
			}

			@Override
			public ResourceElement<SourceFileElement> with(String propName, String value) {
				DelegatedElements.Property prop = properties.stream().filter(it -> it.getName().equals(propName)).findFirst().orElseThrow(RuntimeException::new);
				String content = prop.getPattern().matcher(file.getContent()).replaceAll(result -> {
					return result.group().substring(0, result.start(1) - result.start()) + value + result.group().substring(result.end(1) - result.start());
				});
				return from(SourceFileElement.ofFile(new SourceFile(file.getPath(), file.getName(), content)), properties);
			}

			@Override
			public SourceFile getSourceFile() {
				return file;
			}
		};
	}
}
