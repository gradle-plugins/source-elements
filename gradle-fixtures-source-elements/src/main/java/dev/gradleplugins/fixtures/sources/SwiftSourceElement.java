package dev.gradleplugins.fixtures.sources;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SwiftSourceElement extends SourceElement {
	public SwiftSourceElement withImport(String moduleToImport) {
		return new SwiftSourceElement() {
			@Override
			public String getSourceSetName() {
				return SwiftSourceElement.this.getSourceSetName();
			}

			// FIXME....
			@Override
			public List<SourceFile> getFiles() {
				return SwiftSourceElement.this.getFiles().stream().map(delegate -> {
					return new SourceFile(delegate.getPath(), delegate.getName(), String.join("\n",
						"import " + moduleToImport,
						"",
						delegate.getContent()
					));
				}).collect(Collectors.toList());
			}
		};
	}
}
