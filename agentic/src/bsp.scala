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
import ch.epfl.scala.bsp.Bsp4s
import ch.epfl.scala.bsp.InitializeBuildParams
import ch.epfl.scala.bsp.InitializeBuildParams.codec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import jsonrpc4s.RawJson
import ch.epfl.scala.bsp.endpoints.Build
import jsonrpc4s.RpcActions
import scala.concurrent.Future
import jsonrpc4s.Response
import monix.execution.Ack
import jsonrpc4s.RpcResponse
import monix.eval.Task
import jsonrpc4s.RpcSuccess

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

  val compileSomething = Bsp4s

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

  import monix.execution.Scheduler.Implicits.global
  monix.eval.TaskLift.toAnyLiftIO[cats.effect.IO](cats.effect.LiftIO.ioLiftIO, monix.eval.Task.catsEffect())

  val params = InitializeBuildParams(
    displayName = "dairect",
    version = "0.1.0",
    bspVersion = "2.1.0",
    rootUri = ch.epfl.scala.bsp.Uri("file:///Users/simon/Code/helloScala"),
    capabilities = ch.epfl.scala.bsp.BuildClientCapabilities(
      languageIds = List("scala")
    ),
    dataKind = None,
    data = Some(
      RawJson("""{
    "javaSemanticdbVersion": "0.10.0",
    "semanticdbVersion": "4.9.9",
    "supportedScalaVersions": [
      "2.13.14"
    ],
    "enableBestEffortMode": false
  }""".getBytes)
    )
  )

  // Define the necessary methods here

  val init = Build.initialize

  val stringParams = println(writeToString(params))
  def run: IO[Unit] =
    def hiStream = Stream
      .emits(writeToString(params).getBytes)
      .covary[IO]
    val connectionFilePath = os.Path("/Users/simon/Code/helloScala/.bsp/scala-cli.json")
    for
      (stdin, stoud, stderr) <- BspTool.connectToBspServer(connectionFilePath)
      actions = Fs2RpcActions(stdin, stoud, stderr)
      _ <- IO.sleep(1.second)
      _ <- hiStream.through(stdin).compile.drain
      _ <- init.request(params)(using actions).to[IO]
      _ <- IO.sleep(1.second)
    yield ()
    end for
  end run
end BspClient

class Fs2RpcActions(
    stdin: fs2.Pipe[IO, Byte, Unit],
    stdout: Stream[IO, Byte],
    stderr: Stream[IO, Byte]
) extends RpcActions:
  import cats.effect.unsafe.implicits.global

  override def serverRespond(response: Response): Future[Ack] =
    println(response.jsonrpc.getBytes())

    Future.successful(Ack.Continue)
  end serverRespond

  override def clientRespond(response: Response): Unit =
    println(response.jsonrpc.getBytes())
  end clientRespond

  override def notify[A](
      endpoint: jsonrpc4s.Endpoint[A, Unit],
      notification: A,
      headers: Map[String, String]
  ): Future[Ack] =
    val notificationString = s"Endpoint: $endpoint\nNotification: $notification\nHeaders: $headers\n"
    println(notificationString)
    Future.successful(Ack.Continue)
  end notify

  override def request[A, B](
      endpoint: jsonrpc4s.Endpoint[A, B],
      request: A,
      headers: Map[String, String]
  ): Task[RpcResponse[B]] =
    val requestString = s"Endpoint: $endpoint\nRequest: $request\nHeaders: $headers\n"
    Stream
      .emits(requestString.getBytes)
      .covary[IO]
      .through(stdin)
      .compile
      .drain
      .unsafeRunSync()

    Task(
      RpcSuccess[B](
        underlying = ???,
        value = ???
      )
    )

  end request
end Fs2RpcActions
