package io.github.quafadas.dairect

import smithy4s.*
import smithy4s.deriving.{*, given}
import cats.effect.{IO, IOApp}
import fs2.io.file.{Files, Path}
import fs2.{Stream, text}
import smithy4s.json.Json
import cats.effect.kernel.Resource
import fs2.io.process.{Processes, ProcessBuilder}
import scala.concurrent.duration.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import bsp.InitializeBuildParams
import bsp.*
import bsp.LanguageId.asBijection
import bsp.URI
import alloy.SimpleRestJson

case class ConnectionDetails(
    name: String,
    argv: List[String],
    version: String,
    bspVersion: String,
    languages: List[String]
) derives Schema

object BspTool:

  def readConnectionFile(filePath: os.Path): ConnectionDetails =
    val content = os.read(filePath)
    Json.read[ConnectionDetails](content.blob).left.map(err => new Exception(err)).fold(throw _, identity)
  end readConnectionFile

  def connectToBspServer(connectionPath: os.Path) =
    def printStream(stream: Stream[IO, Byte]) =
      stream
        .through(text.utf8.decode)
        .debug()
        .compile
        .drain
    end printStream

    val details = readConnectionFile(connectionPath)
    ProcessBuilder(details.argv.head, details.argv.tail*).spawn[IO].use { process =>
      val stE = printStream(process.stderr)

      val stO = printStream(process.stdout)

      stE.both(stO) *>
        IO(process.stdin, process.stdout, process.stderr)

    }
  end connectToBspServer

  // def connectToBspServer(details: ConnectionDetails): os.SubProcess =
  //   val process = os.proc(details.argv).spawn()
  //   val stdinThread = new Thread(() =>
  //     val stdin = process.stdin
  //     while true do
  //       val input = scala.io.StdIn.readLine()
  //       if input != null then stdin.write(input + "\n")
  //       end if
  //     end while
  //   )

  //   val stdoutThread = new Thread(() =>
  //     val stdout = process.stdout
  //     scala.io.Source.fromInputStream(stdout).getLines().foreach(println)
  //   )

  //   val stderrThread = new Thread(() =>
  //     val stderr = process.stderr
  //     scala.io.Source.fromInputStream(stderr).getLines().foreach(System.err.println)
  //   )

  //   stdinThread.start()
  //   stdoutThread.start()
  //   stderrThread.start()
  //   process
  // end connectToBspServer

  // def connectToBspServer(filePath: os.Path) =
  //   val details = readConnectionFile(filePath)
  //   val process = os.proc(details.argv).spawn()

  // end connectToBspServer

end BspTool

object BspClient extends IOApp.Simple:

  val validUri: URI = URI("file:///Users/simon/Code/helloScala")

  val params = InitializeBuildParams(
    displayName = "dairect",
    version = "0.1.0",
    bspVersion = "2.1.0",
    rootUri = validUri,
    capabilities = BuildClientCapabilities(
      languageIds = List(LanguageId("scala"))
    ),
    data = InitializeBuildParamsData(Document.nullDoc).some
  )

  // val stringParams = println(writeToString(params))
  def run: IO[Unit] =
    def hiStream = Stream
      .emits(Json.writePrettyString(params).getBytes)
      .covary[IO]
    val connectionFilePath = os.Path("/Users/simon/Code/helloScala/.bsp/scala-cli.json")
    for
      (stdin, stoud, stderr) <- BspTool.connectToBspServer(connectionFilePath)

      _ <- IO.sleep(1.second)
      _ <- hiStream.through(stdin).compile.drain

      _ <- IO.sleep(1.second)
    yield ()
    end for
  end run
end BspClient
