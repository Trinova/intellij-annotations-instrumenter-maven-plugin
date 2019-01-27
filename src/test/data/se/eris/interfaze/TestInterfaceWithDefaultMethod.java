package se.eris.interfaze;

import org.jetbrains.annotations.NotNull;

public class TestInterfaceWithDefaultMethod {
	public static TestInterface testDefaultMethodViaLambda(){
		return string -> string;
	}

	public static TestInterface testDefaultMethodViaAnonymousClass(){
		return new TestInterface() {
			public String notAnnotated(String someString) {
				return someString;
			}
		};
	}

	public interface TestInterface {
		String notAnnotated(String someString);

		default int annotatedParameterDefaultMethod(@NotNull String notNull) {
			return notNull.length();
		}

		default @NotNull String annotatedReturnDefaultMethod(String someString) {
			return someString;
		}
	}
}