/*
 * Copyright 2013-2016 Eris IT AB
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
package se.eris.functional.implicit;

import se.eris.util.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static se.eris.util.CompiledVersionsTest.InjectCompiler.NO;


@InstrumentationConfiguration(implicit = true)
@CompiledVersionsTest(sourceClasses = "se.eris.implicit.TestImplicitConfiguration")
class ImplicitConfigurationInstrumenterTest {

    @CompiledVersionsTest
    void notNullAnnotatedParameter_shouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Method notNullParameterMethod = testClass.getMethod("notNullParameter", String.class);
        ReflectionUtil.simulateMethodCall(notNullParameterMethod, "should work");

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(notNullParameterMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "Implicit NotNull argument 0%s of %s.notNullParameter must not be null",
                        testCompiler.getParameterName("s"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void implicitParameter_shouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Method implicitParameterMethod = testClass.getMethod("implicitParameter", String.class);
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "Implicit NotNull argument 0%s of %s.implicitParameter must not be null",
                        testCompiler.getParameterName("s"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void nullableAnnotatedParameter_shouldNotValidate(final Class<?> testClass) throws Exception {
        final Method implicitParameterMethod = testClass.getMethod("nullableParameter", String.class);
        ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null});
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void implicitReturn_shouldValidate(final Class<?> testClass) throws Exception {
        final String methodName = "implicitReturn";
        final Method notNullReturnMethod = testClass.getMethod(methodName, String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, "should work");

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "NotNull method %s.%s must not return null",
                        new TestClass(testClass.getName()).getAsmName(),
                        methodName
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void annotatedReturn_shouldNotValidate(final Class<?> testClass) throws Exception {
        final Method notNullReturnMethod = testClass.getMethod("annotatedReturn", String.class);
        ReflectionUtil.simulateMethodCall(notNullReturnMethod, (String) null);
    }

    @CompiledVersionsTest
    void nestedClassWithoutConstructor_shouldWork(final TestCompiler testCompiler, final Class<?> outerClass) throws Exception {
        boolean noArgConstructorFound = false;
        final TestClass outerTestClass = new TestClass(outerClass.getName());
        final Class<?> nestedClass = testCompiler.getCompiledClass(outerTestClass.nested("Nested"));
        for (final Constructor<?> constructor : nestedClass.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                noArgConstructorFound = true;
                constructor.setAccessible(true);
                constructor.newInstance();
            } else if (constructor.isSynthetic()) {
                constructor.setAccessible(true);
                constructor.newInstance(constructor.getGenericParameterTypes()[0].getClass().cast(null));
            } else {
                throw new RuntimeException("There should be no constructors with these properties (only no-arg snd synthetic): " + constructor);
            }
        }
        assertTrue(noArgConstructorFound);
    }

}