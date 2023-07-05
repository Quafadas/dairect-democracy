package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.union

sealed trait CardValue extends scala.Product with scala.Serializable {
  @inline final def widen: CardValue = this
}
object CardValue extends ShapeTag.Companion[CardValue] {
  val id: ShapeId = ShapeId("unions", "CardValue")

  val hints: Hints = Hints.empty

  final case class NCase(n: NormalCardValue) extends CardValue
  final case class FCase(f: FaceCard) extends CardValue

  object NCase {
    val hints: Hints = Hints.empty
    val schema: Schema[NCase] = bijection(NormalCardValue.schema.addHints(hints), NCase(_), _.n)
    val alt = schema.oneOf[CardValue]("n")
  }
  object FCase {
    val hints: Hints = Hints.empty
    val schema: Schema[FCase] = bijection(FaceCard.schema.addHints(hints), FCase(_), _.f)
    val alt = schema.oneOf[CardValue]("f")
  }

  implicit val schema: Schema[CardValue] = union(
    NCase.alt,
    FCase.alt,
  ){
    case c: NCase => NCase.alt(c)
    case c: FCase => FCase.alt(c)
  }.withId(id).addHints(hints)
}
