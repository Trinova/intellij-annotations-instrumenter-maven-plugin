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
package se.eris.types;

import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.version.CompiledVersionsTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CompiledVersionsTest(sourceClasses = "se.eris.types.TestTypes")
class ParameterTypesTest {

    @CompiledVersionsTest
    void objectArgument(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Constructor<?> constructor = testClass.getConstructor();

        final Object instance = ReflectionUtil.simulateConstructorCall(constructor);

        final Method paramObject = instance.getClass().getDeclaredMethod("paramObject", String.class);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(instance, paramObject, (Object) null)
        );
        assertEquals(
                String.format("NotNull annotated argument 0%s of %s.paramObject must not be null",
                        testCompiler.getParameterName("string"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void arrayArgument(final TestCompiler testCompiler, final Class<?> testClass) throws Exception {
        final Constructor<?> constructor = testClass.getConstructor();

        final Object instance = ReflectionUtil.simulateConstructorCall(constructor);

        final Method paramObject = instance.getClass().getDeclaredMethod("paramArray", int[].class);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(instance, paramObject, (Object) null)
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 0%s of %s.paramArray must not be null",
                        testCompiler.getParameterName("array"),
                        new TestClass(testClass.getName()).getAsmName()
                ), exception.getMessage()
        );
    }
}
