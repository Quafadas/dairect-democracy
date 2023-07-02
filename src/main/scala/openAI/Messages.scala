package openAI

import smithy4s.Hints
import smithy4s.Newtype
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.schema.Schema.bijection
import smithy4s.schema.Schema.list

/** A list of messages comprising the conversation so far. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_format_inputs_to_ChatGPT_models.ipynb). */
object Messages extends Newtype[List[ChatCompletionRequestMessage]] {
  val id: ShapeId = ShapeId("openAI", "Messages")
  val hints: Hints = Hints(
    smithy.api.Documentation("A list of messages comprising the conversation so far. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_format_inputs_to_ChatGPT_models.ipynb)."),
  )
  val underlyingSchema: Schema[List[ChatCompletionRequestMessage]] = list(ChatCompletionRequestMessage.schema).withId(id).addHints(hints).validated(smithy.api.Length(min = Some(1L), max = None))
  implicit val schema: Schema[Messages] = bijection(underlyingSchema, asBijection)
}
