package smithyOpenAI

import software.amazon.smithy.jsonschema.JsonSchemaConverter
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node
import smithy4s.ShapeId

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.schema.Schema

//import smithy4s.dynamic.DynamicSchemaIndex
import smithy4s.Schema
import smithy4s.dynamic.DynamicSchemaIndex
import java.net.URL
import smithy4s.schema.Field
import smithy4s.schema.Alt
import smithy4s.Refinement
import smithy4s.Hints
import smithy4s.schema.EnumValue
import smithy4s.Bijection
import smithy4s.schema.EnumTag
import smithy4s.schema.Alt.Dispatcher
import smithy4s.schema.Primitive
import smithy4s.Lazy
import smithy4s.schema.CollectionTag
import java.awt.Shape
import smithy4s.json.Json
import weather.WeatherService
import smithy4s.deriving.API

class IntegrationSuite extends munit.FunSuite:

  val ns = "test"

  test("weather schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(API[WeatherService].liftService(weather.weatherImpl))
    val stringSchema = Json.writeDocumentAsPrettyString(generatedSchema)

    val shouldBe =
      """[{"name":"GetWeather","description":"Get the weather for a city","parameters":{"type":"object","properties":{"location":{"description":"The name of the city","type":"string"}},"required":["location"]}},{"name":"GetWeatherLatLong","description":"Get the weather for a city given a latitude and longitude","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}},"required":["lat","long"]}},{"name":"GetWeatherLatLongPacked","description":"Get the weather for a city given a latitude and longitude, but pack the inputs together","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}},"required":["lat","long"]}}]"""

    val compareStr = s"[$shouldBe, $stringSchema]"

    assertNoDiff(
      stringSchema,
      shouldBe
    )
  }

  test("example generation") {

    val shapeName = "Game" // Change this \\ Game, Person, Company, Eye, Location
    val smithy = s"""namespace $ns
        |
        |string Country
        |
        | structure Game{
        | n: String,
        | with: Person
        | at: Location
        |}
        |
        |structure Person {
        |    spouse: Person,
        |    children: People,
        |    employer: Company,
        |    @required s: String,
        |    e1: Eye,
        |    e2: Eye,
        |    birthLoc: Country,
        |    loc:Country
        |
        |
        |}
        |
        |structure Company {
        |    name: String,
        |    employees: People,
        |    headquarters: Location,
        |    i: Integer
        |}
        |
        |list People {
        |    member: Person
        |}
        |
        |structure Eye {
        |   side: String,
        |   colour: String
        |}
        |
        |structure Location {
        |    country: Country,
        |    company: Company,
        |
        |}
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val buildModel = DynamicSchemaIndex.loadModel(myModel)
    val schemaUnderTest = buildModel.getSchema(ShapeId(ns, shapeName)).get

    // An explanatory approach. Could this be packaged up and PR'ed?
    // Firstly, let's see what AWS things JSON schema looks like.

    val awsDefs = awsSmithyCompleteSchema(ns, smithy)
    val awsShape = awsSmithyToSchema(ns, smithy, shapeName)

    println("------AWS Version-------------")
    println("defs")
    println(awsDefs)
    println(" ")
    println("Shape ")
    println(awsShape)
    println("------END AWS Version-------------")

    // For ourselves, we need a way, to know what are defs, and what are not.
    // We assume (!), that in the first instance, all structs are defs.
    val structFinder = FindStructsVisitor()
    structFinder(schemaUnderTest)
    val proposedDefs = structFinder.getStructs

    // Now that we have a proposal for our defs... let's see what it looks like.
    // It'll accept any Set of ShapeIds
    val defs = makeDefs(proposedDefs, schemaUnderTest)
    val defsJson = Json.writeDocumentAsPrettyString(defs)

    val schemaVisitor = new JsonSchemaVisitor {}
    val internalRep = schemaVisitor(schemaUnderTest)
    val generatedJsonSchema: Document = Document.DObject(internalRep.makeWithDefs(proposedDefs))

    println("------Smithy4s Version-------------")
    println("defs")
    println(defsJson)
    println(" ")
    println("Shape ")
    println(Json.writeDocumentAsPrettyString((generatedJsonSchema)))
    println("------END Smithy4s Version-------------")

    // Compare the generated defs with the AWS defs
    val smithyDefsParsed = io.circe.parser.parse(defsJson)
    val awsDefsParsed = io.circe.parser.parse(awsDefs)
    val defCompStr = s"[$awsDefs, $defsJson]"
    println(defCompStr)
    assertEquals(awsDefsParsed, smithyDefsParsed)

    // Compare the generated shape with the AWS shape
    val smithyShapeParsed =
      io.circe.parser.parse(Json.writeDocumentAsPrettyString(generatedJsonSchema))
    val awsShapeParsed = io.circe.parser.parse(awsShape)
    val shapeCompStr = s"[$awsShapeParsed, $smithyShapeParsed]"
    println(defCompStr)
    assertEquals(awsDefsParsed, smithyDefsParsed)

    // naive headline grabbing... but perhaps not useful useful 1 liner
    // val naive = Document.DObject(new JsonSchemaVisitor{}.apply(schemaUnderTest).makeWithDefs(Set[ShapeId]()))
    // println(naive)
    // println(com.github.plokhotnyuk.jsoniter_scala.core.writeToString(naive)(jc) )

  }

end IntegrationSuite
