package smithyOpenAI

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.http.json.JCodec
import smithy4s.schema.Schema


class IntegrationSuite extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  test("weather schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(weatherServiceImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    val shouldBe = """[{"name":"GetWeather","description":"Get the weather for a city","parameters":{"type":"object","properties":{"location":{"description":"The name of the city","type":"string"}},"required":["location"]}},{"name":"GetWeatherLatLong","description":"Get the weather for a city given a latitude and longitude","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}},"required":["lat","long"]}},{"name":"GetWeatherLatLongPacked","description":"Get the weather for a city given a latitude and longitude, but pack the inputs together","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}},"required":["lat","long"]}}]"""

    val compareStr = s"[$shouldBe, $stringSchema]"

    assertNoDiff(
      stringSchema,
      shouldBe
    )
  }

end IntegrationSuite
