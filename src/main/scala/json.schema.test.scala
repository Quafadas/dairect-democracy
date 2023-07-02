package weather

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.http.json.JCodec
import smithy4s.schema.Schema
import people.peopleImpl


class MySuite extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  // test("weather schema") {
  //   val generatedSchema = JsonProtocolF[IO].toJsonSchema(weatherServiceImpl)
  //   val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

  //   assertNoDiff(
  //     stringSchema,
  //     """[{"name":"GetWeather","description":"Get the weather for a city","parameters":{"type":"object","properties":{"location":{"type":"string"}}}},{"name":"GetWeatherLatLong","description":"Get the weather for a city given a latitude and longitude","parameters":{"type":"object","properties":{"lat":{"type":"number"},"long":{"type":"number"}}}},{"name":"GetWeatherLatLongPacked","description":"Get the weather for a city given a latitude and longitude, but pack the inputs together","parameters":{"type":"object","properties":{"lat":{"type":"number"},"long":{"type":"number"}}}}]"""
  //   )
  // }

  test("people schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(peopleImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    println(generatedSchema)
    assertNoDiff(
      stringSchema,
      """[]"""
    )
  }

end MySuite
