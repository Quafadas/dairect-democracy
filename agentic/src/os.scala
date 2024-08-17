package io.github.quafadas.dairect

import scala.annotation.experimental
import smithy4s.*
import smithy4s.deriving.{*, given}

import cats.effect.IO
import cats.effect.std.Console

@experimental
@hints(smithy.api.Documentation("Local file and os operations"))
/** Local file and os operations
  */
trait OsTool derives API:
  /** Creates a temporary directory on the local file system.
    */
  def makeTempDir(dirPrefix: String): IO[String] =
    IO.println("Creating a temporary directory") >>
      IO.blocking {
        val outDir = os.temp.dir(deleteOnExit = false, prefix = dirPrefix).toString
        outDir.toString
      }

  def createOrOverwriteFileInDir(dir: String, fileName: String, contents: Option[String]): IO[String] =
    IO.println(s"Creating a file in $dir") >>
      IO.blocking {
        val filePath = os.Path(dir) / fileName
        os.write.over(filePath, contents.getOrElse(""))
        filePath.toString
      }

  def askForHelp(question: String): IO[String] =
    for
      _ <- Console[IO].println(s"I need guidance with: $question")
      n <- Console[IO].readLine
    yield n

end OsTool
