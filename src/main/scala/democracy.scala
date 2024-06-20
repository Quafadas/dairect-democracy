package io.github.quafadas.dairect

import cats.syntax.all.toTraverseOps
import ChatGpt.AiMessage
import ChatGpt.AiResponseFormat

object Democracy:
  def collaborate() = ???

  def vote() = ???

  def proposeInitiatives(agents: List[Agent[?]]) =
    val msg = AiMessage.user(
      """In this iteration, you should not take any action or call any tools. You are asked to propose `initiatives`. An initiative is small plan -
      | a stepping stone toward the user goal. Each initiative should be small.
      | it does not need to be the whole journey. Each initiative should be a concrete actions. They may help or unblock other team members to use their skills in future up initiatives.
      | It is not mandatory to propose an intiative.
      | You should only put forward initiatives you think are helpful. You may put forward a maximum of three initiatives - increment the number in the ID to make more than one.
      | Respond with a json array of initiatives e.g. "initiatives" = [{
      | "id": "{{my-name}}-1",
      | "expectedOutcome": "I will do X, Y, Z",
      | "why": "A chain of thought on why this might be helpful"
      |}] but do not take any action - make this plan only""".stripMargin
    )

    agents.traverse(a =>
      Agent.startAgent(
        a.model,
        a.seedMessages :+ msg,
        a.modelParams.copy(responseFormat = Some(AiResponseFormat.json)),
        a.toolkit
      )(using a.service)
    )

  end proposeInitiatives

end Democracy
