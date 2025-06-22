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
			context.dir(projectPathOf(project)).visit(project);
		}
	}

	protected void visitProject(ProjectElement element, Context context) {
		context.dir("src/main").visit(element.getMainElement());
		context.dir("src/test").visit(element.getTestElement());
	}

	protected void visitNative(NativeElement element, Context context) {
		if (element instanceof NativeLibraryElement) {
			context.dir("public").visit(((NativeLibraryElement) element).getPublicHeaders());
			context.dir("headers").visit(((NativeLibraryElement) element).getPrivateHeaders());
		} else {
			context.dir("headers").visit(element.getHeaders());
		}

		context.visit(element.getSources());
	}

	protected void visitSource(SourceElement element, Context context) {
		if (!element.getFiles().isEmpty()) {
			// TODO: Use UTI instead
			SourceFile source = element.getFiles().get(0);
			if (source.getName().endsWith(".c")) {
				context = context.dir("c");
			} else if (source.getName().endsWith(".cpp")) {
				context = context.dir("cpp");
			} else if (source.getName().endsWith(".java")) {
				context = context.dir("java");
			}
		}

		for (SourceFile source : element.getFiles()) {
			context.visit(source);
		}
	}
}
