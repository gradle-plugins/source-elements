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

import java.util.stream.Collectors;

import static dev.gradleplugins.fixtures.sources.SourceElement.empty;

public class NativeElements {
	public static SourceElement privateHeaders(SourceElement element) {
		if (element instanceof NativeLibraryElement) {
			return ((NativeLibraryElement) element).getPrivateHeaders();
		} else if (element instanceof NativeSourceElement) {
			return ((NativeSourceElement) element).getHeaders();
		} else if (element instanceof ProjectSourceElement) {
			return ((ProjectSourceElement) element).withElement(NativeElements::privateHeaders);
		} else if (element instanceof CompositeSourceElement) {
			return SourceElement.ofElements(((CompositeSourceElement) element).getElements().stream().map(NativeElements::privateHeaders).collect(Collectors.toList()));
		} else {
			return empty().withSourceSetName(element.getSourceSetName());
		}
	}

	public static SourceElement publicHeaders(SourceElement element) {
		if (element instanceof NativeLibraryElement) {
			return ((NativeLibraryElement) element).getPublicHeaders();
		} else if (element instanceof ProjectSourceElement) {
			return ((ProjectSourceElement) element).withElement(NativeElements::publicHeaders);
		} else if (element instanceof CompositeSourceElement) {
			return SourceElement.ofElements(((CompositeSourceElement) element).getElements().stream().map(NativeElements::publicHeaders).collect(Collectors.toList()));
		} else {
			return empty().withSourceSetName(element.getSourceSetName());
		}
	}

	public static SourceElement headers(SourceElement element) {
		if (element instanceof NativeSourceElement) {
			return ((NativeSourceElement) element).getHeaders();
		} else if (element instanceof ProjectSourceElement) {
			return ((ProjectSourceElement) element).withElement(NativeElements::headers);
		} else if (element instanceof CompositeSourceElement) {
			return SourceElement.ofElements(((CompositeSourceElement) element).getElements().stream().map(NativeElements::headers).collect(Collectors.toList()));
		} else {
			return empty().withSourceSetName(element.getSourceSetName());
		}
	}

	public static SourceElement sources(SourceElement element) {
		if (element instanceof NativeSourceElement) {
			return ((NativeSourceElement) element).getSources();
		} else if (element instanceof ProjectSourceElement) {
			return ((ProjectSourceElement) element).withElement(NativeElements::sources);
		} else if (element instanceof CompositeSourceElement) {
			return SourceElement.ofElements(((CompositeSourceElement) element).getElements().stream().map(NativeElements::sources).collect(Collectors.toList()));
		} else {
			return element;
		}
	}

	public static SourceElement.ConvertOperation subproject(String subprojectPath) {
		return it -> new ProjectSourceElement(subprojectPath, it);
	}

	public static SourceElement.ConvertOperation lib() {
		return NativeElements::lib;
	}

	private static SourceElement lib(SourceElement element) {
		if (element instanceof NativeLibraryElement) {
			return ((NativeLibraryElement) element).asLib();
		} else if (element instanceof ProjectSourceElement) {
			return ((ProjectSourceElement) element).withElement(NativeElements::lib);
		} else if (element instanceof CompositeSourceElement) {
			return SourceElement.ofElements(((CompositeSourceElement) element).getElements().stream().map(NativeElements::lib).collect(Collectors.toList()));
		} else {
			return element;
		}
	}
}
