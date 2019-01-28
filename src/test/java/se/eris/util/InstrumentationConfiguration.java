package se.eris.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface InstrumentationConfiguration {
    boolean implicit() default false;

    String[] notNull() default {};

    String[] nullable() default {};

    String[] classMatcher() default {};
}
