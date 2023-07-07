// package weather

// import cats.effect.IOApp
// import java.util.Base64.Decoder
// import org.http4s.EntityDecoder
// import cats.effect.IO
// import org.http4s.ember.client.EmberClientBuilder
// import org.http4s.Request

// import org.http4s.syntax.all.*
// import org.http4s.Method
// import ciris.*
// import org.http4s.Header
// import org.http4s.Headers.*
// import org.http4s.Headers
// import org.http4s.MediaType
// import org.http4s.headers.Authorization
// import org.http4s.Credentials
// import org.http4s.AuthScheme
// import javax.swing.text.AbstractDocument.Content
// import org.http4s.headers.`Content-Type`
// import org.http4s.client.middleware.Logger
// import org.http4s.client.Client
// import cats.effect.kernel.Resource
// import io.circe.*
// import io.circe.parser.*
// import org.typelevel.ci.CIStringSyntax
// import javax.print.attribute.standard.Media
// import org.http4s.circe.*
// import io.circe.literal.*
// import ujson.circe.CirceJson
// import upickle.core.LinkedHashMap
// import org.http4s.headers.Accept
// import smithy4s.*
// import smithy4s.kinds.*

// import cats.effect.unsafe.implicits.global
// import smithy4s.http.json.JCodec
// import scala.collection.mutable.ArrayBuffer
// import ujson.Value

// object WeatherTool extends IOApp.Simple:

//   implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

//   val key = env("OPEN_API_TOKEN").as[String].load[IO].toResource

//   val logger = (cIn: Client[IO]) =>
//     Logger(logBody = true, logHeaders = true, _ => false, Some((x: String) => IO.println(x)))(cIn)

//   val clientR: Resource[cats.effect.IO, Client[cats.effect.IO]] = EmberClientBuilder.default[IO].build.map(logger(_))

//   def run =
//     clientR.both(key).use { (client, token) =>

//       // Theses prompts (when I use them :-)) select the smithy service I'd expect according to ther descriptions in weather.smithy.
//       val prompt1 = "Get the weather for Zurich, Switzerland"
//       val prompt2 = "Get the weeather at lat 47.3769 and long 8.5417"
//       val prompt3 = "Get the weather at lat 47.3769 and long 8.5417, use the packed tool"

//       /*
//         Boilerplate messages for openAI. Should probably be made typesafe via smithy translate,
//         but for this POC ujson simplicity and flexibility was valuable.
//        */
//       val messages = ujson.Arr(
//         ujson.Obj(
//           "role" -> "system",
//           "content" -> "You are a helpful assistent. "
//         ),
//         ujson.Obj(
//           "role" -> "user",
//           "content" -> prompt1
//         )
//       )

//       val basicBodyAddSmithyFct = ujson.Obj(
//         "messages" -> messages,
//         "model" -> "gpt-3.5-turbo-0613",
//         "temperature" -> 0
//       )

//       /*
//         The proposal is to harden these three lines into a new "SmithyOpenAI" thing.
//         That lives . Somewhere ?
//        */
//       val testJiggy = new JsonProtocolF[IO] // Started as copty paste. See notes in that source
//       val smithyDispatcher = testJiggy.openAiFunctionDispatch(weatherServiceImpl) // SmithyOpenAi dispatcher
//       val tools = testJiggy.toJsonSchema(weatherServiceImpl) // SmithyOpenAi tool schema
//       // -----------------------------

//       // becauase we're relying on ujson, this is boilerplate for later removal .
//       val schemaString = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(tools)
//       basicBodyAddSmithyFct("functions") = ujson.read(schemaString)

//       // Beacuse http4s works easiest with circe and I can't be bothered to figure out anythign esle right now.
//       val body: Json = CirceJson(basicBodyAddSmithyFct)

//       def request: Request[IO] = Request(
//         Method.POST,
//         uri"https://api.openai.com/v1/chat/completions",
//         headers = Headers(
//           Header("Authorization", "Bearer " + token),
//           Accept(MediaType.application.json),
//           Accept(MediaType.text.plain)
//         )
//       )

//       /*
//         1. send our messages (including schema telling openAI how to use @simpleRestJson) to openAI
//         2. get back a functino call response
//         3. dispatch that function call to smithy (woot!) using dispatcher
//         4. Send back the function call response to openAI
//         5. print it's response.
//       */
//       client
//         .expect[Json](request.withEntity(body))
//         .flatMap: s =>
//           // val fctConfig = com.github.plokhotnyuk.jsoniter_scala.core.readFromString(s)
//           val raw = ujson.read(s.toString)
//           val check = raw("choices")(0)("message").objOpt.flatMap(_.get("function_call"))
//           check match
//             case Some(value) =>
//               val fctResult: IO[Document] = smithyDispatcher.apply(raw)

//               // Want to discuss what these lines should look like.
//               // I think they need to be left to the caller. I don't think it belongs in smithy,
//               // but rather some scala langchain clone
//               val fctResultJ = fctResult.map(doc =>
//                 val tmp = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(doc)
//                 ujson.read(tmp).obj
//               )

//               val sendBack = fctResultJ
//                 .map(s =>
//                   val newMessages: ArrayBuffer[Value] = messages.arr :+ raw("choices")(0)("message") :+ s
//                   ujson.Obj(
//                     "model" -> "gpt-3.5-turbo-0613",
//                     "messages" -> newMessages,
//                     "temperature" -> 0
//                   )
//                 )
//                 .flatTap(IO.println)

//               IO.println("------------------") >>
//                 sendBack.flatMap(bodyTwo => client.expect[Json](request.withEntity(CirceJson(bodyTwo)))) >>
//                 IO.println("------------------")

//             case None => IO.println("no function")
//           end match
//     }
//   end run
// end WeatherTool
