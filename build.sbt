inThisBuild(List(
  organization := "com.github.eldis",
  organizationHomepage := url("http://eldis.ru").some,
  homepage := url("https://github.com/eldis/java-tool-launcher").some,
  description := "Java application/ToolProvider launcher with @argfile support",
  startYear := 2019.some,

  scalaVersion := "2.13.1",
  scalafmtOnCompile := true
))

// Set up the root project
name := "root"
publish / skip := true

val core = project.settings(
  name := "tool-launcher",

  // We want the resulting JAR to be self-contained, and not interacting with
  // any other dependencies - so write it in raw Java.
  autoScalaLibrary := false,
  crossPaths := false,

  javacOptions ++= List(
    // No `-release`, and no `-library`. We want to support Java 9+ features where available.
    "-source", "8", "-target", "8",
    "-Xlint:all"
  ),
  doc / javacOptions := List("--release", "11"),

  // Set up publishing.
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }
)

// Tests need to be in a separate subproject, since we want to be able to use Scala.
val tests = project.settings(
  publish / skip := true,

  javacOptions += "-Xlint:all",
  libraryDependencies += "org.specs2" %% "specs2-core" % "4.6.0" % Test,

  // We need to fork to make sure `java.class.path` is properly set.
  Test / fork := true
).dependsOn(core)
