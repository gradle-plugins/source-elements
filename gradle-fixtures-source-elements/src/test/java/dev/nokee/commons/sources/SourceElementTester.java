package dev.nokee.commons.sources;

import dev.gradleplugins.fixtures.sources.SourceElement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public interface SourceElementTester {
	SourceElement subject();

	@Test
	default void doesNotChangeOriginalSourceSetName() {
		SourceElement newElement = subject().withSourceSetName("other");
		assertThat(subject().getSourceSetName(), not(equalTo("other")));
		assertThat(newElement.getSourceSetName(), equalTo("other"));
	}
}
