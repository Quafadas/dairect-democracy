package weather

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

case class WeatherOut(weather: String)
object WeatherOut extends ShapeTag.Companion[WeatherOut] {
  val id: ShapeId = ShapeId("weather", "WeatherOut")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[WeatherOut] = struct(
    string.required[WeatherOut]("weather", _.weather).addHints(smithy.api.Required()),
  ){
    WeatherOut.apply
  }.withId(id).addHints(hints)
}