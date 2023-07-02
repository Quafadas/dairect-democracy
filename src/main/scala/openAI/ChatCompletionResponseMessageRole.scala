package openAI

import smithy4s.Enumeration
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.EnumTag
import smithy4s.schema.Schema.enumeration

/** The role of the author of this message. */
sealed abstract class ChatCompletionResponseMessageRole(_value: String, _name: String, _intValue: Int, _hints: Hints) extends Enumeration.Value {
  override type EnumType = ChatCompletionResponseMessageRole
  override val value: String = _value
  override val name: String = _name
  override val intValue: Int = _intValue
  override val hints: Hints = _hints
  override def enumeration: Enumeration[EnumType] = ChatCompletionResponseMessageRole
  @inline final def widen: ChatCompletionResponseMessageRole = this
}
object ChatCompletionResponseMessageRole extends Enumeration[ChatCompletionResponseMessageRole] with ShapeTag.Companion[ChatCompletionResponseMessageRole] {
  val id: ShapeId = ShapeId("openAI", "ChatCompletionResponseMessageRole")

  val hints: Hints = Hints(
    smithy.api.Documentation("The role of the author of this message."),
  )

  case object system extends ChatCompletionResponseMessageRole("system", "system", 0, Hints())
  case object user extends ChatCompletionResponseMessageRole("user", "user", 1, Hints())
  case object assistant extends ChatCompletionResponseMessageRole("assistant", "assistant", 2, Hints())
  case object function extends ChatCompletionResponseMessageRole("function", "function", 3, Hints())

  val values: List[ChatCompletionResponseMessageRole] = List(
    system,
    user,
    assistant,
    function,
  )
  val tag: EnumTag = EnumTag.StringEnum
  implicit val schema: Schema[ChatCompletionResponseMessageRole] = enumeration(tag, values).withId(id).addHints(hints)
}
