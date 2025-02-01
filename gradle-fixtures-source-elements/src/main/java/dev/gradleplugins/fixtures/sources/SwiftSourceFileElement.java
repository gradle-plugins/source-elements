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

import java.util.Collections;
import java.util.List;

public abstract class SwiftSourceFileElement extends SwiftSourceElement {
	public abstract SourceFile getSourceFile();

	@Override
	public List<SourceFile> getFiles() {
		return Collections.singletonList(getSourceFile());
	}

	//	public SwiftSourceFileElement withImport(String moduleToImport) {
//		return new SwiftSourceFileElement() {
//			private final SourceFile delegate = SwiftSourceFileElement.this.getSourceFile();
//
//			@Override
//			public SourceFile getSourceFile() {
//				return sourceFile(delegate.getPath(), delegate.getName(), String.join("\n",
//					"import " + moduleToImport,
//					"",
//					delegate.getContent()
//				));
//			}
//
//			@Override
//			public String getSourceSetName() {
//				return SwiftSourceFileElement.this.getSourceSetName();
//			}
//		};
//	}
}
