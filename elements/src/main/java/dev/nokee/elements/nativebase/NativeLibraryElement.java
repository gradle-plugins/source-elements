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

import static dev.nokee.elements.core.SourceElement.empty;
import static dev.nokee.elements.core.SourceElement.ofElements;


/**
 * Represents a native library with public/private headers and sources.
 */
public abstract class NativeLibraryElement extends NativeSourceElement {
	/**
	 * {@return the public headers of this library element}
	 */
	public abstract SourceElement getPublicHeaders();

	public NativeLibraryElement withoutPublicHeaders() {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return empty();
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

	public NativeLibraryElement withoutPrivateHeaders() {
		return new NativeLibraryElement() {
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

	public NativeLibraryElement withoutHeaders() {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return empty();
			}

			@Override
			public SourceElement getSources() {
				return NativeLibraryElement.this.getSources();
			}
		};
	}

	/**
	 * {@return the private headers of this library element}
	 */
	public SourceElement getPrivateHeaders() {
		return empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final SourceElement getHeaders() {
		return SourceElement.ofElements(getPublicHeaders(), getPrivateHeaders());
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

	public NativeSourceElement asImplementation() {
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

//	public static NativeLibraryElement ofSources(SourceElement sources) {
//		return new NativeLibraryElement() {
//			@Override
//			public SourceElement getSources() {
//				return sources;
//			}
//
//			@Override
//			public SourceElement getPublicHeaders() {
//				return empty();
//			}
//		};
//	}
}
