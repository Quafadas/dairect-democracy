package io.github.quafadas.dairect

import cats.effect.IO
import cats.effect.kernel.Resource
import ciris.env
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals.uri

case class OpenAiPlatform(
    chatApi: ChatGpt,
    assistantApi: AssistantApi,
    filesApi: FilesApi,
    vectorStoreApi: VectorStoreApi,
    vectorStoreFilesApi: VectorStoreFilesApi,
    runApi: RunApi,
    runStepsApi: RunStepsApi,
    threadApi: ThreadApi,
    messageApi: MessagesApi,
    authdClient: Client[IO]
)

object OpenAiPlatform:
  def defaultAuthLogToFile(
      provided: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  ): Resource[IO, OpenAiPlatform] =
    val apikey = env("OPEN_AI_API_TOKEN").as[String].load[IO].toResource

    val logPath = fs2.io.file.Path("")
    def loggerAndPath(name: String) =
      val logPathNext = logPath / name
      val logger = fileLogger(logPathNext)
      (logPathNext, logger)
    end loggerAndPath

    val (mlogPath, mApiLogger) = loggerAndPath("messagesApi.log")
    val (threadLogPath, threadApiLogger) = loggerAndPath("threadApi.log")

    val (runApiLogPath, runApiLogger) = loggerAndPath("runApi.log")
    val (runStepsApiLogPath, runStepsApiLogger) = loggerAndPath("runStepsApi.log")
    val (vectorStoreFilesLogPath, vectorStoreFilesApiLogger) = loggerAndPath("vectorStoreFiles.log")
    val (vectorStoreLogPath, vectorStoreApiLogger) = loggerAndPath("vectorStore.log")
    val (filesLogPath, filesApiLogger) = loggerAndPath("filesApi.log")
    val (assistantLogPath, assistantApiLogger) = loggerAndPath("assistantApi.log")
    val (rawLogPath, rawLog) = loggerAndPath("rawClient.log")

    for
      _ <- makeLogFile(mlogPath).toResource
      _ <- makeLogFile(threadLogPath).toResource
      _ <- makeLogFile(runApiLogPath).toResource
      _ <- makeLogFile(runStepsApiLogPath).toResource
      _ <- makeLogFile(vectorStoreFilesLogPath).toResource
      _ <- makeLogFile(vectorStoreLogPath).toResource
      _ <- makeLogFile(filesLogPath).toResource
      _ <- makeLogFile(assistantLogPath).toResource
      _ <- makeLogFile(rawLogPath).toResource
      client <- provided
      authdClient = authMiddleware(apikey)(client)
      assistClient = assistWare(authdClient)

      chatGpt <- ChatGpt.apply(rawLog(authdClient), uri"https://api.openai.com/")
      assistantApi <- AssistantApi.apply(assistantApiLogger(assistClient), uri"https://api.openai.com/")
      filesApi <- FilesApi.apply(filesApiLogger(assistClient), uri"https://api.openai.com/")
      vectorStoreApi <- VectorStoreApi.apply(vectorStoreApiLogger(assistClient), uri"https://api.openai.com/")
      vectorStoreFilesApi <- VectorStoreFilesApi.apply(
        vectorStoreFilesApiLogger(assistClient),
        uri"https://api.openai.com/"
      )
      runApi <- RunApi.apply(runApiLogger(assistClient), uri"https://api.openai.com/")
      runStepsApi <- RunStepsApi.apply(runStepsApiLogger(assistClient), uri"https://api.openai.com/")
      threadApi <- ThreadApi.apply(threadApiLogger(assistClient), uri"https://api.openai.com/")
      messageApi <- MessagesApi.apply(mApiLogger(assistClient), uri"https://api.openai.com/")
    yield OpenAiPlatform(
      chatGpt,
      assistantApi,
      filesApi,
      vectorStoreApi,
      vectorStoreFilesApi,
      runApi,
      runStepsApi,
      threadApi,
      messageApi,
      assistClient
    )
    end for
  end defaultAuthLogToFile

end OpenAiPlatform
