package weather

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

/** @param weather
  *   A description of the weather in the city
  */
case class GetWeatherOutput(weather: String)
object GetWeatherOutput extends ShapeTag.Companion[GetWeatherOutput] {
  val id: ShapeId = ShapeId("weather", "GetWeatherOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[GetWeatherOutput] = struct(
    string.required[GetWeatherOutput]("weather", _.weather).addHints(smithy.api.Documentation("A description of the weather in the city"), smithy.api.Required()),
  ){
    GetWeatherOutput.apply
  }.withId(id).addHints(hints)
}