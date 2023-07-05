package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class PlaceCardDescriptionInput(value: PlayingCard)
object PlaceCardDescriptionInput extends ShapeTag.Companion[PlaceCardDescriptionInput] {
  val id: ShapeId = ShapeId("unions", "PlaceCardDescriptionInput")

  val hints: Hints = Hints(
    smithy.api.Input(),
  )

  implicit val schema: Schema[PlaceCardDescriptionInput] = struct(
    PlayingCard.schema.required[PlaceCardDescriptionInput]("value", _.value).addHints(smithy.api.Required()),
  ){
    PlaceCardDescriptionInput.apply
  }.withId(id).addHints(hints)
}
