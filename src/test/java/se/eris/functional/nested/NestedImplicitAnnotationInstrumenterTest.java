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
package se.eris.functional.nested;

import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;
import se.eris.util.version.CompiledVersionsTest;
import se.eris.util.version.InstrumentationConfiguration;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NestedImplicitAnnotationInstrumenterTest {

    @InstrumentationConfiguration(implicit = true)
    @CompiledVersionsTest(sourceClasses = "se.eris.nested.TestNestedImplicitAnnotation")
    void anonymousClassConstructor_shouldFollowParentAnnotation(final TestCompiler testCompiler, final Class<?> outerClass) throws Exception {
        final Method anonymousClassNullable = outerClass.getMethod("anonymousClassNullable");
        ReflectionUtil.simulateMethodCall(anonymousClassNullable);

        final Method anonymousClassNotNull = outerClass.getMethod("anonymousClassNotNull");
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(anonymousClassNotNull)
        );
        assertEquals(
                String.format(
                        "Implicit NotNull argument 0%s of %s$Foo.<init> must not be null",
                        testCompiler.getParameterName("i"),
                        new TestClass(outerClass.getName()).getAsmName()
                ), exception.getMessage())
        ;
    }

}