package io.github.quafadas.dairect

import cats.syntax.all.toTraverseOps
import ChatGpt.AiMessage

import smithy4s.deriving.{*, given}

import smithy4s.schema.*
import smithy4s.json.Json
import smithy4s.Blob
import cats.effect.IO

object Democracy:
  def collaborate(agents: List[Agent[?]]) = ???

  def delegate(initiatives: List[Initiative], votes: Map[Vote, Int], agents: List[Agent[?]]) =
    votes.toList.sortBy(_._2).map(_._1).headOption match
      case Some(vote) =>
        val initiative = initiatives
          .find(initiative => vote.id == initiative.id && vote.agent == initiative.agent)
          .getOrElse(throw new Exception(s"Failed to find initiative: $vote"))

        val chosenAgent = agents
          .find(_.name == initiative.agent)
          .getOrElse(throw new Exception(s"Failed to find agent to execute: $initiative"))

        Agent
          .startAgent(
            chosenAgent.model,
            chosenAgent.seedMessages :+ AiMessage.user(
              s"Your team has voted for the initiative: $vote. You have been selected to carry out the next initiative. Please read the following and take action. You should _only_ complete the " +
                s"work outlined in the initiative. Once it is complete return the finished response." +
                s"Expected Outcome: ${initiative.expectedOutcome}. Respond with a JSON object containing the vote ID and the summary of your work."
            ),
            chosenAgent.modelParams.copy(responseFormat = Some(AiResponseFormat.json)),
            chosenAgent.toolkit
          )(using chosenAgent.service)

      case None => IO.raiseError(new Exception("No votes were recieved and there is therefore nothing to do"))

  def vote(agents: List[Agent[?]], proposals: List[Initiative]): IO[Map[Vote, Int]] =
    val msg = AiMessage.user(
      s"""You must not take any action or call any tools. You are asked to vote on the initiatives proposed by your team members to decide which one to do next.
      | Initiatives are in the form of {
      | "id": "{"agent" : agent.name, "id" = x}",
      | "expectedOutcome": "I will do X, Y, Z",
      | "why": "A chain of thought on why this might be helpful"
      | }. Read each carefully, and consider which initiative is most helpful to achieve the user goal.
      | You _MUST_ vote for at least one initiative. You vote by including the ID of the initiative in the `votes` array of your response.
      | You should vote for the course of action you believe is most helpful to achieve the user goal. You may vote for your own initiative.
      | Respond with a json array of initiatives you wish to vote in favour of e.g. "votes" = [
      | {"agent" = agent.name,"id" = x }, {"agent" = agent.name,"id" = x }
      |].
      |The order is important. These initiatives will be processed sequentially.
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

    allVotes.flatTap(IO.println).map(inVotes => inVotes.groupMapReduce(identity)(_ => 1)(_ + _))

  end vote

  def proposeInitiatives(agents: List[Agent[?]]): IO[List[Initiative]] =
    def msg(a: Agent[?]) = AiMessage.user(
      s"""You must not take any action or call any tools. You are asked to propose `initiatives`. An initiative is small plan -
      | a stepping stone toward the user goal. Each initiative should be small and independant and may depend on the success of other initiatives where stated.
      | Each initiative should have a concrete outcome. An initiative may help or unblock other team members to use their skills in future initiatives.
      | It is not mandatory to propose an intiative.
      | You should only put forward initiatives you think are helpful and that _you_ can execute. You may put forward a maximum of three initiatives - increment the number in the ID to make more than one.
      | Respond with a json array of initiatives e.g. "initiatives" = [{
      | "id": "1",
      | "agent": "${a.name}",
      | "expectedOutcome": "I will do X, Y, Z",
      | "why": "A chain of thought on why this might be helpful"
      |}] but do not take any action - make this plan only. If you believe the user needs are satisfied, vote `COMPLETED` """.stripMargin
    )

    agents
      .flatTraverse(a =>
        val response = Agent.startAgent(
          a.model,
          a.seedMessages :+ msg(a),
          a.modelParams.copy(responseFormat = Some(AiResponseFormat.json)),
          a.toolkit
        )(using a.service)

        response.map { r =>
          val b = Blob(r.last.content.get)
          Json
            .read[Initiatives](b)
            .fold(
              e => throw new Exception(s"Failed to parse initiatives: $e. Recieved \n $b"),
              identity
            )
            .initiatives
        }
      )

  end proposeInitiatives

  case class Initiative(
      id: String,
      agent: String,
      expectedOutcome: String,
      why: String
  ) derives Schema

  case class Initiatives(initiatives: List[Initiative]) derives Schema

  case class Votes(votes: List[Vote]) derives Schema

  case class Vote(agent: String, id: String) derives Schema

end Democracy
