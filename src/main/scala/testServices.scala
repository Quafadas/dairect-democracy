package openai

import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.*
import cats.effect.IO
import scala.annotation.experimental
import smithy.api.Documentation // if you want to use hints from the official smithy standard library
import alloy.* // if you want to use hints from the alloy library

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

val osImpl = new OsService() {}

@experimental
@hints(smithy.api.Documentation("Os lib service"))
/** Get the weather for a city given a latitude and longitude
  */
trait OsService derives API:
  @readonly
  def makeTempDir(dirPrefix: String): IO[String] =
    IO.blocking {
      println("Creating a temporary directory")
      val outDir = os.temp.dir(deleteOnExit = false, prefix = dirPrefix).toString
      println(outDir)
      outDir.toString
    }

  def createFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
    IO.println(s"Creating a file in $dir") >>
      IO.blocking {
        val filePath = os.Path(dir) / fileName
        os.write(filePath, "Hello, world!")
        filePath.toString
      }

  def askHuman(question: String): IO[String] =
    IO.println(s"Human, please answer: $question") *>
      IO.blocking {
        scala.io.StdIn.readLine()
      }
end OsService
