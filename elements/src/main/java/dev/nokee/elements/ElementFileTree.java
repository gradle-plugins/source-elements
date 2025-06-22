package dev.nokee.elements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ElementFileTree {
	String value();
	String[] includes() default {};
	String[] excludes() default {};
}
