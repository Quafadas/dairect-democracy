package io.github.quafadas.dairect

import cats.effect.IO
import fs2.Stream
import fs2.io.file.Flags
import org.http4s.client.Client
import org.http4s.client.middleware.Logger

val filesIo = fs2.io.file.Files[IO]

val printLogger = (cIn: Client[IO]) =>
  Logger(
    logBody = true,
    logHeaders = false,
    name => name.toString.toLowerCase.contains("token"),
    Some((x: String) => IO.println(x))
  )(cIn)

def makeLogFile(toFile: fs2.io.file.Path): IO[Unit] =
  IO.println(s"Making log file at $toFile") >>
    filesIo
      .isRegularFile(toFile)
      .flatMap { exists =>
        if exists then filesIo.delete(toFile) else IO.unit
      } >>
    fs2.io.file.Files[IO].createFile(toFile)

def fileLogger(toFile: fs2.io.file.Path): (Client[IO] => Client[IO]) = (cIn: Client[IO]) =>
  val writer = filesIo.writeUtf8Lines(toFile, Flags.Append)
  Logger(
    logBody = true,
    logHeaders = false,
    name => name.toString.toLowerCase.contains("token"),
    logAction = Some((x: String) =>
      writer(
        Stream.emit[IO, String](x).append(Stream.emit[IO, String]("\n ------------------- \n"))
      ).compile.drain
    )
  )(cIn)
