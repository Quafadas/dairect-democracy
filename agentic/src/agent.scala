package io.github.quafadas.dairect

import cats.effect.IO
import cats.syntax.all.toTraverseOps
import io.github.quafadas.dairect.ChatGpt.AiMessage
import io.github.quafadas.dairect.ChatGpt.ChatGptConfig
import smithy4s.Service
import smithy4s.json.Json
import smithy4s.kinds.FunctorAlgebra

case class Agent[Alg[_[_, _, _, _, _]]](
    model: ChatGpt,
    // systemMessage: SystemMessage,
    seedMessages: List[AiMessage],
    modelParams: ChatGptConfig,
    toolkit: FunctorAlgebra[Alg, IO],
    service: Service[Alg],
    name: String
)

extension [Alg[_[_, _, _, _, _]]](agent: Agent[Alg])
  inline def startAgent =
    Agent.startAgent(agent.model, agent.seedMessages, agent.modelParams, agent.toolkit)(using
      agent.service
    )

  def userMessage(msg: String) = AiMessage.user(msg, Some(agent.name))
  def systemMessage(msg: String) = AiMessage.system(msg, Some(agent.name))
end extension

object Agent:

  private enum ContinueFold:
    case Stop
    case Continue
  end ContinueFold

  /** @param model
    *   \- An implementation of the ChatGpt service. There is not a "single" one of these, as it is anticipated that
    *   people may wish to configure different middleware (logging et al) for individual agents.
    * @param seedMessages
    *   \- The messages that will seed the conversation with this agent.
    * @param modelParams
    *   \- Params to call chatGPT with.
    * @param toolkit
    *   \- This is a smithy4s 'API' or 'Service'. This agent will be able to call the Operations exposed by this
    *   service, as tool calls.
    * @return
    */
  def startAgent[Alg[_[_, _, _, _, _]]](
      model: ChatGpt,
      seedMessages: List[AiMessage],
      modelParams: ChatGptConfig,
      toolkit: FunctorAlgebra[Alg, IO]
  )(implicit S: Service[Alg]) =
    val functions = ioToolGen.toJsonSchema(toolkit)
    val functionDispatcher = ioToolGen.openAiSmithyFunctionDispatch(toolkit)
    fs2.Stream
      .unfoldEval[IO, (ContinueFold, List[AiMessage]), List[AiMessage]]((ContinueFold.Continue, seedMessages)) {
        (continue, allMessages) =>
          continue match
            case ContinueFold.Stop => IO.pure(None)
            case ContinueFold.Continue =>
              model
                .chat(
                  model = modelParams.model,
                  temperature = modelParams.temperature,
                  messages = allMessages,
                  tools = Some(functions),
                  response_format = modelParams.responseFormat
                )
                .flatMap { response =>
                  // println(response)
                  val botChoices = response.choices.head
                  // val responseMsg = botChoices.message
                  botChoices.finish_reason match
                    case None =>
                      IO.raiseError(
                        new Exception("No finish reason provided. Bot should always provide a finish reason")
                      )
                    case Some(value) =>
                      value match
                        case "tool_calls" =>
                          val fctCalls = botChoices.message.tool_calls.getOrElse(List.empty)
                          val newMessages = fctCalls
                            .traverse(fct =>
                              functionDispatcher.apply(fct.function).map { result =>
                                // println(result)
                                AiMessage.tool(
                                  tool_call_id = fct.id,
                                  content = Json.writeDocumentAsPrettyString(result)
                                )
                              }
                            )
                            .map { msgs =>
                              allMessages ++ botChoices.toMessage ++ msgs
                            }
                          newMessages.map(msgs => Some((msgs, (ContinueFold.Continue, msgs))))

                        case "stop" =>
                          val finalMessages = allMessages ++ botChoices.toMessage
                          IO.pure(Some((finalMessages, (ContinueFold.Stop, finalMessages))))
                        case _ =>
                          IO.println("Bot finished with unknown finish reason") >>
                            IO.println(response) >>
                            IO.raiseError(new Exception("Bot finished with unknown finish reason"))

                  end match
                }

      }
      .compile
      .lastOrError

  end startAgent

end Agent
