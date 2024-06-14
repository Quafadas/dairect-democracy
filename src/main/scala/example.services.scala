package io.github.quafadas.dairect

import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.*
import cats.effect.IO
import scala.annotation.experimental
import smithy.api.Documentation // if you want to use hints from the official smithy standard library
import alloy.* // if you want to use hints from the alloy library
import cats.effect.std.Console

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

@experimental
@hints(smithy.api.Documentation("Local file and os operations"))
/** Local file and os operations
  */
trait OsTool derives API:
  /** Creates a temporary directory on the local file system.
    */
  def makeTempDir(dirPrefix: String): IO[String] =
    IO.println("Creating a temporary directory") >>
      IO.blocking {
        val outDir = os.temp.dir(deleteOnExit = false, prefix = dirPrefix).toString
        outDir.toString
      }

  def createFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
    IO.println(s"Creating a file in $dir") >>
      IO.blocking {
        val filePath = os.Path(dir) / fileName
        os.write(filePath, contents.getOrElse(""))
        filePath.toString
      }

  def askForHelp(question: String): IO[String] =
    for
      _ <- Console[IO].println(s"I need guidance with: $question")
      n <- Console[IO].readLine
    yield n

end OsTool
