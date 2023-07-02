package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.int
import smithy4s.schema.Schema.struct

final case class CreateChatCompletionResponseUsage(prompt_tokens: Int, completion_tokens: Int, total_tokens: Int)
object CreateChatCompletionResponseUsage extends ShapeTag.Companion[CreateChatCompletionResponseUsage] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionResponseUsage")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[CreateChatCompletionResponseUsage] = struct(
    int.required[CreateChatCompletionResponseUsage]("prompt_tokens", _.prompt_tokens).addHints(smithy.api.Required()),
    int.required[CreateChatCompletionResponseUsage]("completion_tokens", _.completion_tokens).addHints(smithy.api.Required()),
    int.required[CreateChatCompletionResponseUsage]("total_tokens", _.total_tokens).addHints(smithy.api.Required()),
  ){
    CreateChatCompletionResponseUsage.apply
  }.withId(id).addHints(hints)
}
