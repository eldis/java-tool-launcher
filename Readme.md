# Java tool launcher

![Travis (.com)](https://img.shields.io/travis/com/eldis/java-tool-launcher)
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.github.eldis/tool-launcher?server=https%3A%2F%2Foss.sonatype.org)

A self-contained launcher for Java applications, providing `@argfile` support.

## Motivation

- Some use cases for Java tools require passing a large command line, which can reach the size limit on Windows (or even on Linux). There are workarounds in some scenarios (e.g. using classpath jars, or built-in `@argfile` support), but not in general.
- In some build systems (e.g. SBT), using tools from another JVM is more difficult than running `java` or `javac`.

## Features

- Supports launching main classes.
- Supports launching `ToolProvider` tools (requires JVM 9 or higher).
- Supports `@argfile`s.

## Syntax

Launch a main class:

```sh
$ java -cp my-app.jar -jar tool-launcher.jar -main my.App <args>
```

Launch a Java tool:

```sh
$ java -jar tool-launcher.jar -tool jdeps my-app.jar
```

Expand an `@argfile`:

```sh
$ java -jar tool-launcher.jar -tool jdeps @my-args.txt
```

Escape an argument starting with `@` (for bash - this depends on your shell):

```sh
$ java -jar tool-launcher.jar -tool javac '\@foo'
```

## Requirements

- JVM 8+ for running;
- JVM 9+ for `ToolProvider` support;
- JVM 11+ for building.

## Releasing/publishing

- This is versioned via [sbt-dynver](https://github.com/dwijnand/sbt-dynver).
- This is published to [Sonatype](https://oss.sonatype.org) - you would need to set up your environment as described [here](https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html)
