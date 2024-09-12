package io.github.quafadas.dairect

import org.http4s.ember.client.EmberClientBuilder
import cats.effect.IO
import io.github.quafadas.dairect.RunApi.CreateThread
import smithy4s.deriving.API
import cats.syntax.parallel

@main def searchTest =
  val client = EmberClientBuilder.default[IO].build.allocated.map(_._1).Ø

  val OpenAiPlatform(
    chatGpt,
    assistantApi,
    filesApi,
    vectorStoreApi,
    vectorStoreFilesApi,
    runApi,
    runStepsApi,
    threadApi,
    messageApi,
    httpClient // This will send your open AI keys with it's requests... be careful
  ) = OpenAiPlatform.defaultAuthLogToFile().allocated.map(_._1).Ø

  val internetTool = InternetTool.makeFromClient(client)
  val internetToolAPI = API[InternetTool].liftService(internetTool)
  // val internetToolDispatch = internetToolAPI.dispatcher

  val assistant = assistantApi.create("gpt-4o").Ø

  println(assistant)

  val thread = threadApi
    .create(
      List(
        ThreadMessage
          .user("""
          | I want to purchase a new monitor. I use it mostly for coding. What would be a suitable montior size?
          | Search for suitable monitors available in switzerland. Provide a summary table with prices (in CHF), links to reviews and purchase links in markdown format.
          | Vist each of the links and summarise the reviews in a paragraph in the table.
          | Compare the monitors based on the following criteria: price, resolution, refresh rate size, suitability for development work.
          | Make a final recommendation including a chain of thought for the best monitor to purchase.
          | Create a temporary directory on the local machine.
          | Save this markdown file locally in the temporary directory.
          | Provide the file path and summarise your work, provide a chain of thought to check if the task is copmlete.
          """)
      )
    )
    .Ø

  println(thread)

  val run = runApi.streamToolRun(
    httpClient,
    thread.id,
    assistant_id = assistant.id,
    tools = internetToolAPI,
    parallel_tool_calls = Some(false)
  )

  run.compile.drain.Ø

  println(
    threadApi
      .getThread(thread.id)
      .Ø
  )

end searchTest
