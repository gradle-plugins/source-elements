/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.fixtures.sources;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.gradleplugins.fixtures.sources.SourceElement.empty;
import static dev.gradleplugins.fixtures.sources.SourceElement.ofElements;
import static dev.gradleplugins.fixtures.sources.SourceElement.ofFiles;

public class NativeElements {
	public static SourceElement.QueryOperation privateHeaders() {
		return it -> {
			return it.accept(new VisitSubElements(new NativeVisitor(new NativeVisitor.Visitor() {
				@Override
				public SourceElement visit(SourceElement element) {
					return empty();
				}

				// In theory, we should check NativeSourceElement,
				//   but we have to according to the legacy implementation
				@Override
				public SourceElement visit(NativeSourceElement element) {
					return keepSourceSetName(element, whereHeaderFiles(it -> !it.getPath().equals("public")));
				}
			})));
		};
	}

	public static SourceElement.QueryOperation publicHeaders() {
		return it -> {
			return it.accept(new VisitSubElements(new NativeVisitor(new NativeVisitor.Visitor() {
				@Override
				public SourceElement visit(SourceElement element) {
					return empty();
				}

				// In theory, we should check NativeSourceElement,
				//   but we have to according to the legacy implementation
				@Override
				public SourceElement visit(NativeSourceElement element) {
					return keepSourceSetName(element, whereHeaderFiles(it -> it.getPath().equals("public")));
				}
			})));
		};
	}

	private static Function<NativeSourceElement, SourceElement> whereHeaderFiles(Predicate<? super SourceFile> predicate) {
		return element -> ofFiles(element.getHeaders().getFiles().stream().filter(predicate).collect(Collectors.toList()));
	}

	public static SourceElement.QueryOperation headers() {
		return it -> {
			return it.accept(new VisitSubElements(new NativeVisitor(new NativeVisitor.Visitor() {
				@Override
				public SourceElement visit(SourceElement element) {
					return empty();
				}

				@Override
				public SourceElement visit(NativeSourceElement element) {
					return keepSourceSetName(element, NativeSourceElement::getHeaders);
				}
			})));
		};
	}

	public static SourceElement.QueryOperation sources() {
		return it -> {
			return it.accept(new VisitSubElements(new NativeVisitor(new NativeVisitor.Visitor() {
				@Override
				public SourceElement visit(SourceElement element) {
					return element;
				}

				@Override
				public SourceElement visit(NativeSourceElement element) {
					return keepSourceSetName(element, NativeSourceElement::getSources);
				}
			})));
		};
	}

	private static <T extends SourceElement> SourceElement keepSourceSetName(T element, Function<? super T, ? extends SourceElement> transform) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return transform.apply(element).getFiles();
			}

			@Override
			public String getSourceSetName() {
				return element.getSourceSetName();
			}
		};
	}

	private static final class NativeVisitor implements VisitSubElements.Visitor {
		private final Visitor delegate;

		private NativeVisitor(Visitor delegate) {
			this.delegate = delegate;
		}

		@Override
		public SourceElement visit(SourceElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(SourceFileElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(NativeSourceElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(NativeLibraryElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(NativeSourceFileElement element) {
			return delegate.visit(element);
		}

		interface Visitor {
			SourceElement visit(SourceElement element);

			SourceElement visit(NativeSourceElement element);
		}
	}


	private static final class VisitSubElements implements SourceElement.Visitor<SourceElement> {
		private final Visitor delegate;

		private VisitSubElements(Visitor delegate) {
			this.delegate = delegate;
		}

		@Override
		public SourceElement visit(SourceElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(SourceFileElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(NativeSourceElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(NativeLibraryElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(NativeSourceFileElement element) {
			return delegate.visit(element);
		}

		@Override
		public SourceElement visit(SourceElements element) {
			return ofElements(element.getElements().stream().map(t -> t.accept(this)).collect(Collectors.toList()));
		}

		interface Visitor {
			SourceElement visit(SourceElement element);

			SourceElement visit(SourceFileElement element);

			SourceElement visit(NativeSourceElement element);

			SourceElement visit(NativeLibraryElement element);

			SourceElement visit(NativeSourceFileElement element);
		}
	}

	public static SourceElement.ConvertOperation subproject(String subprojectPath) {
		return it -> {
			return new SourceElement() {
				@Override
				public List<SourceFile> getFiles() {
					return it.getFiles();
				}

				@Override
				public String getSourceSetName() {
					return it.getSourceSetName();
				}

				@Override
				public void writeToProject(Path projectDir) {
					super.writeToProject(projectDir.resolve(subprojectPath));
				}

				@Override
				public <R> R accept(Visitor<R> visitor) {
					return it.accept(visitor);
				}
			};
		};
	}

	public static SourceElement.ConvertOperation lib() {
		return it -> {
			return it.accept(new VisitSubElements(new VisitSubElements.Visitor() {
				@Override
				public SourceElement visit(SourceElement element) {
					return element;
				}

				@Override
				public SourceElement visit(SourceFileElement element) {
					return element;
				}

				@Override
				public SourceElement visit(NativeSourceElement element) {
					return element;
				}

				@Override
				public SourceElement visit(NativeLibraryElement element) {
					return asLib(element);
				}

				@Override
				public SourceElement visit(NativeSourceFileElement element) {
					return asLib(element);
				}

				private NativeSourceElement asLib(NativeLibraryElement element) {
					return new NativeSourceElement() {
						@Override
						public SourceElement getHeaders() {
							final List<SourceFile> headers = new ArrayList<>();
							for (SourceFile sourceFile : element.getPublicHeaders().getFiles()) {
								headers.add(sourceFile("public", sourceFile.getName(), sourceFile.getContent()));
							}
							return ofElements(ofFiles(headers), element.getPrivateHeaders());
						}

						@Override
						public SourceElement getSources() {
							return element.getSources();
						}

						@Override
						public String getSourceSetName() {
							return element.getSourceSetName();
						}
					};
				}
			}));
		};
	}
}
