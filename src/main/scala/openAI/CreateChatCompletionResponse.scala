package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.int
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class CreateChatCompletionResponse(id: String, _object: String, created: Int, model: String, choices: List[CreateChatCompletionResponseChoicesItem], usage: Option[CreateChatCompletionResponseUsage] = None)
object CreateChatCompletionResponse extends ShapeTag.Companion[CreateChatCompletionResponse] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionResponse")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[CreateChatCompletionResponse] = struct(
    string.required[CreateChatCompletionResponse]("id", _.id).addHints(smithy.api.Required()),
    string.required[CreateChatCompletionResponse]("object", _._object).addHints(smithy.api.Required()),
    int.required[CreateChatCompletionResponse]("created", _.created).addHints(smithy.api.Required()),
    string.required[CreateChatCompletionResponse]("model", _.model).addHints(smithy.api.Required()),
    CreateChatCompletionResponseChoices.underlyingSchema.required[CreateChatCompletionResponse]("choices", _.choices).addHints(smithy.api.Required()),
    CreateChatCompletionResponseUsage.schema.optional[CreateChatCompletionResponse]("usage", _.usage),
  ){
    CreateChatCompletionResponse.apply
  }.withId(id).addHints(hints)
}
