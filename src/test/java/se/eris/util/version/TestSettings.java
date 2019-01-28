package se.eris.util.version;

import java.util.Arrays;
import java.util.StringJoiner;

class TestSettings {
    CompiledVersionsTest.Version sinceVersion = CompiledVersionsTest.Version.JAVA7;
    String sourceDirString = "src/test/data";
    String targetDirString = "target/test/data/classes";
    String[] classes = CompiledVersionsTest.NO_CLASSES;
    CompiledVersionsTest.InjectCompiler injectCompiler = CompiledVersionsTest.InjectCompiler.YES;

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
