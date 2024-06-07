package weather

import smithy4s.*
import smithy4s.deriving.{given, *}
import smithy4s.deriving.aliases.*
import cats.effect.IO
import scala.annotation.experimental

// @error("execution error")
case class LocationNotRecognised(errorMessage: String) extends Throwable derives Schema:
  override def getMessage(): String = errorMessage
end LocationNotRecognised

@experimental
@simpleRestJson
trait WeatherService() derives API:

  @readonly
  @httpGet("/weather/{lat}/{long}")
  def getWeatherLatLong(
      @httpLabel lat: Double,
      @httpLabel long: Double
  ): IO[WeatherOut] // = IO(WeatherOut(s"Lovely weather at $lat, $long"))
end WeatherService

case class WeatherOut(weather: String) derives Schema

case class LatLong(
) derives Schema
