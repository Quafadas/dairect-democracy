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

object Main extends IOApp.Simple:

  val key = env("OPEN_API_TOKEN").as[String].load[IO].toResource

  // Generate this from `smithyGPTool`
  val toolInfo = ujson.read("""[
        {
            "name": "get_current_weather",
            "description": "Get the current weather in a given location",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA"
                    },
                    "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
                },
                "required": ["location"]
            }
        }
    ]""")

  val messages = ujson.Arr(
      ujson.Obj(
        "role" -> "system",
        "content" -> "You are a helpful assistent. "
      ),
      ujson.Obj(
        "role" -> "user",
        "content" -> "Tell me the forecast for today, I'm in Zurich switzerland."
      )
    )

  val basicBody = ujson.Obj(
    "messages" -> messages,
    "model" -> "gpt-3.5-turbo-0613",
    "temperature" -> 0,
    "functions" -> toolInfo
  )

  val logger = (cIn: Client[IO]) =>
    Logger(logBody = true, logHeaders = true, _ => false, Some((x: String) => IO.println(x)))(cIn)
  val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] = EmberClientBuilder.default[IO].build.map(logger(_))

  def run =
    clientR.both(key).use { (client, token) =>

      val body: Json = CirceJson(basicBody)
      def request: Request[IO] = Request(
        Method.POST,
        uri"https://api.openai.com/v1/chat/completions",
        headers = Headers(
          Header("Authorization", "Bearer " + token),
          Accept(MediaType.application.json),
          Accept(MediaType.text.plain),
        )
      )

      val tmp = client
        .expect[Json](request.withEntity(body))
        .flatMap: s => 
          val raw = ujson.read(s.toString)
          val check = raw("choices")(0)("message").objOpt.flatMap(_.get("function_call"))
          check match
            case Some(value) => 
              // dispatch to `smithyGPT` here
              println(value.toString())
              val fakeFunctResult = ujson.Obj(
                // it is seriously weird, that it needs the JSON ... encoded as a string. Quite the footgun. 
                "content" -> ujson.Str("""{"location" : "Zurich, Switerland", "unit" : "celsius", "temperature" : "27", "forecast" : ["sunny", "windy"]}"""),
                "name" -> "get_current_weather",
                "role" -> "function"
              )

              val newMessages = messages.arr :+ raw("choices")(0)("message") :+ fakeFunctResult
              
              
              // In realz, recursive, but for now, just do it once
              val bodyTwo = ujson.Obj(
                "model" -> "gpt-3.5-turbo-0613",
                "messages" -> newMessages,
                "temperature" -> 0
              )
              println(ujson.write(bodyTwo))
              IO.println("------------------") >>
              client.expect[Json](request.withEntity(CirceJson(bodyTwo))) >>
              IO.println("------------------")
              
            case None => IO.println("no function")
          
      tmp
    }
  end run
end Main
