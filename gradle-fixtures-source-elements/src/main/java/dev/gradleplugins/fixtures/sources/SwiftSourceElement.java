package dev.gradleplugins.fixtures.sources;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent an element containing zero or more Swift source files.
 */
public abstract class SwiftSourceElement extends SourceElement {
	/**
	 * {@return a new source element when each files has the imported module}
	 */
	public SwiftSourceElement withImport(String moduleToImport) {
		return new SwiftSourceElement() {
			@Override
			public String getSourceSetName() {
				return SwiftSourceElement.this.getSourceSetName();
			}

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

	/**
	 * Represents a source element definition coming from XML resource.
	 */
    public abstract static class FromResource extends SwiftSourceElement {
        private final SourceElement delegate;

        protected FromResource() {
            this.delegate = DelegatedElements.sourceOf(getClass());
        }

        protected FromResource(SourceElement delegate) {
            this.delegate = delegate;
        }

        @Override
        public final List<SourceFile> getFiles() {
            return delegate.getFiles();
        }

        @Override // allow override
        public String getSourceSetName() {
            return delegate.getSourceSetName();
        }
    }
}
