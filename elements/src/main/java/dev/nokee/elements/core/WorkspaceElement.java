package dev.nokee.elements.core;

import java.util.List;
import java.util.UUID;

public abstract class WorkspaceElement extends Element {
	private final Object identifier;

	protected WorkspaceElement() {
		this(UUID.randomUUID());
	}

	protected WorkspaceElement(Object identifier) {
		this.identifier = identifier;
	}

	public abstract List<ProjectElement> getProjects();

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
