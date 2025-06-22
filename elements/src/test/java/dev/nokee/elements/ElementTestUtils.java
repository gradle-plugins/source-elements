package dev.nokee.elements;

import dev.nokee.elements.core.Element;

import java.util.ArrayList;
import java.util.List;

public class ElementTestUtils {
	public static List<Element> visited(Element subject) {
		List<Element> result = new ArrayList<>();
		subject.accept(new Element.Visitor() {
			@Override
			public void visit(Element element) {
				result.add(element);
			}
		});
		return result;
	}
}
