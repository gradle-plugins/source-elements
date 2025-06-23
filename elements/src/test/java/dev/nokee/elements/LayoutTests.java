package dev.nokee.elements;

import dev.nokee.elements.core.*;
import dev.nokee.elements.nativebase.NativeElement;
import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static dev.nokee.commons.hamcrest.gradle.FileSystemMatchers.hasRelativeDescendants;
import static org.hamcrest.MatcherAssert.assertThat;

class LayoutTests {
	WorkspaceElement workspace = new WorkspaceElement() {
		@Override
		public List<ProjectElement> getProjects() {
			return Arrays.asList(
				new ProjectElement("app") {
					@Override
					public Element getMainElement() {
						return new NativeSourceElement() {
							@Override
							public SourceElement getHeaders() {
								return ofFiles(
									sourceFile("bar.h",
										"#pragma once",
										"int bar();")
								);
							}

							@Override
							public SourceElement getSources() {
								return ofFiles(
									sourceFile("main.cpp",
										"#include \"foo/foo.h\"",
										"#include \"bar.h\"",
										"int main() { return foo() + bar(); }"),
									sourceFile("bar.cpp",
										"#include \"bar.h\"",
										"int bar() { return 10; }")
								);
							}
						};
					}
				},
				new ProjectElement("lib") {
					@Override
					public Element getMainElement() {
						return new NativeLibraryElement() {
							@Override
							public SourceElement getPublicHeaders() {
								return ofFiles(
									sourceFile("foo/foo.h",
										"#pragma once",
										"int foo();")
								);
							}

							@Override
							public SourceElement getSources() {
								return ofFiles(sourceFile("foo.cpp",
										"#include \"foo/foo.h\"",
										"int foo() { return 32; }"
									));
							}
						};
					}

					@Override
					public Element getTestElement() {
						return new NativeSourceElement() {
							@Override
							public SourceElement getHeaders() {
								return SourceFileElement.ofFile(sourceFile("assertion-code.h",
									"#define SUCCESS 0",
									"#define FAILURE -1")
								);
							}

							@Override
							public SourceElement getSources() {
								return SourceFileElement.ofFile(sourceFile("main.cpp",
									"#include \"foo/foo.h\"",
									"#include \"assertion-code.h\"",
									"int main() { return foo() == 32 ? SUCCESS : FAILURE;"));
							}
						};
					}
				}
			);
		}
	};

	@Test
	void xcodeLayout(@TempDir Path testDirectory) {
		new XcodeLayoutElement() {
			@Override
			protected String projectNameOf(ProjectElement element) {
				return StringUtils.capitalize(element.id().toString());
			}
		}.applyTo(workspace).writeToDirectory(testDirectory);

		assertThat(testDirectory, hasRelativeDescendants(
			"App/main.cpp",
			"App/bar.cpp",
			"App/bar.h",
			"Lib/foo.cpp",
			"Lib/foo/foo.h",
			"LibTest/main.cpp",
			"LibTest/assertion-code.h"
		));
	}

	@Test
	void traditionalNative(@TempDir Path testDirectory) {
		new TraditionalNativeLayoutElement() {
			@Override
			protected String projectNameOf(ProjectElement element) {
				return element.id().toString();
			}
		}.applyTo(workspace).writeToDirectory(testDirectory);
		assertThat(testDirectory, hasRelativeDescendants(
			"src/app/main.cpp",
			"src/app/bar.cpp",
			"src/app/bar.h",
			"src/lib/foo.cpp",
			"src/lib/tests/main.cpp",
			"src/lib/tests/assertion-code.h",
			"includes/lib/foo/foo.h"
		));
	}

	@Test
	void standardGradle(@TempDir Path testDirectory) {
		new GradleLayoutElement().applyTo(workspace).writeToDirectory(testDirectory);
		assertThat(testDirectory, hasRelativeDescendants(
			"app/src/main/cpp/main.cpp",
			"app/src/main/cpp/bar.cpp",
			"app/src/main/headers/bar.h",
			"lib/src/main/cpp/foo.cpp",
			"lib/src/main/public/foo/foo.h",
			"lib/src/test/cpp/main.cpp",
			"lib/src/test/headers/assertion-code.h"
		));
	}

	public abstract static class TraditionalNativeLayoutElement extends SimpleLayoutElement {
		protected abstract String projectNameOf(ProjectElement element);

		@Override
		protected void visitWorkspace(WorkspaceElement element, Context context) {
			for (ProjectElement project : element.getProjects()) {
				includeCtx = context.dir("includes").dir(projectNameOf(project));
				context.dir("src").dir(projectNameOf(project)).visit(project);
			}
		}

		Context includeCtx = null;

		@Override
		protected void visitProject(ProjectElement element, Context context) {
			context.visit(element.getMainElement());
			context.dir("tests").visit(element.getTestElement());
		}

		@Override
		protected void visitNative(NativeElement element, Context context) {
			if (element instanceof NativeLibraryElement) {
				includeCtx.visit(((NativeLibraryElement) element).getPublicHeaders());
				context.visit(((NativeLibraryElement) element).getPrivateHeaders());
			} else {
				context.visit(element.getHeaders());
			}

			context.visit(element.getSources());
		}

		@Override
		protected void visitSource(SourceElement element, Context context) {
			context.visitSources(element);
		}
	}

	public abstract static class XcodeLayoutElement extends SimpleLayoutElement {
		protected abstract String projectNameOf(ProjectElement element);

		@Override
		protected void visitWorkspace(WorkspaceElement element, Context context) {
			for (ProjectElement project : element.getProjects()) {
				context.dir(projectNameOf(project)).visit(project.getMainElement());
				context.dir(projectNameOf(project) + "Test").visit(project.getTestElement());
			}
		}

		@Override
		protected void visitProject(ProjectElement element, Context context) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void visitNative(NativeElement element, Context context) {
			context.visit(element.getHeaders());
			context.visit(element.getSources());
		}

		@Override
		protected void visitSource(SourceElement element, Context context) {
			context.visitSources(element);
		}
	}
}
