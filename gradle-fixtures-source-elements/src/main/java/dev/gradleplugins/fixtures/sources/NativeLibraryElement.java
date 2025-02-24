/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.fixtures.sources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static dev.gradleplugins.fixtures.sources.DelegatedElements.sourceSetNameOf;

/**
 * Represents a native library with public/private headers and sources.
 */
public abstract class NativeLibraryElement extends NativeSourceElement {
	/**
	 * {@return the public headers of this library element}
	 */
	public abstract SourceElement getPublicHeaders();

	/**
	 * {@return the private headers of this library element}
	 */
	public SourceElement getPrivateHeaders() {
		return empty().withSourceSetName(getSourceSetName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final SourceElement getHeaders() {
		return ofElements(getPublicHeaders().withSourceSetName(getSourceSetName()), getPrivateHeaders().withSourceSetName(getSourceSetName()));
	}

	/**
	 * Returns a copy of this library with the public headers the 'public' headers directory.
	 */
	public NativeSourceElement asLib() {
		return new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return SourceElement.ofElements(
					NativeLibraryElement.this.getPrivateHeaders(),
					withFiles(NativeLibraryElement.this.getPublicHeaders(), files -> {
						return files.stream().map(sourceFile -> {
							int idx = sourceFile.getPath().indexOf('/');
							if (idx == -1) {
								return new SourceFile("public", sourceFile.getName(), sourceFile.getContent());
							} else {
								return new SourceFile("public" + sourceFile.getPath().substring(idx), sourceFile.getName(), sourceFile.getContent());
							}
						}).collect(Collectors.toList());
					})
				);
			}

			private SourceElement withFiles(SourceElement self, UnaryOperator<List<SourceFile>> operation) {
				return SourceElement.ofFiles(operation.apply(self.getFiles())).withSourceSetName(self.getSourceSetName());
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources();
			}

			@Override
			public String getSourceSetName() {
				return NativeLibraryElement.this.getSourceSetName();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NativeLibraryElement withSourceSetName(String sourceSetName) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return NativeLibraryElement.this.getPublicHeaders().withSourceSetName(sourceSetName);
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return NativeLibraryElement.this.getPrivateHeaders().withSourceSetName(sourceSetName);
			}

			@Override
			public String getSourceSetName() {
				return sourceSetName;
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources().withSourceSetName(sourceSetName);
			}
		};
	}

	public abstract static class FromResource extends NativeLibraryElement implements ResourceElementEx {
		private final NativeLibraryElement delegate;

		protected FromResource() {
			NativeLibraryElement delegate = toNativeLibrary(fromResource(getClass()));
			String sourceSetName = sourceSetNameOf(this, FromResource.class).orElse(null);
			if (sourceSetName != null) {
				delegate = delegate.withSourceSetName(sourceSetName);
			}
			this.delegate = delegate;
		}

		private static NativeLibraryElement toNativeLibrary(SourceElement self) {
			List<SourceFile> publicHeaders = new ArrayList<>();
			List<SourceFile> privateHeaders = new ArrayList<>();
			List<SourceFile> sources = new ArrayList<>();
			for (SourceFile file : self.getFiles()) {
				if (file.getPath().startsWith("public")) {
					publicHeaders.add(new SourceFile("headers" + file.getPath().substring("public".length()), file.getName(), file.getContent()));
				} else if (file.getPath().startsWith("headers")) {
					privateHeaders.add(file);
				} else {
					sources.add(file);
				}
			}
			return new NativeLibraryElement() {
				@Override
				public SourceElement getPrivateHeaders() {
					return ofFiles(privateHeaders).withSourceSetName(self.getSourceSetName());
				}

				@Override
				public SourceElement getPublicHeaders() {
					return ofFiles(publicHeaders).withSourceSetName(self.getSourceSetName());
				}

				@Override
				public SourceElement getSources() {
					return ofFiles(sources).withSourceSetName(self.getSourceSetName());
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final SourceElement getPublicHeaders() {
			return delegate.getPublicHeaders();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final SourceElement getPrivateHeaders() {
			return delegate.getPrivateHeaders();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final SourceElement getSources() {
			return delegate.getSources();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final NativeLibraryElement withSourceSetName(String sourceSetName) {
			return super.withSourceSetName(sourceSetName);
		}

		public NativeLibraryElement withSources(SourceElement sources) {
			return new NativeLibraryElement() {
				@Override
				public SourceElement getSources() {
					return sources;
				}

				@Override
				public SourceElement getPublicHeaders() {
					return delegate.getPublicHeaders();
				}

				@Override
				public SourceElement getPrivateHeaders() {
					return delegate.getPrivateHeaders();
				}

				@Override
				public String getSourceSetName() {
					return delegate.getSourceSetName();
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override // allow override
		public String getSourceSetName() {
			return delegate.getSourceSetName();
		}
	}
}
