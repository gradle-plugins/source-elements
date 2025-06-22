package dev.nokee.elements;

import dev.nokee.elements.core.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.commons.hamcrest.gradle.NamedMatcher.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ProjectElementTests {
	private interface Tester {
		ProjectElement subject();

		@Test
		default void doesNotChangeIdentifierOnTestElementReplacement() {
			ProjectElement newSubject = subject().withTest(SourceFileElement.ofFile(SourceFile.of("bar_test.cpp", "...")));
			assertThat("does not change identifier", newSubject.id(), equalTo(subject().id()));
			assertThat("use same main element", newSubject.getMainElement(), is(subject().getMainElement()));
			assertThat(((SourceElement) newSubject.getTestElement()).getFiles(), contains(named("bar_test.cpp")));

		}

		@Test
		default void canReplaceMainElement() {
			ProjectElement newSubject = subject().withMain(SourceFileElement.ofFile(SourceFile.of("bar.cpp", "...")));
			assertThat("does not change identifier", newSubject.id(), equalTo(subject().id()));
			assertThat(((SourceElement) newSubject.getMainElement()).getFiles(), contains(named("bar.cpp")));
			assertThat("use same test element", newSubject.getTestElement(), is(subject().getTestElement()));
		}
	}

	@Nested
	class CustomProjectElement implements Tester {
		ProjectElement subject = new ProjectElement() {
			@Override
			public Element getMainElement() {
				return SourceFileElement.ofFile(sourceFile("foo.cpp", "..."));
			}

			@Override
			public Element getTestElement() {
				return SourceFileElement.ofFile(sourceFile("foo_test.cpp", "..."));
			}
		};

		@Override
		public ProjectElement subject() {
			return subject;
		}
	}


	@Nested
	class WithDefaultTest implements Tester {
		ProjectElement subject = new ProjectElement() {
			@Override
			public Element getMainElement() {
				return SourceFileElement.ofFile(sourceFile("foo.cpp", "..."));
			}
		};

		@Override
		public ProjectElement subject() {
			return subject;
		}

		@Test
		void defaultToEmptyTestElement() {
			assertThat(subject.getTestElement(), equalTo(SourceElement.empty()));
		}
	}

	@Nested
	class OfMainFactory implements Tester {
		ProjectElement subject = ProjectElement.ofMain(SourceFileElement.ofFile(SourceFile.of("foo.cpp", "...")));

		@Override
		public ProjectElement subject() {
			return subject;
		}

		@Test
		void useSpecifiedElementAsMainElement() {
			assertThat(subject.getMainElement(), isA(SourceElement.class));
			assertThat(((SourceElement) subject.getMainElement()).getFiles(), contains(named("foo.cpp")));
		}

		@Test
		void defaultToEmptyTestElement() {
			assertThat(subject.getTestElement(), equalTo(SourceElement.empty()));
		}
	}

	@Nested
	class OfTestFactory implements Tester {
		ProjectElement subject = ProjectElement.ofTest(SourceFileElement.ofFile(SourceFile.of("foo_test.cpp", "...")));

		@Override
		public ProjectElement subject() {
			return subject;
		}

		@Test
		void useSpecifiedElementAsTestElement() {
			assertThat(subject.getTestElement(), isA(SourceElement.class));
			assertThat(((SourceElement) subject.getTestElement()).getFiles(), contains(named("foo_test.cpp")));
		}

		@Test
		void defaultToEmptyMainElement() {
			assertThat(subject.getMainElement(), equalTo(SourceElement.empty()));
		}
	}
}
