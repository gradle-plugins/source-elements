package dev.nokee.elements.nativebase;

import dev.nokee.elements.core.Element;
import dev.nokee.elements.core.SourceElement;

import static dev.nokee.elements.core.SourceElement.empty;

public abstract class NativeElement extends Element {
	public abstract SourceElement getHeaders();

	/**
	 * Discards all headers from this native element.
	 *
	 * @return a new native element without headers.
	 */
	public abstract NativeElement withoutHeaders();

	public abstract SourceElement getSources();

	/**
	 * Discards all sources from this native element.
	 *
	 * @return a new native element without sources.
	 */
	public NativeElement withoutSources() {
		return withSources(empty());
	}

	/**
	 * Replaces all sources from this native element.
	 *
	 * @param sources  the new sources for this element.
	 * @return a new native element with new sources.
	 */
	public abstract NativeElement withSources(SourceElement sources);

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
