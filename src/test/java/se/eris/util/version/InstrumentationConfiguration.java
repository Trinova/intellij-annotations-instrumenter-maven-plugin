package se.eris.util.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface InstrumentationConfiguration {
    /**
     * @return true iff implicit instrumentation is activated
     */
    boolean implicit() default false;

    /**
     * @return fully qualified class names for not null annotations
     */
    String[] notNull() default {};

    /**
     * @return fully qualified class names for nullable annotations
     */
    String[] nullable() default {};

    /**
     * @return matcher patters for files which are excluded if implicit is true
     */
    String[] excludes() default {};
}
