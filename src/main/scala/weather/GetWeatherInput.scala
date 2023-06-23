package weather

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

/** @param location
  *   The name of the city
  */
case class GetWeatherInput(location: String)
object GetWeatherInput extends ShapeTag.Companion[GetWeatherInput] {
  val id: ShapeId = ShapeId("weather", "GetWeatherInput")

  val hints: Hints = Hints(
    smithy.api.Input(),
  )

  implicit val schema: Schema[GetWeatherInput] = struct(
    string.required[GetWeatherInput]("location", _.location).addHints(smithy.api.Documentation("The name of the city"), smithy.api.HttpLabel(), smithy.api.Required()),
  ){
    GetWeatherInput.apply
  }.withId(id).addHints(hints)
}