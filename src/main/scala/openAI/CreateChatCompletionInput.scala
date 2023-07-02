package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class CreateChatCompletionInput(body: CreateChatCompletionRequest)
object CreateChatCompletionInput extends ShapeTag.Companion[CreateChatCompletionInput] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionInput")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[CreateChatCompletionInput] = struct(
    CreateChatCompletionRequest.schema.required[CreateChatCompletionInput]("body", _.body).addHints(smithy.api.HttpPayload(), smithy.api.Required()),
  ){
    CreateChatCompletionInput.apply
  }.withId(id).addHints(hints)
}
