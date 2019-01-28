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
package se.eris.functional.inner;

import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.version.CompiledVersionsTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CompiledVersionsTest(sourceClasses = "se.eris.inner.TestInner")
class InnerClassInstrumenterTest {

    @CompiledVersionsTest
    void innerClassConstructorShouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Constructor<?> constructor = testClass.getConstructor(String.class, Integer.class, Integer.class);

        ReflectionUtil.simulateConstructorCall(constructor, "A String", 17, 18);
        ReflectionUtil.simulateConstructorCall(constructor, null, 17, 18);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateConstructorCall(constructor, new Object[]{null, null, 18})
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 1%s of %s$InnerClass.<init> must not be null",
                        testCompiler.getParameterName("notNull"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void innerClassMethodShouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Constructor<?> constructor = testClass.getConstructor(String.class, Integer.class, Integer.class);

        final Object outer = ReflectionUtil.simulateConstructorCall(constructor, "A String", 17, 18);

        final Method getInner = outer.getClass().getDeclaredMethod("getInner");
        final Object innerClass = ReflectionUtil.simulateMethodCall(outer, getInner);

        final Method innerClassMethod = innerClass.getClass().getDeclaredMethod("innerMethod", String.class, Integer.class);
        ReflectionUtil.simulateMethodCall(innerClass, innerClassMethod, null, 12);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(innerClass, innerClassMethod, null, null)
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 1%s of %s$InnerClass.innerMethod must not be null",
                        testCompiler.getParameterName("innerNotNull"),
                        new TestClass(testClass.getName()).getAsmName()),
                exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void nestedClassConstructorShouldValidate(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Constructor<?> constructor = testClass.getConstructor(String.class, Integer.class, Integer.class);

        ReflectionUtil.simulateConstructorCall(constructor, "A String", 17, 18);
        ReflectionUtil.simulateConstructorCall(constructor, null, 17, 18);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateConstructorCall(constructor, new Object[]{null, 17, null})
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 1%s of %s$NestedClass.<init> must not be null",
                        testCompiler.getParameterName("notNull"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

}