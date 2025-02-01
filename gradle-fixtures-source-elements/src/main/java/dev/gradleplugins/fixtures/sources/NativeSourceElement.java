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

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public abstract class NativeSourceElement extends SourceElement {
	private final SourceElement delegate;

	protected NativeSourceElement() {
		this.delegate = SourceElement.ofElements(getHeaders().withSourceSetName(getSourceSetName()), getSources().withSourceSetName(getSourceSetName()));
	}

	public SourceElement getHeaders() {
		return empty().withSourceSetName(getSourceSetName());
	}

	public abstract SourceElement getSources();

	/**
	 * {@inheritDoc}
	 */
	public List<SourceFile> getFiles() {
		return delegate.getFiles();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeToProject(Path projectDir) {
		delegate.writeToProject(projectDir);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NativeSourceElement withSourceSetName(String sourceSetName) {
		return new NativeSourceElement() {
			@Override
			public SourceElement getHeaders() {
				return NativeSourceElement.this.getHeaders().withSourceSetName(sourceSetName);
			}

			@Override
			public SourceElement getSources() {
				return NativeSourceElement.this.getSources().withSourceSetName(sourceSetName);
			}

			@Override
			public String getSourceSetName() {
				return sourceSetName;
			}
		};
	}

	public final Set<String> getSourceFileNamesWithoutHeaders() {
		return getSources().getSourceFileNames();
	}

	public static NativeSourceElement ofSources(SourceElement sources) {
		return new NativeSourceElement() {
			@Override
			public SourceElement getSources() {
				return sources;
			}

			@Override
			public String getSourceSetName() {
				return sources.getSourceSetName();
			}
		};
	}
}
