package weather

import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.*
import cats.effect.IO
import scala.annotation.experimental
import smithy.api.* // if you want to use hints from the official smithy standard library
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
@hints(Documentation("Get the weather at lat long"))
trait WeatherService() derives API:

  @readonly
  @httpGet("/weather/{lat}/{long}")
  def getWeatherLatLong(
      @hints(Documentation("Latitude")) @httpLabel lat: Double,
      @hints(Documentation("Longditude")) @httpLabel long: Double
  ): IO[WeatherOut] // = IO(WeatherOut(s"Lovely weather at $lat, $long"))
end WeatherService

case class WeatherOut(weather: String) derives Schema

case class LatLong(
) derives Schema
