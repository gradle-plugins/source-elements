package dev.nokee.elements.core;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class SwiftSourceElement extends SourceElement {
	/**
	 * {@return a new source element where each files has the imported module}
	 */
	public SwiftSourceElement withImport(String moduleToImport) {
		return new SwiftSourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return SwiftSourceElement.this.getFiles().stream().map(SwiftSourceElement.withImportedModule(moduleToImport)).collect(Collectors.toList());
			}
		};
	}

	private static UnaryOperator<SourceFile> withImportedModule(String moduleToImport) {
		return sourceFile -> {
			return sourceFile.withContent(content -> {
				return String.join("\n",
					"import " + moduleToImport,
					"",
					content
				);
			});
		};
	}
}
