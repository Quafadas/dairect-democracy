package openAI

import smithy4s.Enumeration
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.EnumTag
import smithy4s.schema.Schema.enumeration

/** The role of the messages author. One of `system`, `user`, `assistant`, or `function`. */
sealed abstract class ChatCompletionRequestMessageRole(_value: String, _name: String, _intValue: Int, _hints: Hints) extends Enumeration.Value {
  override type EnumType = ChatCompletionRequestMessageRole
  override val value: String = _value
  override val name: String = _name
  override val intValue: Int = _intValue
  override val hints: Hints = _hints
  override def enumeration: Enumeration[EnumType] = ChatCompletionRequestMessageRole
  @inline final def widen: ChatCompletionRequestMessageRole = this
}
object ChatCompletionRequestMessageRole extends Enumeration[ChatCompletionRequestMessageRole] with ShapeTag.Companion[ChatCompletionRequestMessageRole] {
  val id: ShapeId = ShapeId("openAI", "ChatCompletionRequestMessageRole")

  val hints: Hints = Hints(
    smithy.api.Documentation("The role of the messages author. One of `system`, `user`, `assistant`, or `function`."),
  )

  case object system extends ChatCompletionRequestMessageRole("system", "system", 0, Hints())
  case object user extends ChatCompletionRequestMessageRole("user", "user", 1, Hints())
  case object assistant extends ChatCompletionRequestMessageRole("assistant", "assistant", 2, Hints())
  case object function extends ChatCompletionRequestMessageRole("function", "function", 3, Hints())

  val values: List[ChatCompletionRequestMessageRole] = List(
    system,
    user,
    assistant,
    function,
  )
  val tag: EnumTag = EnumTag.StringEnum
  implicit val schema: Schema[ChatCompletionRequestMessageRole] = enumeration(tag, values).withId(id).addHints(hints)
}
