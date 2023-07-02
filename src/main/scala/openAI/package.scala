package object openAI {
  type OpenAIService[F[_]] = smithy4s.kinds.FunctorAlgebra[OpenAIServiceGen, F]
  val OpenAIService = OpenAIServiceGen

  type ChatCompletionFunctionParameters = openAI.ChatCompletionFunctionParameters.Type
  /** A list of messages comprising the conversation so far. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_format_inputs_to_ChatGPT_models.ipynb). */
  type Messages = openAI.Messages.Type
  /** A list of functions the model may generate JSON inputs for. */
  type Functions = openAI.Functions.Type
  type CreateChatCompletionResponseChoices = openAI.CreateChatCompletionResponseChoices.Type
  type CreateChatCompletionRequestStopOneOfAlt1 = openAI.CreateChatCompletionRequestStopOneOfAlt1.Type

}