package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.int
import smithy4s.schema.Schema.struct

final case class CreateChatCompletionResponseChoicesItem(index: Option[Int] = None, message: Option[ChatCompletionResponseMessage] = None, finish_reason: Option[CreateChatCompletionResponseChoicesItemFinishReason] = None)
object CreateChatCompletionResponseChoicesItem extends ShapeTag.Companion[CreateChatCompletionResponseChoicesItem] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionResponseChoicesItem")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[CreateChatCompletionResponseChoicesItem] = struct(
    int.optional[CreateChatCompletionResponseChoicesItem]("index", _.index),
    ChatCompletionResponseMessage.schema.optional[CreateChatCompletionResponseChoicesItem]("message", _.message),
    CreateChatCompletionResponseChoicesItemFinishReason.schema.optional[CreateChatCompletionResponseChoicesItem]("finish_reason", _.finish_reason),
  ){
    CreateChatCompletionResponseChoicesItem.apply
  }.withId(id).addHints(hints)
}
