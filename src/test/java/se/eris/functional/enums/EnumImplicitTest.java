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
package se.eris.functional.enums;

import se.eris.util.ReflectionUtil;
import se.eris.util.version.CompiledVersionsTest;
import se.eris.util.version.InstrumentationConfiguration;

import java.lang.reflect.Method;

import static se.eris.util.version.CompiledVersionsTest.InjectCompiler.NO;

class EnumImplicitTest {

    @InstrumentationConfiguration(implicit = true)
    @CompiledVersionsTest(sourceClasses = "se.eris.enums.TestEnum", injectCompiler = NO)
    void enumParametersShouldValidate(final Class<?> testClass) throws Exception {
        final Method valueOf = testClass.getMethod("valueOf", String.class);

        ReflectionUtil.simulateMethodCall(valueOf, "FALSE"); // will initialize class (calling constructors)
    }

}