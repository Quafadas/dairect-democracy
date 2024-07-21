package io.github.quafadas.dairect

import cats.syntax.all.toTraverseOps
import ChatGpt.AiMessage
import ChatGpt.AiResponseFormat
import smithy4s.schema.Schema

import smithy4s.deriving.{*, given}

import smithy4s.schema.*
import smithy4s.json.Json
import smithy4s.Blob
import cats.effect.IO

object Democracy:
  def collaborate(agents: List[Agent[?]]) = ???

  def delegate(initiatives: List[Initiative], votes: List[(String, Int)], agents: List[Agent[?]]) =
    votes.sortBy(_._2).map(_._1).headOption match
      case Some(vote) =>
        val initiative = initiatives
          .find(_.id == vote.split("-").head)
          .getOrElse(throw new Exception(s"Failed to find initiative: $vote"))
        val agent = agents
          .find(_.modelParams.name.map(_ == vote.split("-").head).getOrElse(false))
          .getOrElse(throw new Exception(s"Failed to find agent: $vote"))

        Agent
          .startAgent(
            agent.model,
            agent.seedMessages :+ AiMessage.user(
              s"Your team has voted for the initiative: $vote. You have been selected to carry out the next initiative. Please read the following and take action" +
                s"Expected Outcome: ${initiative.expectedOutcome}"
            ),
            agent.modelParams.copy(responseFormat = Some(AiResponseFormat.json)),
            agent.toolkit
          )(using agent.service)

      case None => IO.raiseError(new Exception("No votes were recieved and there is therefore nothing to do"))

  def vote(agents: List[Agent[?]], proposals: List[Initiative]): IO[List[(String, Int)]] =
    val msg = AiMessage.user(
      s"""In this iteration, you should not take any action or call any tools. You are asked to vote on the initiatives proposed by your team members to decide which one to do next.
      | Initiatives are in the form of {
      | "id": "{{agent.name}}-{{x}}",
      | "expectedOutcome": "I will do X, Y, Z",
      | "why": "A chain of thought on why this might be helpful"
      | }. Read each carefully, and consider which initiative is most helpful to achieve the user goal.
      | You _MUST_ vote for at least one initiative. You vote by including the ID of the initiative in the `votes` array of your response.
      | You should vote for the course of action you believe is most helpful to achieve the user goal. You may vote for your own initiative.
      | Respond with a json array of initiatives you wish to vote in favour of e.g. "votes" = [
      | "{{agent.name}}-{{x}}"
      |]
      |""".stripMargin
    )

    val allVotes = agents
      .flatTraverse(a =>
        val response = Agent.startAgent(
          a.model,
          a.seedMessages :+ AiMessage.user(Json.writePrettyString(proposals)) :+ msg,
          a.modelParams.copy(responseFormat = Some(AiResponseFormat.json)),
          a.toolkit
        )(using a.service)
        response.flatTap(IO.println) >>
          response.map { r =>
            val b = Blob(r.last.content.get)
            Json
              .read[Votes](b)
              .fold(
                e => throw new Exception(s"Failed to parse initiatives: $e"),
                identity
              )
              .votes
          }
      )

    allVotes.flatTap(IO.println).map(inVotes => inVotes.groupBy(identity).mapValues(_.length).toList)

  end vote

  def proposeInitiatives(agents: List[Agent[?]]): IO[List[Initiative]] =
    val msg = AiMessage.user(
      """In this iteration, you should not take any action or call any tools. You are asked to propose `initiatives`. An initiative is small plan -
      | a stepping stone toward the user goal. Each initiative should be small and independant. An initiative should be achienavle in a singel interation and may not depend on the success of a prior initiative.
      | it does not need to be the whole journey. Each initiative should have a concrete outcome. An initiative may help or unblock other team members to use their skills in future up initiatives.
      | It is not mandatory to propose an intiative.
      | You should only put forward initiatives you think are helpful. You may put forward a maximum of three initiatives - increment the number in the ID to make more than one.
      | Respond with a json array of initiatives e.g. "initiatives" = [{
      | "id": "1",
      | "expectedOutcome": "I will do X, Y, Z",
      | "why": "A chain of thought on why this might be helpful"
      |}] but do not take any action - make this plan only. If you believe the user needs are satisfied, vote `COMPLETED` """.stripMargin
    )

    agents
      .flatTraverse(a =>
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

  case class Votes(votes: List[String]) derives Schema

end Democracy
