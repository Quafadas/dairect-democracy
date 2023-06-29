package weather

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.http.json.JCodec
import smithy4s.schema.Schema



class MySuite extends munit.FunSuite:
  test("hello"):
    //val default = JsonProtocolF[IO].toJsonSchema(DefaultSchemaVisitor)
    implicit val jc : JCodec[Document] = JCodec.fromSchema(Schema.document)

    val generatedSchema = JsonProtocolF[IO].toJsonSchema(weatherServiceImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    assertNoDiff(
      stringSchema,
      """[{"name":"GetWeather","description":"Get the weather for a city","parameters":{"type":"object","properties":{"location":{"type":"string"}}}}]"""
    )

end MySuite
