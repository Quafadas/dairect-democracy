package weather

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.http.json.JCodec
import smithy4s.schema.Schema
import people.peopleImpl
import enums.enumServiceImpl
import union.unionServiceImpl


// TODO
class IntegrationSuite extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  test("weather schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(weatherServiceImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    assertNoDiff(
      stringSchema,
      """[{"name":"GetWeather","description":"Get the weather for a city","parameters":{"type":"object","properties":{"location":{"description":"The name of the city","type":"string"}}}},{"name":"GetWeatherLatLong","description":"Get the weather for a city given a latitude and longitude","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}}}},{"name":"GetWeatherLatLongPacked","description":"Get the weather for a city given a latitude and longitude, but pack the inputs together","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}}}}]"""
    )
  }

  test("people schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(peopleImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    println(generatedSchema)
    assertNoDiff(
      stringSchema,
      """[{"name":"GetPerson","description":"Get the information about a person","parameters":{"type":"object","properties":{"id":{"description":"The id of the person","type":"string","format":"uuid"}}}},{"name":"FamilyTreeDepth","description":"Find number of childeren at each depth of the family tree","parameters":{"type":"object","properties":{"id":{"description":"The id of this person","type":"string","format":"uuid"},"mother":{"$ref":"#"},"father":{"$ref":"#"},"childeren":{"description":"Childeren of this person","type":"array","items":{"$ref":"#"}}}}},{"name":"GetChilderen","description":"Find number of childeren at each depth of the family tree","parameters":{"type":"object","properties":{"id":{"description":"The id of this person","type":"string","format":"uuid"},"mother":{"$ref":"#"},"father":{"$ref":"#"},"childeren":{"description":"Childeren of this person","type":"array","items":{"$ref":"#"}}}}}]"""
    )
  }

  test("enum schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(enumServiceImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    println(generatedSchema)
    assertNoDiff(
      stringSchema,
      """[{"name":"PlaceCardPicture","description":"A description of a card from a stanard deck of playing cards","parameters":{"type":"object","properties":{"faceCard":{"enum":[11,12,13,1,0]},"suit":{"enum":["DIAMOND","CLUB","HEART","SPADE"]}}}},{"name":"IsFaceCard","description":"Checks if a card is a face card","parameters":{"type":"object","properties":{"cardValue":{"description":"The face card","type":"integer"},"suit":{"description":"The suit","enum":["DIAMOND","CLUB","HEART","SPADE"]}}}}]"""
    )
  }

  test("union schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(unionServiceImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    println(generatedSchema)
    assertNoDiff(
      stringSchema,
      """[{"name":"PlaceCardDescription","description":"A description of a card from a stanard deck of playing cards","parameters":{"type":"object","properties":{"value":{"type":"object","properties":{"cardValue":{"oneOf":[{"n":{"type":"integer"}},{"f":{"enum":[11,12,13,1,0]}}]}}}}}},{"name":"PlaceCardDescriptionUntagged","description":"A description of a card from a stanard deck of playing cards, but untagged","parameters":{"type":"object","properties":{"value":{"type":"object","properties":{"cardValue":{"oneOf":[{"type":"integer"},{"enum":[11,12,13,1,0]}]}}}}}}]"""
    )
  }

end MySuite
