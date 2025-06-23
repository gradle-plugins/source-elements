package dev.nokee.elements;

import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;
import dev.nokee.elements.core.SourceFileElement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.nokee.elements.ElementTestUtils.visited;
import static dev.nokee.elements.core.SourceElement.ofElements;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class CompositeSourceElementTests {
	@Test
	void visitDeeplyNestedElements() {
		SourceElement first = new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				return Collections.singletonList(sourceFile("foo.cpp", "..."));
			}
		};
		SourceElement second = SourceElement.empty();
		SourceElement third = new SourceFileElement() {
			@Override
			public SourceFile getSourceFile() {
				return sourceFile("bar.cpp", "...");
			}
		};
		SourceElement fourth = SourceFileElement.ofFile(SourceFile.of("far.cpp", "..."));
		SourceElement fifth = SourceElement.ofFiles(Collections.singletonList(SourceFile.of("tar.cpp", "...")));


		assertThat(visited(ofElements(first, ofElements(second, ofElements(third), ofElements(fourth, fifth)))), contains(first, second, third, fourth, fifth));
	}
}
