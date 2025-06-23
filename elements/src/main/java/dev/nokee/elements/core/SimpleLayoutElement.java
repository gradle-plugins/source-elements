package dev.nokee.elements.core;

import dev.nokee.elements.nativebase.NativeElement;

// TODO: Not sure if this should be folded into LayoutElement directly
public abstract class SimpleLayoutElement extends LayoutElement {
	@Override
	protected final void visit(Element e, Context context) {
		e.accept(element -> {
			if (element instanceof WorkspaceElement) {
				visitWorkspace((WorkspaceElement) element, context);
			} else if (element instanceof ProjectElement) {
				visitProject((ProjectElement) element, context);
			} else if (element instanceof NativeElement) {
				visitNative((NativeElement) element, context);
			} else if (element instanceof SourceElement) {
				visitSource((SourceElement) element, context);
			}
		});
	}

	protected abstract void visitWorkspace(WorkspaceElement element, Context context);

	protected abstract void visitProject(ProjectElement element, Context context);

	protected abstract void visitNative(NativeElement element, Context context);

	protected abstract void visitSource(SourceElement element, Context context);
}
