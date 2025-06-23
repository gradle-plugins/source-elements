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

import dev.nokee.elements.core.SourceElement;

/**
 * Represents a native library with public/private headers and sources.
 */
public abstract class NativeLibraryElement extends NativeElement {
	//region public headers
	/**
	 * {@return the public headers of this library element}
	 */
	public abstract SourceElement getPublicHeaders();

	public final NativeLibraryElement withoutPublicHeaders() {
		return withPublicHeaders(SourceElement.empty());
	}

	public final NativeLibraryElement withPublicHeaders(SourceElement headers) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return headers;
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return NativeLibraryElement.this.getPrivateHeaders();
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources();
			}
		};
	}
	//endregion

	//region private headers
	/**
	 * {@return the private headers of this library element}
	 */
	public SourceElement getPrivateHeaders() {
		return SourceElement.empty();
	}

	public final NativeLibraryElement withoutPrivateHeaders() {
		return withPrivateHeaders(SourceElement.empty());
	}

	public final NativeLibraryElement withPrivateHeaders(SourceElement headers) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPrivateHeaders() {
				return headers;
			}

			@Override
			public SourceElement getPublicHeaders() {
				return NativeLibraryElement.this.getPublicHeaders();
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources();
			}
		};
	}
	//endregion

	//region headers
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final SourceElement getHeaders() {
		return SourceElement.ofElements(getPublicHeaders(), getPrivateHeaders());
	}

	/**
	 * {@inheritDoc}
	 */
	public final NativeLibraryElement withoutHeaders() {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return SourceElement.empty();
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources();
			}
		};
	}
	//endregion

	//region sources
	public final NativeLibraryElement withoutSources() {
		return withSources(SourceElement.empty());
	}

	public final NativeLibraryElement withSources(SourceElement sources) {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return NativeLibraryElement.this.getPublicHeaders();
			}

			@Override
			public SourceElement getPrivateHeaders() {
				return NativeLibraryElement.this.getPrivateHeaders();
			}

			@Override
			public SourceElement getSources() {
				return sources;
			}
		};
	}
	//endregion

	public NativeElement asImplementation() {
		return new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return NativeLibraryElement.this.getHeaders();
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources();
			}
		};
	}

	public static NativeLibraryElement empty() {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return SourceElement.empty();
			}

			@Override
			public SourceElement getSources() {
				return SourceElement.empty();
			}
		};
	}
}
