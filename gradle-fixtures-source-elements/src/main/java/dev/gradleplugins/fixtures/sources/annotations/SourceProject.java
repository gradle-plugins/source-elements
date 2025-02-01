package dev.gradleplugins.fixtures.sources.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SourceProject {
	String value();
	SourceFileProperty[] properties() default {};
	String[] includes() default {};
	String[] excludes() default {};
}
