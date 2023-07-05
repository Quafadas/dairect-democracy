package enums

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class PlaceCardPictureInput(faceCard: FaceCard, suit: Suit)
object PlaceCardPictureInput extends ShapeTag.Companion[PlaceCardPictureInput] {
  val id: ShapeId = ShapeId("enums", "PlaceCardPictureInput")

  val hints: Hints = Hints(
    smithy.api.Input(),
  )

  implicit val schema: Schema[PlaceCardPictureInput] = struct(
    FaceCard.schema.required[PlaceCardPictureInput]("faceCard", _.faceCard).addHints(smithy.api.Required()),
    Suit.schema.required[PlaceCardPictureInput]("suit", _.suit).addHints(smithy.api.Required()),
  ){
    PlaceCardPictureInput.apply
  }.withId(id).addHints(hints)
}
