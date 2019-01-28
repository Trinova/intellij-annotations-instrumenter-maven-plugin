/*
 * Copyright 2013-2015 Eris IT AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.eris.functional.test;

import se.eris.util.*;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

// an arbitrary second annotation for testing (not realistic use)
@InstrumentationConfiguration(notNull = {"org.jetbrains.annotations.NotNull", "java.lang.Deprecated"})
@CompiledVersionsTest(sourceClasses = "se.eris.notnull.TestNotNull")
class NotNullAnnotationInstrumenterTest {

    @CompiledVersionsTest
    void annotatedParameter_shouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Method notNullParameterMethod = testClass.getMethod("notNullParameter", String.class);
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, "should work");

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(notNullParameterMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 0%s of %s.notNullParameter must not be null",
                        testCompiler.getParameterName("s"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void notnullReturn_shouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Method notNullReturnMethod = testClass.getMethod("notNullReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "NotNull method %s.notNullReturn must not return null",
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void annotatedReturn_shouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Method notNullReturnMethod = testClass.getMethod("annotatedReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "NotNull method %s.annotatedReturn must not return null",
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void overridingMethod_isInstrumented(final TestCompiler testCompiler, final Class<?> outerClass) throws Exception {
        final TestClass testClass = new TestClass(outerClass.getName());
        final Class<?> subargClass = testCompiler.getCompiledClass(testClass.nested("Subarg"));

        final TestClass subTestClass = testClass.nested("Sub");
        final Class<?> subClass = testCompiler.getCompiledClass(subTestClass);
        final Object subClassInstance = ReflectionUtil.simulateConstructorCall(subClass.getConstructor());
        final String methodName = "overload";
        final Method specializedMethod = subClass.getMethod(methodName, subargClass);

        assertFalse(specializedMethod.isSynthetic());
        assertFalse(specializedMethod.isBridge());

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(subClassInstance, specializedMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 0%s of %s.%s must not be null",
                        testCompiler.getParameterName("s"),
                        subTestClass.getAsmName(),
                        methodName
                ), exception.getMessage()
        );
    }
}
