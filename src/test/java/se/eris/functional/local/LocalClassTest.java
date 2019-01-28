package se.eris.functional.local;

import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.version.CompiledVersionsTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CompiledVersionsTest(sourceClasses = {"se.eris.local.TestLocal", "se.eris.local.TestLocal$1LocalClass"})
class LocalClassTest {

    private static final String METHOD_NAME = "localMethod";

    @CompiledVersionsTest
    void localClassConstructorShouldValidate(final TestCompiler testCompiler, final Class<?> outerClass, final Class<?> localClass) throws Exception {
        final Object outerInstance = ReflectionUtil.simulateConstructorCall(outerClass.getConstructor());
        final Constructor<?> localConstructor = localClass.getConstructor(outerClass, String.class, String.class);

        localConstructor.setAccessible(true);

        //first parameter for inner class is auto generated and
        ReflectionUtil.simulateConstructorCall(localConstructor, outerInstance, "A String", "Another String");
        ReflectionUtil.simulateConstructorCall(localConstructor, null, "A String", "Another String");
        ReflectionUtil.simulateConstructorCall(localConstructor, outerInstance, null, "Another String");
        ReflectionUtil.simulateConstructorCall(localConstructor, null, null, "Another String");

        final IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateConstructorCall(localConstructor, outerInstance, null, null)
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 1%s of %s.<init> must not be null",
                        testCompiler.getParameterName("notNull"),
                        new TestClass(localClass.getName()).getAsmName()
                ), exception1.getMessage()
        );
    }

    @CompiledVersionsTest
    void localClassMethodShouldValidate(final TestCompiler testCompiler, final Class<?> outerClass, final Class<?> localClass) throws Exception {
        final Object outerInstance = ReflectionUtil.simulateConstructorCall(outerClass.getConstructor());
        final Constructor<?> localConstructor = localClass.getConstructor(outerClass, String.class, String.class);

        localConstructor.setAccessible(true);

        final Method localMethod = localClass.getMethod(METHOD_NAME, String.class, String.class);
        localMethod.setAccessible(true);

        final Object innerInstance = ReflectionUtil.simulateConstructorCall(localConstructor, outerInstance, "A String", "Another String");

        ReflectionUtil.simulateMethodCall(innerInstance, localMethod, "A String", "Another String");
        ReflectionUtil.simulateMethodCall(innerInstance, localMethod, null, "Another String");

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(innerInstance, localMethod, "A String", null)
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 1%s of %s.%s must not be null",
                        testCompiler.getParameterName("notNull"),
                        new TestClass(localClass.getName()).getAsmName(),
                        METHOD_NAME
                ), exception.getMessage()
        );
    }
}
