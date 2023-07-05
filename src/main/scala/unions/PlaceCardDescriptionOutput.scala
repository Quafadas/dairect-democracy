package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class PlaceCardDescriptionOutput(s: Option[String] = None)
object PlaceCardDescriptionOutput extends ShapeTag.Companion[PlaceCardDescriptionOutput] {
  val id: ShapeId = ShapeId("unions", "PlaceCardDescriptionOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[PlaceCardDescriptionOutput] = struct(
    string.optional[PlaceCardDescriptionOutput]("s", _.s),
  ){
    PlaceCardDescriptionOutput.apply
  }.withId(id).addHints(hints)
}
