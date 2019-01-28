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
package se.eris.functional.exclude;

import se.eris.util.*;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.eris.util.CompiledVersionsTest.InjectCompiler.NO;

/**
 * Tests to verify that package exclusion works.
 */
@InstrumentationConfiguration(implicit = true, excludes = "se.eris.exclude.*")
@CompiledVersionsTest(sourceClasses = "se.eris.exclude.TestExclude")
class ExcludeImplicitClassesTest {


    @CompiledVersionsTest
    void annotatedParameter_shouldValidate(final TestCompiler testCompiler, Class<?> testClass) throws Exception {
        final String methodName = "notNullParameter";
        final Method notNullParameterMethod = testClass.getMethod(methodName, String.class);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ReflectionUtil.simulateMethodCall(notNullParameterMethod, (Object) null));
        final String expected = String.format(
                "NotNull annotated argument 0%s of %s.%s must not be null",
                testCompiler.getParameterName("s"),
                new TestClass(testClass.getName()).getAsmName(),
                methodName
        );
        assertEquals(expected, exception.getMessage());
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void annotatedReturn_shouldValidate(Class<?> testClass) throws Exception {
        final String methodName = "notNullReturn";
        final Method notNullReturnMethod = testClass.getMethod(methodName, String.class);

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ReflectionUtil.simulateMethodCall(notNullReturnMethod, new Object[]{null}));
        final String expected = String.format(
                "NotNull method %s.%s must not return null",
                new TestClass(testClass.getName()).getAsmName(),
                methodName
        );
        assertEquals(expected, exception.getMessage());
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void notAnnotatedParameter_shouldNotValidate(Class<?> c) throws Exception {
        final Method notNullParameterMethod = c.getMethod("unAnnotatedParameter", String.class);

        ReflectionUtil.simulateMethodCall(notNullParameterMethod, (Object) null);
    }

    @CompiledVersionsTest(injectCompiler = NO)
    void notAnnotatedReturn_shouldNotValidate(Class<?> testClass) throws Exception {
        final Method notNullParameterMethod = testClass.getMethod("unAnnotatedReturn", String.class);

        ReflectionUtil.simulateMethodCall(notNullParameterMethod, (Object) null);
    }

}