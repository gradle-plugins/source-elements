package dev.nokee.elements.core;

import dev.nokee.elements.nativebase.NativeElement;
import dev.nokee.elements.nativebase.NativeLibraryElement;

public class GradleLayoutElement extends SimpleLayoutElement {
	public GradleLayoutElement(Element element) {
		super(element);
	}

	protected String projectPathOf(ProjectElement element) {
		return element.id().toString();
	}

	@Override
	protected void visitWorkspace(WorkspaceElement element, Context context) {
		for (ProjectElement project : element.getProjects()) {
			visit(project, context.dir(projectPathOf(project)));
		}
	}

	protected void visitProject(ProjectElement element, Context context) {
		visit(element.getMainElement(), context.dir("src/main"));
		visit(element.getTestElement(), context.dir("src/test"));
	}

	protected void visitNative(NativeElement element, Context context) {
		if (element instanceof NativeLibraryElement) {
			visit(((NativeLibraryElement) element).getPublicHeaders(), context.dir("public"));
			visit(((NativeLibraryElement) element).getPrivateHeaders(), context.dir("headers"));
		} else {
			visit(element.getHeaders(), context.dir("headers"));
		}

		visit(element.getSources(), context);
	}

	protected void visitSource(SourceElement element, Context context) {
		for (SourceFile source : element.getFiles()) {
			// TODO: Use UTI instead
			if (source.getName().endsWith(".c")) {
				visit(source, context.dir("c"));
			} else if (source.getName().endsWith(".cpp")) {
				visit(source, context.dir("cpp"));
			} else if (source.getName().endsWith(".java")) {
				visit(source, context.dir("java"));
			} else {
				visit(source, context);
			}
		}
	}
}
