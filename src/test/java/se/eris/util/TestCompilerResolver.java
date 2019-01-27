package se.eris.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import se.eris.util.VersionTest.Version;
import se.eris.util.version.VersionCompiler;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class TestCompilerResolver implements ArgumentsProvider {

    private static final String DEFAULT_SOURCE_DIRECTORY = "src/test/data";
    private static final String DEFAULT_TARGET_DIRECTORY = "target/test/data/classes";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        final TestSettings testSettings = new TestSettings();

        System.out.println(testSettings);
        //overwrite default settings with explicitly set values on class
        extensionContext.getTestClass().map(c -> c.getAnnotation(VersionTest.class)).ifPresent(testSettings::overwrite);
        System.out.println(testSettings);
        //overwrite settings with explicitly set values on method
        extensionContext.getTestMethod().map(m -> m.getAnnotation(VersionTest.class)).ifPresent(testSettings::overwrite);
        System.out.println(testSettings);

        if (testSettings.classes.length == 0) {
            throw new IllegalArgumentException("At least one class to compile must be provided.");
        }

        Path targetDirectory = new File(testSettings.targetDirString).toPath();

        //map file name strings to java.io.File
        final File[] sourceFiles = Arrays.stream(testSettings.classes)
                .map(TestClass::new)
                .map(tc -> tc.getJavaFile(new File(testSettings.sourceDirString)))
                .toArray(File[]::new);

        //filter all versions before VersionTest.since()
        final Version[] versions = Arrays.stream(Version.values())
                .filter(v -> v.ordinal() >= testSettings.sinceVersion.ordinal())
                .toArray(Version[]::new);

        //map TestCompilers to junit Argument stream
        return VersionCompiler.compile(targetDirectory, versions, sourceFiles).entrySet().stream()
                .map(versionCompilerPair -> getParameterSet(versionCompilerPair.getKey(), versionCompilerPair.getValue(), testSettings.classes));
    }

    private Arguments getParameterSet(String version, TestCompiler testCompiler, String[] classes) {
        Object[] arguments = new Object[classes.length + 1];
        for (int i = 0; i < classes.length; i++) {
            try {
                arguments[i] = testCompiler.getCompiledClass(classes[i]);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format(
                        "Failed to retrieve java version %s compiled class %s",
                        version,
                        classes[i]
                ));
            }
        }
        arguments[arguments.length - 1] = testCompiler;
        return () -> arguments;
    }

    private static class TestSettings {
        Version sinceVersion = Version.JAVA7;
        String sourceDirString = DEFAULT_SOURCE_DIRECTORY;
        String targetDirString = DEFAULT_TARGET_DIRECTORY;
        String[] classes = VersionTest.NO_CLASSES;

        void overwrite(VersionTest versionTest) {
            if (versionTest.since() != VersionTest.NO_VERSION) {
                sinceVersion = versionTest.since();
            }
            if (!VersionTest.NO_DIRECTORY.equals(versionTest.sourceDir())) {
                sourceDirString = versionTest.sourceDir();
            }
            if (!VersionTest.NO_DIRECTORY.equals(versionTest.targetDir())) {
                targetDirString = versionTest.targetDir();
            }
            if (versionTest.classes().length > 0) {
                classes = versionTest.classes();
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
