package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class PlaceCardDescriptionUntaggedOutput(s: Option[String] = None)
object PlaceCardDescriptionUntaggedOutput extends ShapeTag.Companion[PlaceCardDescriptionUntaggedOutput] {
  val id: ShapeId = ShapeId("unions", "PlaceCardDescriptionUntaggedOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  )

  implicit val schema: Schema[PlaceCardDescriptionUntaggedOutput] = struct(
    string.optional[PlaceCardDescriptionUntaggedOutput]("s", _.s),
  ){
    PlaceCardDescriptionUntaggedOutput.apply
  }.withId(id).addHints(hints)
}
