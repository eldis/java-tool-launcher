package ru.eldis;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.util.spi.ToolProvider;

public final class ToolLauncher {

  private ToolLauncher() {};

  public static void main(String[] args) {
    if (args.length < 2) {
      printUsage();
      System.exit(1);
      return;
    }

    Consumer<String[]> impl;
    switch (args[0]) {
      case "-main":
        impl = expandedArgs -> runMain(args[1], expandedArgs);
        break;
      case "-tool":
        impl = expandedArgs -> runTool(args[1], expandedArgs);
        break;
      default:
        printUsage();
        System.exit(1);
        return;
    }

    String[] expandedArgs =
        Arrays.stream(args)
            // Already parsed these.
            .skip(2)
            .flatMap(ToolLauncher::expandArgument)
            .toArray(String[]::new);
    impl.accept(expandedArgs);
  }

  private static void runMain(String className, String[] args) {
    try {
      Arrays.stream(Class.forName(className).getMethods())
          .filter(ToolLauncher::isMainMethod)
          .findAny()
          .orElseThrow(() -> new IllegalArgumentException("Class does not have a main method"))
          .invoke(null, new Object[] {args});
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void runTool(String toolName, String[] args) {
    ToolProvider.findFirst(toolName)
        .orElseThrow(() -> new IllegalArgumentException("Tool not found"))
        .run(System.out, System.err, args);
  }

  private static void printUsage() {
    System.err.println(
        "Usage:\n"
            + "  java -jar tool-launcher.jar -main <main class> <args>\n"
            + "  java -jar tool-launcher.jar -tool <tool name> <args>\n"
            + "\n"
            + "Launch an application or a tool with @argfile support.\n"
            + "\n"
            + "Options:\n"
            + "  -main       Find and run a main class.\n"
            + "  -tool       Find and run a ToolProvider (JVM 9+).\n"
            + "  @<file>     Inject a UTF-8 argfile (one argument per line).\n"
            + "  \\@<arg>     Pass an argument starting with the '@' character.\n");
  }

  private static boolean isMainMethod(Method m) {
    int publicStatic = Modifier.PUBLIC | Modifier.STATIC;

    // Method named `main`;
    return "main".equals(m.getName())
        // Public & static;
        && (m.getModifiers() & publicStatic) == publicStatic
        // Not generic;
        && m.getTypeParameters().length == 0
        // Returns void;
        && m.getReturnType().equals(Void.TYPE)
        // Accepts a single `String[]` parameter.
        && Arrays.equals(m.getParameterTypes(), new Class<?>[] {String[].class});
  }

  private static Stream<String> expandArgument(String arg) {
    if (arg.startsWith("@")) {
      // @argfile - expand.
      try {
        return Files.lines(Paths.get(arg.substring(1)));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else if (arg.startsWith("\\@")) {
      // Escaped @param - strip escaping.
      return Stream.of(arg.substring(1));
    } else {
      // Anything else - pass as-is.
      return Stream.of(arg);
    }
  }
}
