package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class PlaceCardDescriptionUntaggedInput(value: UntaggedPlayingCard)
object PlaceCardDescriptionUntaggedInput extends ShapeTag.Companion[PlaceCardDescriptionUntaggedInput] {
  val id: ShapeId = ShapeId("unions", "PlaceCardDescriptionUntaggedInput")

  val hints: Hints = Hints(
    smithy.api.Input(),
  )

  implicit val schema: Schema[PlaceCardDescriptionUntaggedInput] = struct(
    UntaggedPlayingCard.schema.required[PlaceCardDescriptionUntaggedInput]("value", _.value).addHints(smithy.api.Required()),
  ){
    PlaceCardDescriptionUntaggedInput.apply
  }.withId(id).addHints(hints)
}
