package smithyOpenAI


import software.amazon.smithy.jsonschema.JsonSchemaConverter
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node
import smithy4s.ShapeId

import cats.effect.IO
import cats.Id
import smithy4s.internals.DocumentEncoder
import smithy4s.Document
import smithy4s.http.json.JCodec
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

class IntegrationSuite extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val ns = "test"

  test("weather schema") {
    val generatedSchema = JsonProtocolF[IO].toJsonSchema(weather.weatherServiceImpl)
    val stringSchema = com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)

    val shouldBe = """[{"name":"GetWeather","description":"Get the weather for a city","parameters":{"type":"object","properties":{"location":{"description":"The name of the city","type":"string"}},"required":["location"]}},{"name":"GetWeatherLatLong","description":"Get the weather for a city given a latitude and longitude","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}},"required":["lat","long"]}},{"name":"GetWeatherLatLongPacked","description":"Get the weather for a city given a latitude and longitude, but pack the inputs together","parameters":{"type":"object","properties":{"lat":{"description":"Latitude","type":"number"},"long":{"description":"Longditude","type":"number"}},"required":["lat","long"]}}]"""

    val compareStr = s"[$shouldBe, $stringSchema]"

    assertNoDiff(
      stringSchema,
      shouldBe
    )
  }

  test("def generation") {

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
    val buildModel = DynamicSchemaIndex.loadModel(myModel).toTry.get
    val schemaUnderTest = buildModel.getSchema(ShapeId(ns, shapeName)).get

    // naive headline grabbing... but perhaps not useful useful 1 liner
    val naive = Document.DObject(new JsonSchemaVisitor{}.apply(schemaUnderTest).makeWithDefs(Set[ShapeId]()))
    println(naive)
    //println(com.github.plokhotnyuk.jsoniter_scala.core.writeToString(naive)(jc) )

    val schemaforVisitor = new JsonSchemaVisitorForShape(ShapeId("test", "Company")){}
    schemaforVisitor(schemaUnderTest)
    println(schemaforVisitor.found)

    // A slower, more explanatory approach.
    // We need a way, to decide what is a def, and what is not.

    println("------AWS Version-------------")
    println("defs")
    println(awsSmithyCompleteSchema(ns, smithy))
    println(" ")
    println("Shape ")
    println(awsSmithyToSchema(ns, smithy, shapeName))
    println("------END AWS Version-------------")


    val shapeCounts = new ShapeCountSchemaVisitor{}
    shapeCounts(schemaUnderTest)
    val countResult = shapeCounts.getCounts
    println(countResult)

    val proposedDefs = countResult
      .filterNot((shape, _) => shape.namespace == "smithy.api" )
      .filter((_, count) => count > 1 )
      .keySet

    println(proposedDefs)

    println("smithy defs")
    println(makeDefs(proposedDefs, schemaUnderTest))


    // The below mechanism implicitly represents an "eject" possibility. If we've totally borked something up,
    // then one can always make up the defs (some other way) and use those definitions instead.
    // The generation here simply respects what it's told on that front...
    val schemaVisitor = new JsonSchemaVisitor {}
    val internalRep = schemaVisitor(schemaUnderTest)
    val generatedJsonSchema: Document = Document.DObject(internalRep.makeWithDefs(proposedDefs))
    println(generatedJsonSchema)
    println(com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedJsonSchema)(jc) )

    // Now our generated schemna respects the defs, but we don't have any defs defined right now!

    val x = 1

    assert(false)

  }



end IntegrationSuite
