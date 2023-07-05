package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class PlayingCard(cardValue: CardValue)
object PlayingCard extends ShapeTag.Companion[PlayingCard] {
  val id: ShapeId = ShapeId("unions", "PlayingCard")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[PlayingCard] = struct(
    CardValue.schema.required[PlayingCard]("cardValue", _.cardValue).addHints(smithy.api.Required()),
  ){
    PlayingCard.apply
  }.withId(id).addHints(hints)
}
