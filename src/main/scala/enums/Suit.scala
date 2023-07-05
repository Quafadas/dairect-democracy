package enums

import smithy4s.Enumeration
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.EnumTag
import smithy4s.schema.Schema.enumeration

sealed abstract class Suit(_value: String, _name: String, _intValue: Int, _hints: Hints) extends Enumeration.Value {
  override type EnumType = Suit
  override val value: String = _value
  override val name: String = _name
  override val intValue: Int = _intValue
  override val hints: Hints = _hints
  override def enumeration: Enumeration[EnumType] = Suit
  @inline final def widen: Suit = this
}
object Suit extends Enumeration[Suit] with ShapeTag.Companion[Suit] {
  val id: ShapeId = ShapeId("enums", "Suit")

  val hints: Hints = Hints.empty

  case object DIAMOND extends Suit("diamond", "DIAMOND", 0, Hints())
  case object CLUB extends Suit("club", "CLUB", 1, Hints())
  case object HEART extends Suit("heart", "HEART", 2, Hints())
  case object SPADE extends Suit("spade", "SPADE", 3, Hints())

  val values: List[Suit] = List(
    DIAMOND,
    CLUB,
    HEART,
    SPADE,
  )
  val tag: EnumTag = EnumTag.StringEnum
  implicit val schema: Schema[Suit] = enumeration(tag, values).withId(id).addHints(hints)
}
