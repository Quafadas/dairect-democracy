package io.github.quafadas.dairect

object Democracy:
  def collaborate() = ???

  def vote() = ???

  def proposeInitiatives(agents: List[Agent[?]]) =
    val msg =
      """You are asked to propose an `initiative`. Each initiative should be a
      | stepping stone toward the goal of the democracy you are part of, and was defined earlier. Each initiative should be small - a step in the right direction,
      | it does not need to be the whole journey. Each initiative should be a set of concrete actions that can be taken by yourself. They may help other democratic members to use their skills in follow up initiatives.
      | It is not mandatory to propose an intiative.
      | You should only put forward initiatives you think are helpful. You may put forward a maximum of three initiatives - increment the number in the ID to make more than one.
      | Initiatives should be of the form [{
      | "id": "{{my-name}}-1",
      | "expectedOutcome": "I will do X, Y, Z",
      |}
      | """.stripMargin
  end proposeInitiatives

end Democracy
