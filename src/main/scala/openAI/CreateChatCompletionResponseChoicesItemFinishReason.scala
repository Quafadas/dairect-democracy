package openAI

import smithy4s.Enumeration
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.EnumTag
import smithy4s.schema.Schema.enumeration

sealed abstract class CreateChatCompletionResponseChoicesItemFinishReason(_value: String, _name: String, _intValue: Int, _hints: Hints) extends Enumeration.Value {
  override type EnumType = CreateChatCompletionResponseChoicesItemFinishReason
  override val value: String = _value
  override val name: String = _name
  override val intValue: Int = _intValue
  override val hints: Hints = _hints
  override def enumeration: Enumeration[EnumType] = CreateChatCompletionResponseChoicesItemFinishReason
  @inline final def widen: CreateChatCompletionResponseChoicesItemFinishReason = this
}
object CreateChatCompletionResponseChoicesItemFinishReason extends Enumeration[CreateChatCompletionResponseChoicesItemFinishReason] with ShapeTag.Companion[CreateChatCompletionResponseChoicesItemFinishReason] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionResponseChoicesItemFinishReason")

  val hints: Hints = Hints.empty

  case object stop extends CreateChatCompletionResponseChoicesItemFinishReason("stop", "stop", 0, Hints())
  case object length extends CreateChatCompletionResponseChoicesItemFinishReason("length", "length", 1, Hints())
  case object function_call extends CreateChatCompletionResponseChoicesItemFinishReason("function_call", "function_call", 2, Hints())

  val values: List[CreateChatCompletionResponseChoicesItemFinishReason] = List(
    stop,
    length,
    function_call,
  )
  val tag: EnumTag = EnumTag.StringEnum
  implicit val schema: Schema[CreateChatCompletionResponseChoicesItemFinishReason] = enumeration(tag, values).withId(id).addHints(hints)
}
