package se.eris.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import se.eris.util.CompiledVersionsTest.Version;
import se.eris.util.version.VersionCompiler;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class TestCompilerResolver implements ArgumentsProvider, ParameterResolver {

    private static final String DEFAULT_SOURCE_DIRECTORY = "src/test/data";
    private static final String DEFAULT_TARGET_DIRECTORY = "target/test/data/classes";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        final TestSettings testSettings = new TestSettings();
        //overwrite default settings with explicitly set values on class
        extensionContext.getTestClass().map(c -> c.getAnnotation(CompiledVersionsTest.class)).ifPresent(testSettings::overwrite);
        //overwrite settings with explicitly set values on method
        extensionContext.getTestMethod().map(m -> m.getAnnotation(CompiledVersionsTest.class)).ifPresent(testSettings::overwrite);

        if (testSettings.classes.length == 0) {
            throw new IllegalArgumentException("At least one class to compile must be provided.");
        }

        Path targetDirectory = new File(testSettings.targetDirString).toPath();

        //map file name strings of non nested classes (without `$` in the name) to java.io.File
        final File[] sourceFiles = Arrays.stream(testSettings.classes)
                .filter(className -> !className.contains("$"))
                .map(TestClass::new)
                .map(tc -> tc.getJavaFile(new File(testSettings.sourceDirString)))
                .toArray(File[]::new);


        //filter all versions before CompiledVersionsTest.since()
        final Version[] versions = Arrays.stream(Version.values())
                .filter(v -> v.ordinal() >= testSettings.sinceVersion.ordinal())
                .toArray(Version[]::new);

        final Map<String, TestCompiler> compilers = VersionCompiler.compile(targetDirectory, versions, sourceFiles);

        //map TestCompilers to junit Argument stream
        return Arrays.stream(versions)
                .map(Version::getVersionString)
                .map(version -> getParameterSet(version, compilers.get(version), testSettings.classes));
    }

    /**
     * @param version target and source java version
     * @param testCompiler the test compiler instance
     * @param classes sourceClasses to provide as arguments
     * @return [TestCompiler testCompiler, Class... sourceClasses, ]
     */
    private Arguments getParameterSet(String version, TestCompiler testCompiler, String[] classes) {
        Object[] arguments = new Object[classes.length + 1];
        arguments[0] = testCompiler;
        for (int i = 1; i < arguments.length; i++) {
            try {
                arguments[i] = testCompiler.getCompiledClass(classes[i - 1]);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format(
                        "Failed to retrieve java version %s compiled class %s",
                        version,
                        classes[i]
                ));
            }
        }

        return Arguments.of(arguments);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return null;
    }

    private static class TestSettings {
        Version sinceVersion = Version.JAVA7;
        String sourceDirString = DEFAULT_SOURCE_DIRECTORY;
        String targetDirString = DEFAULT_TARGET_DIRECTORY;
        String[] classes = CompiledVersionsTest.NO_CLASSES;

        void overwrite(CompiledVersionsTest compiledVersionsTest) {
            if (compiledVersionsTest.since() != CompiledVersionsTest.NO_VERSION) {
                sinceVersion = compiledVersionsTest.since();
            }
            if (!CompiledVersionsTest.NO_DIRECTORY.equals(compiledVersionsTest.sourceDir())) {
                sourceDirString = compiledVersionsTest.sourceDir();
            }
            if (!CompiledVersionsTest.NO_DIRECTORY.equals(compiledVersionsTest.targetDir())) {
                targetDirString = compiledVersionsTest.targetDir();
            }
            if (compiledVersionsTest.sourceClasses().length > 0) {
                classes = compiledVersionsTest.sourceClasses();
            }
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", TestSettings.class.getSimpleName() + "[", "]")
                    .add("sinceVersion=" + sinceVersion)
                    .add("sourceDirString='" + sourceDirString + "'")
                    .add("targetDirString='" + targetDirString + "'")
                    .add("classes=" + Arrays.toString(classes))
                    .toString();
        }
    }
}