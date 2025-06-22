package dev.nokee.elements.core;

import dev.nokee.elements.nativebase.NativeLibraryElement;
import dev.nokee.elements.nativebase.NativeSourceElement;

public class GradleLayoutElement extends LayoutElement {
	public GradleLayoutElement(Element element) {
		super(element);
	}

	@Override
	protected void visit(Element e, Context context) {
		e.accept(element -> {
			if (element instanceof ProjectElement) {
				visitProject((ProjectElement) element, context);
			} else if (element instanceof NativeSourceElement) {
				visitNativeSource((NativeSourceElement) element, context);
			} else if (element instanceof SourceElement) {
				visitSource((SourceElement) element, context);
			}
		});
	}

	protected void visitProject(ProjectElement element, Context context) {
		visit(element.getMainElement(), context.dir("src/main"));
		visit(element.getTestElement(), context.dir("src/test"));
	}

	protected void visitNativeSource(NativeSourceElement element, Context context) {
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
