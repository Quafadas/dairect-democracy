package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class UntaggedPlayingCard(cardValue: CardValueUntagged)
object UntaggedPlayingCard extends ShapeTag.Companion[UntaggedPlayingCard] {
  val id: ShapeId = ShapeId("unions", "UntaggedPlayingCard")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[UntaggedPlayingCard] = struct(
    CardValueUntagged.schema.required[UntaggedPlayingCard]("cardValue", _.cardValue).addHints(smithy.api.Required()),
  ){
    UntaggedPlayingCard.apply
  }.withId(id).addHints(hints)
}
