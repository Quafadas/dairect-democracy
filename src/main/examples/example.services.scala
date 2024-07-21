package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.process.ProcessBuilder
import fs2.text
import smithy.api.Documentation
import smithy4s.*
import smithy4s.deriving.aliases.*
import smithy4s.deriving.{*, given}

import scala.annotation.experimental

// @error("execution error")
case class LocationNotRecognised(errorMessage: String) extends Throwable derives Schema:
  override def getMessage(): String = errorMessage
end LocationNotRecognised

val weatherImpl = new WeatherService():
  def getWeatherLatLong(lat: Double, long: Double): IO[WeatherOut] =
    IO(WeatherOut(s"Lovely weather at $lat, $long"))

@experimental
@simpleRestJson
@hints(smithy.api.Documentation("weather service"))
/** Get the weather for a city given a latitude and longitude
  */
trait WeatherService() derives API:
  @readonly
  @httpGet("/weather/{lat}/{long}")
  @hints(smithy.api.Documentation("Get the weather at lat long"))
  def getWeatherLatLong(
      @hints(Documentation("Latitude")) @httpLabel lat: Double,
      @hints(Documentation("Longditude")) @httpLabel long: Double
  ): IO[WeatherOut] // = IO(WeatherOut(s"Lovely weather at $lat, $long"))
end WeatherService

case class WeatherOut(weather: String) derives Schema

val osImpl = new OsTool() {}
val scalaCliImpl = new ScalaCliTool() {}

val autoCodeable = new AutoCode {}

// Note the explicit references below... can't do better for now... move on.
trait AutoCode extends OsTool with ScalaCliTool derives API:
  def compileScalaDir(dir: String): IO[String] = scalaCliImpl.compile(dir)
  def runScalaDir(dir: String): IO[String] = scalaCliImpl.run(dir)
  override def makeTempDir(dirPrefix: String): IO[String] = osImpl.makeTempDir(dirPrefix)
  override def createOrOverwriteFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
    osImpl.createOrOverwriteFileInDir(dir, fileName, contents)
end AutoCode

// trait AutoCode(using os: OsTool, scalaCliTool: ScalaCliTool) derives API:
//   def compile(dir: String): IO[String] = scalaCliTool.compile(dir)
//   def runScalaDir(dir: String): IO[String] = scalaCliTool.run(dir)
//   def makeTempDir(dirPrefix: String): IO[String] = os.makeTempDir(dirPrefix)
//   def createOrOverwriteFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
//     os.createOrOverwriteFileInDir(dir, fileName, contents)
// end AutoCode

trait ScalaCliTool derives API:
  def compile(dir: String): IO[String] =
    val asPath = fs2.io.file.Path(dir)
    val scalaCliArgs = List(
      "compile",
      dir
    )
    ProcessBuilder(
      "scala-cli",
      scalaCliArgs
    ).withWorkingDirectory(asPath)
      .spawn[IO]
      .use { p =>
        val stdout = p.stdout
          .through(text.utf8.decode)
          .compile
          .toList
          .map(_.mkString)

        val stdError = p.stderr
          .through(text.utf8.decode)
          .compile
          .toList
          .map(_.mkString)

        stdout.both(stdError).map { (out, err) =>
          s"stdout: $out\nstderr: $err"
        }
      }
  end compile

  def run(dir: String): IO[String] =
    val asPath = fs2.io.file.Path(dir)
    val scalaCliArgs = List(
      "run",
      dir
    )
    ProcessBuilder(
      "scala-cli",
      scalaCliArgs
    ).withWorkingDirectory(asPath)
      .spawn[IO]
      .use { p =>
        val stdout = p.stdout
          .through(text.utf8.decode)
          .compile
          .toList
          .map(_.mkString)

        val stdError = p.stderr
          .through(text.utf8.decode)
          .compile
          .toList
          .map(_.mkString)

        stdout.both(stdError).map { (out, err) =>
          s"stdout: $out\nstderr: $err"
        }
      }
  end run

end ScalaCliTool
