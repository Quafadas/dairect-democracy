package enums

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

/** @param cardValue
  *   The face card
  * @param suit
  *   The suit
  */
final case class PlayingCard(cardValue: CardValue, suit: Suit)
object PlayingCard extends ShapeTag.Companion[PlayingCard] {
  val id: ShapeId = ShapeId("enums", "PlayingCard")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[PlayingCard] = struct(
    CardValue.schema.required[PlayingCard]("cardValue", _.cardValue).addHints(smithy.api.Documentation("The face card"), smithy.api.HttpLabel(), smithy.api.Required()),
    Suit.schema.required[PlayingCard]("suit", _.suit).addHints(smithy.api.Documentation("The suit"), smithy.api.HttpLabel(), smithy.api.Required()),
  ){
    PlayingCard.apply
  }.withId(id).addHints(hints)
}
