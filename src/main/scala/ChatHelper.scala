package smithyOpenAI

import openAI.ChatCompletionRequestMessage
import openAI.ChatCompletionRequestMessageRole
import openAI.ChatCompletionFunctions
import openAI.CreateChatCompletionRequestStop
import smithy4s.Document
import openAI.CreateChatCompletionRequest
import openAI.CreateChatCompletion200



object ChatHelper:

  extension (s: String)
    def userMsg = ChatCompletionRequestMessage(
      role = ChatCompletionRequestMessageRole.user,
      content = s
    )
    def systemMsg = ChatCompletionRequestMessage(
      role = ChatCompletionRequestMessageRole.system,
      content = s
    )
  end extension

  extension (msg: CreateChatCompletion200)
    def printMsgsOnly =
      pprint.pprintln(msg.body.choices.map(_.message))

  end extension

  lazy val defaultSystemMessage = "You are a helpful assistent".systemMsg

  def apply(
      model: Option[String] = None,
      messages: List[ChatCompletionRequestMessage] = List(),
      functions: Option[List[ChatCompletionFunctions]] = None,
      function_call: Option[String] = None,
      temperature: Option[Double] = None,
      top_p: Option[Double] = None,
      n: Option[Int] = None,
      stream: Option[Boolean] = None,
      stop: Option[CreateChatCompletionRequestStop] = None,
      max_tokens: Option[Int] = None,
      presence_penalty: Option[Double] = None,
      frequency_penalty: Option[Double] = None,
      logit_bias: Option[Document] = None,
      user: Option[String] = None
  ) =
    val model0 = model.getOrElse("gpt-3.5-turbo-0613")

    val messages0 = messages match
      case Nil => List(defaultSystemMessage)
      case _   => messages

      // TODO add other params
    CreateChatCompletionRequest(
      model0,
      messages0,
      functions,
      function_call,
      temperature
    )

  end apply
end ChatHelper