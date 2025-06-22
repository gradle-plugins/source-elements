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

package dev.nokee.elements.nativebase;

import dev.nokee.elements.core.Element;
import dev.nokee.elements.core.SourceElement;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.elements.core.SourceElement.empty;

public abstract class NativeSourceElement extends Element {
	public SourceElement getHeaders() {
		return empty();
	}

	public NativeSourceElement withoutHeaders() {
		return new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return NativeSourceElement.this.getSources();
			}
		};
	}

	public NativeLibraryElement withPublicHeaders(SourceElement publicHeaders) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return publicHeaders;
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return NativeSourceElement.this.getHeaders();
			}

			@Override
			public SourceElement getSources() {
				return NativeSourceElement.this.getSources();
			}
		};
	}

	public abstract SourceElement getSources();

	public NativeSourceElement withoutSources() {
		return new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return NativeSourceElement.this.getHeaders();
			}

			@Override
			public SourceElement getSources() {
				return empty();
			}
		};
	}

	public static NativeSourceElement ofSources(SourceElement element) {
		return new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return element;
			}

			@Override
			public void accept(Visitor visitor) {
				element.accept(visitor);
			}
		};
	}

	public static NativeLibraryElement ofElements(NativeSourceElement... elements) {
		return ofElements(Arrays.asList(elements));
	}

	public static NativeLibraryElement ofElements(List<NativeSourceElement> elements) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return SourceElement.ofElements(elements.stream().filter(NativeLibraryElement.class::isInstance).map(it -> ((NativeLibraryElement) it).getPublicHeaders()).collect(Collectors.toList()));
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return SourceElement.ofElements(elements.stream().map(it -> {
					if (it instanceof NativeLibraryElement) {
						return ((NativeLibraryElement) it).getPrivateHeaders();
					} else {
						return it.getHeaders();
					}
				}).collect(Collectors.toList()));
			}

			@Override
			public NativeSourceElement asImplementation() {
				return ofElements(elements.stream().map(it -> {
					if (it instanceof NativeLibraryElement) {
						return ((NativeLibraryElement) it).asImplementation();
					} else {
						return it;
					}
				}).collect(Collectors.toList()));
			}

			@Override
			public SourceElement getSources() {
				return SourceElement.ofElements(elements.stream().map(it -> it.getSources()).collect(Collectors.toList()));
			}

			@Override
			public void accept(Visitor visitor) {
				for (NativeSourceElement element : elements) {
					visitor.visit(element);
				}
			}
		};
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
