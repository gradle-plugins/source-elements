package dev.nokee.elements.core;

import java.util.UUID;

// TODO: Remove extends from Element
// TODO: Add the concept of additional source sets (as in src/<name> in Gradle layout)
// TODO: Add the concept of additional source files (as in random source files inside the project)
public abstract class ProjectElement extends Element {
	private final Object identifier;

	protected ProjectElement() {
		this(UUID.randomUUID());
	}

	protected ProjectElement(Object identifier) {
		this.identifier = identifier;
	}

	// Identifier for layout mapping
	public final Object id() {
		return identifier.toString();
	}

	/**
	 * {@return the main element of this project.}
	 */
	public abstract Element getMainElement();

	public final ProjectElement withMain(Element mainElement) {
		return new ProjectElement(identifier) {
			@Override
			public Element getMainElement() {
				return mainElement;
			}

			@Override
			public Element getTestElement() {
				return ProjectElement.this.getTestElement();
			}
		};
	}

	/**
	 * {@return the test element of this project}
	 */
	public Element getTestElement() {
		return SourceElement.empty();
	}

	/**
	 * Creates a project element with only the specified main element.
	 *
	 * @param mainElement  the main element of this project.
	 * @return a project element
	 */
	public static ProjectElement ofMain(Element mainElement) {
		return new ProjectElement() {
			@Override
			public Element getMainElement() {
				return mainElement;
			}
		};
	}

	public static ProjectElement ofTest(Element testElement) {
		return new ProjectElement() {
			@Override
			public Element getMainElement() {
				return SourceElement.empty();
			}

			@Override
			public Element getTestElement() {
				return testElement;
			}
		};
	}

	public final ProjectElement withTest(Element element) {
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
