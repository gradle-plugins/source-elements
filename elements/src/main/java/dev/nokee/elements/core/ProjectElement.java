package dev.nokee.elements.core;

import java.util.UUID;

// TODO: Remove extends from Element
public abstract class ProjectElement extends Element {
	private final Object identifier;

	protected ProjectElement() {
		this(UUID.randomUUID());
	}

	protected ProjectElement(Object identifier) {
		this.identifier = identifier;
	}

	public final Object id() {
		return identifier.toString();
	}

	public abstract Element getMainElement();

	public Element getTestElement() {
		return SourceElement.empty();
	}

	public static ProjectElement ofMain(Element mainElement) {
		return new ProjectElement() {
			@Override
			public Element getMainElement() {
				return mainElement;
			}
		};
	}

	public ProjectElement withTest(Element element) {
		return new ProjectElement(identifier) {
			@Override
			public Element getMainElement() {
				return ProjectElement.this.getMainElement();
			}

			@Override
			public Element getTestElement() {
				return element;
			}
		};
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
