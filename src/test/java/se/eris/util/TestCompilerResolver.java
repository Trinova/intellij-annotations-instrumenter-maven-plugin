package se.eris.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import se.eris.notnull.AnnotationConfiguration;
import se.eris.notnull.Configuration;
import se.eris.notnull.ExcludeConfiguration;
import se.eris.notnull.instrumentation.ClassMatcher;
import se.eris.util.CompiledVersionsTest.InjectCompiler;
import se.eris.util.CompiledVersionsTest.Version;
import se.eris.util.version.VersionCompiler;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public class TestCompilerResolver implements ArgumentsProvider {

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

        //filter all versions before CompiledVersionsTest.since()
        final Version[] versions = Arrays.stream(Version.values())
                .filter(v -> v.ordinal() >= testSettings.sinceVersion.ordinal())
                .toArray(Version[]::new);


        Configuration configuration = new Configuration(false, new AnnotationConfiguration(), new ExcludeConfiguration(emptySet()));
        //overwrite default instrumentation configuration with explicit configuration on class
        configuration = extensionContext.getTestClass()
                .map(c -> c.getAnnotation(InstrumentationConfiguration.class))
                .map(this::toConfiguration)
                .orElse(configuration);
        //overwrite instrumentation configuration with explicit configuration on method
        configuration = extensionContext.getTestMethod()
                .map(m -> m.getAnnotation(InstrumentationConfiguration.class))
                .map(this::toConfiguration)
                .orElse(configuration);

        //map file name strings of non nested classes (without `$` in the name) to java.io.File
        final File[] sourceFiles = Arrays.stream(testSettings.classes)
                .filter(className -> !className.contains("$"))
                .map(TestClass::new)
                .map(tc -> tc.getJavaFile(new File(testSettings.sourceDirString)))
                .toArray(File[]::new);

        final Map<String, TestCompiler> compilers = VersionCompiler.compile(targetDirectory, versions, configuration, sourceFiles);

        //map TestCompilers to junit Argument stream
        return Arrays.stream(versions)
                .map(Version::getVersionString)
                .map(version -> getParameterSet(version, compilers.get(version), testSettings.classes, testSettings.injectCompiler));
    }

    private Configuration toConfiguration(InstrumentationConfiguration instrumentationConfiguration) {
        final Set<ClassMatcher> classMatchers = Arrays.stream(instrumentationConfiguration.classMatcher())
                .map(ClassMatcher::namePattern)
                .collect(Collectors.toSet());

        return new Configuration(
                instrumentationConfiguration.implicit(),
                new AnnotationConfiguration(
                        new HashSet<>(Arrays.asList(instrumentationConfiguration.notNull())),
                        new HashSet<>(Arrays.asList(instrumentationConfiguration.nullable()))
                ), new ExcludeConfiguration(classMatchers));
    }

    /**
     * @param version      target and source java version
     * @param testCompiler the test compiler instance
     * @param classes      sourceClasses to provide as arguments
     * @return [TestCompiler testCompiler, Class... sourceClasses, ]
     */
    private Arguments getParameterSet(String version, TestCompiler testCompiler, String[] classes, InjectCompiler injectCompiler) {
        Object[] arguments;
        if (injectCompiler == InjectCompiler.YES) {
            arguments = new Object[classes.length + 1];
            arguments[0] = testCompiler;
        } else {
            arguments = new Object[classes.length];
        }
        int offset = arguments.length - classes.length;
        for (int i = offset; i < arguments.length; i++) {
            try {
                arguments[i] = testCompiler.getCompiledClass(classes[i - offset]);
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

    private static class TestSettings {
        Version sinceVersion = Version.JAVA7;
        String sourceDirString = DEFAULT_SOURCE_DIRECTORY;
        String targetDirString = DEFAULT_TARGET_DIRECTORY;
        String[] classes = CompiledVersionsTest.NO_CLASSES;
        InjectCompiler injectCompiler = InjectCompiler.YES;

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
            if (compiledVersionsTest.injectCompiler() != CompiledVersionsTest.NO_INJECT_COMPILER) {
                injectCompiler = compiledVersionsTest.injectCompiler();
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
