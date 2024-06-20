package io.github.quafadas.dairect

import cats.syntax.all.toTraverseOps
import ChatGpt.AiMessage
import ChatGpt.AiResponseFormat
import smithy4s.schema.Schema

import smithy4s.deriving.{*, given}

import smithy4s.schema.*
import smithy4s.json.Json
import smithy4s.Blob

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

    agents.flatTraverse(a =>
      val response = Agent.startAgent(
        a.model,
        a.seedMessages :+ msg,
        a.modelParams.copy(responseFormat = Some(AiResponseFormat.json)),
        a.toolkit
      )(using a.service)

      response.map { r =>
        val b = Blob(r.last.content.get)
        Json
          .read[Initiatives](b)
          .fold(
            e => throw new Exception(s"Failed to parse initiatives: $e"),
            identity
          )
          .initiatives
      }
    )

  end proposeInitiatives

  case class Initiative(
      id: String,
      expectedOutcome: String,
      why: String
  ) derives Schema

  case class Initiatives(initiatives: List[Initiative]) derives Schema

end Democracy
