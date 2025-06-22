package dev.nokee.elements.core;

import java.util.List;
import java.util.UUID;

public abstract class WorkspaceElement {
	private final Object identifier;

	protected WorkspaceElement() {
		this(UUID.randomUUID());
	}

	protected WorkspaceElement(Object identifier) {
		this.identifier = identifier;
	}

	public abstract List<ProjectElement> getProjects();
}
