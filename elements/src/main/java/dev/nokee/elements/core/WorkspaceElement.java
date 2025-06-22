package dev.nokee.elements.core;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// TODO: Remove extends from Element
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
