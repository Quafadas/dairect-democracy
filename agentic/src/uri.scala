package ch.epfl.scala.bsp

import java.net.URI

final case class Uri private[Uri] (val value: String):
  def toPath: java.nio.file.Path =
    java.nio.file.Paths.get(new java.net.URI(value))
end Uri

object Uri:
  // This is the only valid way to create a URI
  def apply(u: URI): Uri = Uri(u.toString)

end Uri
