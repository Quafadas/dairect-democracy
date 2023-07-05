package enums

import smithy4s.Enumeration
import smithy4s.Hints
import smithy4s.IntEnum
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.EnumTag
import smithy4s.schema.Schema.enumeration

sealed abstract class FaceCard(_value: String, _name: String, _intValue: Int, _hints: Hints) extends Enumeration.Value {
  override type EnumType = FaceCard
  override val value: String = _value
  override val name: String = _name
  override val intValue: Int = _intValue
  override val hints: Hints = _hints
  override def enumeration: Enumeration[EnumType] = FaceCard
  @inline final def widen: FaceCard = this
}
object FaceCard extends Enumeration[FaceCard] with ShapeTag.Companion[FaceCard] {
  val id: ShapeId = ShapeId("enums", "FaceCard")

  val hints: Hints = Hints(
    IntEnum(),
  )

  case object JACK extends FaceCard("JACK", "JACK", 11, Hints())
  case object QUEEN extends FaceCard("QUEEN", "QUEEN", 12, Hints())
  case object KING extends FaceCard("KING", "KING", 13, Hints())
  case object ACE extends FaceCard("ACE", "ACE", 1, Hints())
  case object JOKER extends FaceCard("JOKER", "JOKER", 0, Hints())

  val values: List[FaceCard] = List(
    JACK,
    QUEEN,
    KING,
    ACE,
    JOKER,
  )
  val tag: EnumTag = EnumTag.IntEnum
  implicit val schema: Schema[FaceCard] = enumeration(tag, values).withId(id).addHints(hints)
}
