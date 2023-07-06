package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

/** @param role
  *   The role of the author of this message.
  * @param content
  *   The contents of the message.
  * @param function_call
  *   The name and arguments of a function that should be called, as generated by the model.
  */
final case class ChatCompletionResponseMessage(role: ChatCompletionResponseMessageRole, content: Option[String] = None, function_call: Option[ChatCompletionResponseMessageFunctionCall] = None)
object ChatCompletionResponseMessage extends ShapeTag.Companion[ChatCompletionResponseMessage] {
  val id: ShapeId = ShapeId("openAI", "ChatCompletionResponseMessage")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[ChatCompletionResponseMessage] = struct(
    ChatCompletionResponseMessageRole.schema.required[ChatCompletionResponseMessage]("role", _.role).addHints(smithy.api.Required()),
    string.optional[ChatCompletionResponseMessage]("content", _.content).addHints(smithy.api.Documentation("The contents of the message.")),
    ChatCompletionResponseMessageFunctionCall.schema.optional[ChatCompletionResponseMessage]("function_call", _.function_call),
  ){
    ChatCompletionResponseMessage.apply
  }.withId(id).addHints(hints)
}