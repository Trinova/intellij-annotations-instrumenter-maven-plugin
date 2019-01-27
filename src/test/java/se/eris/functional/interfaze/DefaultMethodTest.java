package se.eris.functional.interfaze;

import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.VersionTest;
import se.eris.util.version.VersionCompiler;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.eris.util.VersionTest.Version.JAVA8;

@VersionTest(since = JAVA8, classes = "se.eris.interfaze.TestInterfaceWithDefaultMethod")
class DefaultMethodTest {

    private static final String METHOD_NAME_LAMBDA = "testDefaultMethodViaLambda";
    private static final String METHOD_NAME_ANONYMOUS = "testDefaultMethodViaAnonymousClass";

    private static final String NOT_NULL_PARAMETER_METHOD_NAME = "annotatedParameterDefaultMethod";
    private static final String NOT_NULL_RETURN_METHOD_NAME = "annotatedReturnDefaultMethod";

    @VersionTest
    void inheritedDefaultMethodShouldValidate_ReturnValue(Class<?> outerClass, TestCompiler compiler) throws Exception {
        final Object testInterfaceFromLambda = ReflectionUtil.simulateMethodCall(outerClass.getMethod(METHOD_NAME_LAMBDA));
        final Object testInterfaceFromAnonymous = ReflectionUtil.simulateMethodCall(outerClass.getMethod(METHOD_NAME_ANONYMOUS));

        for (Object testInstance : Arrays.asList(testInterfaceFromLambda, testInterfaceFromAnonymous)) {
            final Class<?> implementationClass = testInstance.getClass();
            final Method annotatedParameterDefaultMethod = implementationClass.getMethod(NOT_NULL_PARAMETER_METHOD_NAME, String.class);

            ReflectionUtil.simulateMethodCall(testInstance, annotatedParameterDefaultMethod, "Some String value.");

            final IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> ReflectionUtil.simulateMethodCall(testInstance, annotatedParameterDefaultMethod, (Object) null)
            );

            assertEquals(
                    String.format(
                            "NotNull annotated argument 0%s of %s.%s must not be null",
                            VersionCompiler.maybeName(compiler, "notNull"),
                            new TestClass(outerClass.getCanonicalName()).nested("TestInterface").getAsmName(),
                            annotatedParameterDefaultMethod.getName()
                    ), exception.getMessage()
            );
        }


    }

    @VersionTest
	void inheritedDefaultMethodShouldValidate_ReturnValue(Class<?> outerClass) throws Exception {
        final Object testInterfaceFromLambda = ReflectionUtil.simulateMethodCall(outerClass.getMethod(METHOD_NAME_LAMBDA));
        final Object testInterfaceFromAnonymous = ReflectionUtil.simulateMethodCall(outerClass.getMethod(METHOD_NAME_ANONYMOUS));
        for (Object testInstance : Arrays.asList(testInterfaceFromLambda, testInterfaceFromAnonymous)) {

            final Class<?> implementationClass = testInstance.getClass();
            final Method annotatedReturnDefaultMethod = implementationClass.getMethod(NOT_NULL_RETURN_METHOD_NAME, String.class);

            ReflectionUtil.simulateMethodCall(testInstance, annotatedReturnDefaultMethod, "Some String value.");

            final IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> ReflectionUtil.simulateMethodCall(testInstance, annotatedReturnDefaultMethod, (Object) null)
            );

            assertEquals(
                    String.format(
                            "NotNull method %s.%s must not return null",
                            new TestClass(outerClass.getCanonicalName()).nested("TestInterface").getAsmName(),
                            annotatedReturnDefaultMethod.getName()
                    ), exception.getMessage()
            );
        }
	}
}
