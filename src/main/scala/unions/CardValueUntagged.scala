package unions

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.union

sealed trait CardValueUntagged extends scala.Product with scala.Serializable {
  @inline final def widen: CardValueUntagged = this
}
object CardValueUntagged extends ShapeTag.Companion[CardValueUntagged] {
  val id: ShapeId = ShapeId("unions", "CardValueUntagged")

  val hints: Hints = Hints(
    alloy.Untagged(),
  )

  final case class NCase(n: NormalCardValue) extends CardValueUntagged
  final case class FCase(f: FaceCard) extends CardValueUntagged

  object NCase {
    val hints: Hints = Hints.empty
    val schema: Schema[NCase] = bijection(NormalCardValue.schema.addHints(hints), NCase(_), _.n)
    val alt = schema.oneOf[CardValueUntagged]("n")
  }
  object FCase {
    val hints: Hints = Hints.empty
    val schema: Schema[FCase] = bijection(FaceCard.schema.addHints(hints), FCase(_), _.f)
    val alt = schema.oneOf[CardValueUntagged]("f")
  }

  implicit val schema: Schema[CardValueUntagged] = union(
    NCase.alt,
    FCase.alt,
  ){
    case c: NCase => NCase.alt(c)
    case c: FCase => FCase.alt(c)
  }.withId(id).addHints(hints)
}
