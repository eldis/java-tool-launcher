package ru.eldis

import java.nio.file.Files
import scala.jdk.CollectionConverters._
import scala.sys.process.Process

import org.specs2.mutable.Specification
import org.specs2.matcher.NoTypedEqual

class ToolLauncherSpec extends Specification with NoTypedEqual {
  "ToolLauncher" should {
    "pass simple arguments to the application" in {
      runEcho(Vector("foo", "bar")) must_=== Vector("foo", "bar")
    }
    "pass argfiles to the application" in {
      val file = Files.createTempFile("tool-launcher-spec", "")
      try {
        Files.write(file, Vector("foo", "bar", "baz").asJava)

        runEcho(Vector(s"@$file")) must_=== Vector(
          "foo",
          "bar",
          "baz"
        )
      } finally {
        Files.delete(file)
      }
    }
    "support argfiles over command line length limit" in {
      // 300000 arguments, 10 characters each, is 3 million characters -
      // should be over command line limit on most platforms.
      val data = (1.to(300000)).map(i => f"$i%010d").toVector
      val file = Files.createTempFile("tool-launcher-spec", "")
      try {
        Files.write(file, data.asJava)

        runEcho(Vector(s"@$file")) must_=== data
      } finally {
        Files.delete(file)
      }

    }
    "pass escaped arguments to the application" in {
      runEcho(Vector("\\@foo")) must_=== Vector("@foo")
    }
    "support running ToolProviders" in {
      // Run an arbitrary tool via launcher and directly, and compare results. This assumes
      // we are running from a JDK.
      val viaLauncher =
        runToolLauncher(Vector("-tool", "jar", "--version"))

      val directly =
        Process(Vector(jdkExecutable("jar"), "--version")).!!

      viaLauncher must_=== directly
    }
  }

  // Run the echo application, returning the arguments it received.
  def runEcho(args: Vector[String]): Vector[String] =
    runToolLauncher(
      Vector("-main", classOf[test.Echo].getName) ++ args
    ).linesIterator.toVector

  // Run `ToolLauncher` with the provided arguments, returning its stdout.
  def runToolLauncher(args: Vector[String]): String = {
    // Use the current classpath
    val classPath = sys.props.getOrElse(
      "java.class.path",
      sys.error("java.class.path is undefined")
    )
    val command = Vector(
      jdkExecutable("java"),
      "-cp",
      classPath,
      classOf[ToolLauncher].getName
    ) ++ args

    Process(command).!!
  }

  def jdkExecutable(name: String): String = {
    val home = sys.props
      .getOrElse("java.home", sys.error("java.home is undefined"))
    // Don't bother with the extension - that is handled by the `ProcessBuilder`.
    s"$home/bin/$name"
  }
}
