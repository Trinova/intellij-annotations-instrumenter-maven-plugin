package se.eris.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to provide a test configuration for a parameterized tests.
 * Can be used to annotate the test class or a test method.
 * Values annotated on the method override values from the class annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ParameterizedTest(name = "{0}")
@ArgumentsSource(TestCompilerResolver.class)
public @interface CompiledVersionsTest {
    Version NO_VERSION = Version.DEFAULT;
    String NO_DIRECTORY = "";
    String[] NO_CLASSES = {};

    /**
     * @return the lowest supported version applicable for the test
     */
    Version since() default Version.DEFAULT;

    /**
     * @return relative source directory where .java files are loaded from
     */
    String sourceDir() default NO_DIRECTORY;

    /**
     * @return relative target directory where .class files are written to
     */
    String targetDir() default NO_DIRECTORY;

    /**
     * These class are provided for the test as parameters in the same order in which they are annotated.
     *
     * @return fully qualified names of sourceClasses which should be compiled and injected into the test
     */
    String[] sourceClasses() default {};

    @SuppressWarnings("unused - instances are used in iterations of Version.values()")
    enum Version {
        DEFAULT("DEFAULT"),
        JAVA7("1.7"),
        JAVA8("1.8"),
        JAVA9("1.9"),
        JAVA10("10"),
        JAVA11("11");

        private String versionString;

        Version(String versionString) {
            this.versionString = versionString;
        }

        public String getVersionString() {
            return versionString;
        }
    }
}
