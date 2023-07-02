package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

final case class CreateChatCompletion200(body: CreateChatCompletionResponse)
object CreateChatCompletion200 extends ShapeTag.Companion[CreateChatCompletion200] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletion200")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[CreateChatCompletion200] = struct(
    CreateChatCompletionResponse.schema.required[CreateChatCompletion200]("body", _.body).addHints(smithy.api.HttpPayload(), smithy.api.Required()),
  ){
    CreateChatCompletion200.apply
  }.withId(id).addHints(hints)
}
