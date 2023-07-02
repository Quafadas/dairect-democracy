package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

/** @param name
  *   The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
  * @param description
  *   A description of what the function does, used by the model to choose when and how to call the function.
  */
final case class ChatCompletionFunctions(name: String, parameters: ChatCompletionFunctionParameters, description: Option[String] = None)
object ChatCompletionFunctions extends ShapeTag.Companion[ChatCompletionFunctions] {
  val id: ShapeId = ShapeId("openAI", "ChatCompletionFunctions")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[ChatCompletionFunctions] = struct(
    string.required[ChatCompletionFunctions]("name", _.name).addHints(smithy.api.Documentation("The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64."), smithy.api.Required()),
    ChatCompletionFunctionParameters.schema.required[ChatCompletionFunctions]("parameters", _.parameters).addHints(smithy.api.Required()),
    string.optional[ChatCompletionFunctions]("description", _.description).addHints(smithy.api.Documentation("A description of what the function does, used by the model to choose when and how to call the function.")),
  ){
    ChatCompletionFunctions.apply
  }.withId(id).addHints(hints)
}
