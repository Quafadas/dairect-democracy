package weather

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.double
import smithy4s.schema.Schema.struct

/** @param lat
  *   Latitude
  * @param long
  *   Longditude
  */
case class LatLong(lat: Double, long: Double)
object LatLong extends ShapeTag.Companion[LatLong] {
  val id: ShapeId = ShapeId("weather", "LatLong")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[LatLong] = struct(
    double.required[LatLong]("lat", _.lat).addHints(smithy.api.Documentation("Latitude"), smithy.api.HttpLabel(), smithy.api.Required()),
    double.required[LatLong]("long", _.long).addHints(smithy.api.Documentation("Longditude"), smithy.api.HttpLabel(), smithy.api.Required()),
  ){
    LatLong.apply
  }.withId(id).addHints(hints)
}