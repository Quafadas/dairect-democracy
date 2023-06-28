package weather

import cats.effect.IOApp
import java.util.Base64.Decoder
import org.http4s.EntityDecoder
import cats.effect.IO
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Request

import org.http4s.syntax.all.*
import org.http4s.Method
import ciris.*
import org.http4s.Header
import org.http4s.Headers.*
import org.http4s.Headers
import org.http4s.MediaType
import org.http4s.headers.Authorization
import org.http4s.Credentials
import org.http4s.AuthScheme
import javax.swing.text.AbstractDocument.Content
import org.http4s.headers.`Content-Type`
import org.http4s.client.middleware.Logger
import org.http4s.client.Client
import cats.effect.kernel.Resource
import io.circe.*
import io.circe.parser.*
import org.typelevel.ci.CIStringSyntax
import javax.print.attribute.standard.Media
import org.http4s.circe.*
import io.circe.literal.*
import ujson.circe.CirceJson
import upickle.core.LinkedHashMap
import org.http4s.headers.Accept
import smithy4s.*
import smithy4s.kinds.*

import cats.effect.unsafe.implicits.global
import smithy4s.http.json.JCodec
import scala.collection.mutable.ArrayBuffer
import ujson.Value

object Main extends IOApp.Simple:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val key = env("OPEN_API_TOKEN").as[String].load[IO].toResource

  val logger = (cIn: Client[IO]) =>
    Logger(logBody = true, logHeaders = true, _ => false, Some((x: String) => IO.println(x)))(cIn)

  val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] = EmberClientBuilder.default[IO].build.map(logger(_))

  def run =
    clientR.both(key).use { (client, token) =>

      val testJiggy = new JsonProtocolF[IO]
      val smithyDispatcher = testJiggy.openAiFunctionDispatch(weatherServiceImpl)

      val body: Json = CirceJson(basicBody)
      def request: Request[IO] = Request(
        Method.POST,
        uri"https://api.openai.com/v1/chat/completions",
        headers = Headers(
          Header("Authorization", "Bearer " + token),
          Accept(MediaType.application.json),
          Accept(MediaType.text.plain)
        )
      )

      val tmp = client
        .expect[Json](request.withEntity(body))
        .flatMap: s =>
          // val fctConfig = com.github.plokhotnyuk.jsoniter_scala.core.readFromString(s)
          val raw = ujson.read(s.toString)
          val check = raw("choices")(0)("message").objOpt.flatMap(_.get("function_call"))
          check match
            case Some(value) =>
              // dispatch to `smithyGPT` here
              // We need to construct this document
              // val callService = smithyDispatcher.apply(Document.DObject(
              //   Map[String, Document]("GetWeather" -> Document.DObject( Map[String, Document]("location" -> Document.fromString("hi") ) )))).flatMap{ doc =>
              //   IO.println(doc)
              // }

              // val fctConfig = com.github.plokhotnyuk.jsoniter_scala.core.readFromString(value.toString)
              println("OpenAI function call")
              // println(fctConfig)
              val fctRestult = smithyDispatcher
                .apply(raw)
                .map(doc =>
                  ujson.Obj(
                    // it is seriously weird, that it needs the JSON ... encoded as a string. Quite the footgun.
                    "content" -> ujson.Str(com.github.plokhotnyuk.jsoniter_scala.core.writeToString(doc)),
                    "name" -> "GetWeather",
                    "role" -> "function"
                  )
                )

              val sendBack = functResultJson
                .map(s =>
                  val newMessages: ArrayBuffer[Value] = messages.arr :+ raw("choices")(0)("message") :+ s
                  ujson.Obj(
                    "model" -> "gpt-3.5-turbo-0613",
                    "messages" -> newMessages,
                    "temperature" -> 0
                  )
                )
                .flatTap(IO.println)

              // val newMessages = messages.arr :+ raw("choices")(0)("message")

              // // In realz, recursive, but for now, just do it once
              // val bodyTwo = ujson.Obj(
              //   "model" -> "gpt-3.5-turbo-0613",
              //   "messages" -> newMessages,
              //   "temperature" -> 0
              // )
              // println(ujson.write(bodyTwo))
              // callService >>
              IO.println("------------------") >>
                sendBack.flatMap(bodyTwo => client.expect[Json](request.withEntity(CirceJson(bodyTwo)))) >>
                IO.println("------------------")

            case None => IO.println("no function")
          end match

      tmp
    }
  end run
end Main
