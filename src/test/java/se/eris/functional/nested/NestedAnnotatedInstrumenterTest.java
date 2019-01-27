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

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import se.eris.asm.AsmUtils;
import se.eris.util.CompiledVersionsTest;
import se.eris.util.ReflectionUtil;
import se.eris.util.TestClass;
import se.eris.util.TestCompiler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CompiledVersionsTest(sourceClasses = "se.eris.nested.TestNestedAnnotated")
class NestedAnnotatedInstrumenterTest {

    @CompiledVersionsTest
    void syntheticMethod_dispatchesToSpecializedMethod(final TestCompiler testCompiler, final Class<?> outerClass) throws Exception {
        final TestClass testClass = new TestClass(outerClass.getName());
        final Class<?> superargClass = testCompiler.getCompiledClass(testClass.nested("Superarg").getName());
        final TestClass subTestClass = testClass.nested("Sub");
        final Class<?> subClass = testCompiler.getCompiledClass(subTestClass.getName());
        final Method generalMethod = subClass.getMethod("overload", superargClass);

        assertTrue(generalMethod.isSynthetic());
        assertTrue(generalMethod.isBridge());

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionUtil.simulateMethodCall(subClass.getDeclaredConstructor().newInstance(), generalMethod, new Object[]{null})
        );
        assertEquals(
                String.format(
                        "NotNull annotated argument 0%s of %s.overload must not be null",
                        testCompiler.getParameterName("s"),
                        subTestClass.getAsmName()
                ), exception.getMessage()
        );
    }

    @CompiledVersionsTest
    void onlySpecificMethod_isInstrumented(final TestCompiler testCompiler, final Class<?> outerClass) throws Exception {
        final TestClass testClass = new TestClass(outerClass.getName());
        // Check that only the specific method has a string annotation indicating instrumentation
        final TestClass sub = testClass.nested("Sub");
        final ClassReader classReader = sub.getClassReader(testCompiler.getOptions().getDestination().toFile());
        final List<String> strings = getStringConstants(classReader, "overload");
        final String onlyExpectedString = String.format(
                "(L%s;)V:NotNull annotated argument 0%s of %s.overload must not be null",
                testClass.nested("Subarg").getAsmName(),
                testCompiler.getParameterName("s"),
                sub.getAsmName()
        );
        assertEquals(Collections.singletonList(onlyExpectedString), strings);
    }

    @CompiledVersionsTest
    void nestedClassesSegmentIsPreserved(final TestCompiler testCompiler, final Class<?> outerClass) throws Exception {
        final TestClass testClass = new TestClass(outerClass.getName());
        // Check that only the specific method has a string annotation indicating instrumentation
        final TestClass preserved = testClass.nested("NestedClassesSegmentIsPreserved");
        final ClassReader classReader = preserved.getClassReader(testCompiler.getOptions().getDestination().toFile());
        final List<AsmInnerClass> asmInnerClasses = getAsmInnerClasses(classReader);
        assertEquals(2, asmInnerClasses.size());
        //self-entry
        assertEquals(preserved.getAsmName(), asmInnerClasses.get(0).name);
        //inner entry
        final AsmInnerClass expected = new AsmInnerClass(preserved.nested("ASub").getAsmName(),
                preserved.getAsmName(), "ASub", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
        assertEquals(expected, asmInnerClasses.get(1));
    }

    private List<AsmInnerClass> getAsmInnerClasses(final ClassReader cr) {
        final List<AsmInnerClass> asmInnerClasses = new ArrayList<>();
        cr.accept(new ClassVisitor(AsmUtils.ASM_OPCODES_VERSION) {
            @Override
            public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
                asmInnerClasses.add(new AsmInnerClass(name, outerName, innerName, access));
            }
        }, 0);
        return asmInnerClasses;
    }

    @NotNull
    private List<String> getStringConstants(final ClassReader cr, final String methodName) {
        final List<String> strings = new ArrayList<>();
        cr.accept(new ClassVisitor(AsmUtils.ASM_OPCODES_VERSION) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                                             final String[] exceptions) {
                if (name.equals(methodName)) {
                    return new MethodVisitor(AsmUtils.ASM_OPCODES_VERSION) {
                        @Override
                        public void visitLdcInsn(final Object cst) {
                            if (cst instanceof String) {
                                strings.add(desc + ":" + cst);
                            }
                        }
                    };
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);
        return strings;
    }

}