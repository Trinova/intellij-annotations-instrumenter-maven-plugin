package se.eris.util.version;

import com.intellij.NotNullInstrumenter;
import org.jetbrains.annotations.NotNull;
import se.eris.maven.NopLogWrapper;
import se.eris.notnull.AnnotationConfiguration;
import se.eris.notnull.Configuration;
import se.eris.notnull.ExcludeConfiguration;
import se.eris.util.CompiledVersionsTest;
import se.eris.util.TestCompiler;
import se.eris.util.TestCompilerOptions;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionCompiler {

    @NotNull
    public static Map<String, TestCompiler> compile(final Path destinationBasedir, final File... javaFiles) {
        return compile(destinationBasedir, defaultConfiguration(), javaFiles);
    }

    @NotNull
    public static Map<String, TestCompiler> compile(final Path destinationBasedir, final CompiledVersionsTest.Version[] javaVersions, final File... javaFiles) {
        return compile(destinationBasedir, javaVersions, defaultConfiguration(), javaFiles);
    }

    @NotNull
    public static Map<String, TestCompiler> compile(final Path destinationBasedir, final Configuration configuration, final File... javaFiles) {
        return compile(destinationBasedir, supportedJavaVersions(), configuration, javaFiles);
    }

    @NotNull
    public static Map<String, TestCompiler> compile(final Path destinationBasedir, final CompiledVersionsTest.Version[] javaVersions, final Configuration configuration, final File... javaFiles) {
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

    @NotNull
    private static Configuration defaultConfiguration() {
        return new Configuration(
                false,
                new AnnotationConfiguration(
                        singleton("org.jetbrains.annotations.NotNull"),
                        singleton("org.jetbrains.annotations.Nullable")
                ), new ExcludeConfiguration(Collections.emptySet())
        );
    }

    @NotNull
    private static CompiledVersionsTest.Version[] supportedJavaVersions(){
        return CompiledVersionsTest.Version.values();
    }

    /**
     * @return single-quoted parameter name if compilers supports `-parameters` option, empty string otherwise.
     */
    @NotNull
    @Deprecated
    public static String maybeName(final TestCompiler testCompiler, @NotNull final String parameterName) {
        return testCompiler.hasParametersSupport() ? String.format(" (parameter '%s')", parameterName) : "";
    }

}
