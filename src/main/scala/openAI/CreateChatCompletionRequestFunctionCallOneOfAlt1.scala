package openAI

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

/** @param name
  *   The name of the function to call.
  */
final case class CreateChatCompletionRequestFunctionCallOneOfAlt1(name: String)
object CreateChatCompletionRequestFunctionCallOneOfAlt1 extends ShapeTag.Companion[CreateChatCompletionRequestFunctionCallOneOfAlt1] {
  val id: ShapeId = ShapeId("openAI", "CreateChatCompletionRequestFunctionCallOneOfAlt1")

  val hints: Hints = Hints.empty

  implicit val schema: Schema[CreateChatCompletionRequestFunctionCallOneOfAlt1] = struct(
    string.required[CreateChatCompletionRequestFunctionCallOneOfAlt1]("name", _.name).addHints(smithy.api.Documentation("The name of the function to call."), smithy.api.Required()),
  ){
    CreateChatCompletionRequestFunctionCallOneOfAlt1.apply
  }.withId(id).addHints(hints)
}
