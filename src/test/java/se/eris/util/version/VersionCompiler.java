package se.eris.util.version;

import com.intellij.NotNullInstrumenter;
import org.jetbrains.annotations.NotNull;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.Configuration;
import se.eris.util.TestCompiler;
import se.eris.util.TestCompilerOptions;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionCompiler {

    @NotNull
    static Map<String, TestCompiler> compile(
            final Path destinationBasedir,
            final CompiledVersionsTest.Version[] javaVersions,
            final Configuration configuration,
            final File... javaFiles
    ) {
        final Map<String, TestCompiler> compilers = new HashMap<>();
        for (final CompiledVersionsTest.Version versionEnum : javaVersions) {
            if (versionEnum == CompiledVersionsTest.Version.DEFAULT) {
                continue;
            }
            String versionString = versionEnum.getVersionString();
            final Path destination = destinationBasedir.resolve(versionString);
            final TestCompiler compiler = TestCompiler.create(TestCompilerOptions.from(destination, versionString));
            if (!compiler.compile(javaFiles)) {
                throw new RuntimeException("Compilation failed for version " + versionString);
            }
            compilers.put(versionString, compiler);

            final NotNullInstrumenter instrumenter = new NotNullInstrumenter(new NopLogWrapper());
            final int numberOfInstrumentedFiles = instrumenter.addNotNullAnnotations(destination, configuration, Collections.emptyList());

            assertTrue(numberOfInstrumentedFiles > 0);
        }
        return compilers;
    }
}
