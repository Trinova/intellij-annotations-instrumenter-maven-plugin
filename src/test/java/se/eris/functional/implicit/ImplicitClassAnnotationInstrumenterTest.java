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

import se.eris.util.CompiledVersionsTest;
import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.eris.util.CompiledVersionsTest.InjectCompiler.NO;

@CompiledVersionsTest(sourceClasses = "se.eris.implicit.TestImplicitClassAnnotation")
class ImplicitClassAnnotationInstrumenterTest {

    @CompiledVersionsTest(injectCompiler = NO)
    void notNullAnnotatedParameter_shouldValidate(final Class<?> c) throws Exception {
        final Method implicitReturn = c.getMethod("implicitReturn", String.class);
        ReflectionUtil.simulateMethodCall(implicitReturn, "should work");

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionUtil.simulateMethodCall(implicitReturn, new Object[]{null})
        );
        final String expected = String.format(
                "NotNull method %s.implicitReturn must not return null",
                new TestClass(c.getName()).getAsmName()
        );
        assertEquals(
                expected,
                exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void implicitParameter_shouldValidate(final TestCompiler testCompiler, final Class<?> c) throws Exception {
        final Method implicitParameterMethod = c.getMethod("implicitParameter", String.class);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(implicitParameterMethod, new Object[]{null})
        );
        final String expected = String.format(
                "Implicit NotNull argument 0%s of %s.implicitParameter must not be null",
                testCompiler.getParameterName("s"),
                new TestClass(c.getName()).getAsmName()
        );
        assertEquals(expected, exception.getMessage());
    }

    @CompiledVersionsTest
    void implicitConstructorParameter_shouldValidate(final TestCompiler testCompiler, final Class<?> c) throws Exception {
        final Constructor<?> implicitParameterConstructor = c.getConstructor(String.class);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateConstructorCall(implicitParameterConstructor, new Object[]{null})
        );
        final String expected = String.format(
                "Implicit NotNull argument 0%s of %s.<init> must not be null",
                testCompiler.getParameterName("s"),
                new TestClass(c.getName()).getAsmName()
        );
        assertEquals(expected, exception.getMessage());
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void anonymousClassConstructor_shouldNotBeInstrumented(final Class<?> c) throws Exception {
        final Method anonymousClassNullable = c.getMethod("anonymousClassNullable");
        ReflectionUtil.simulateMethodCall(anonymousClassNullable);

        final Method anonymousClassNotNull = c.getMethod("anonymousClassNotNull");
        ReflectionUtil.simulateMethodCall(anonymousClassNotNull);
    }

}