package weather

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
import people.peopleImpl
import enums.enumServiceImpl
import union.unionServiceImpl
//import smithy4s.dynamic.DynamicSchemaIndex
import smithy4s.Schema
import smithy4s.dynamic.DynamicSchemaIndex

class AwsSuite extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  def toModel(ns: String, smithy: String) = Model
    .assembler()
    .addUnparsedModel(
      s"$ns.smithy",
      smithy
    )
    .assemble()
    .unwrap()

  def awsSmithyToSchema(ns: String, smithy: String, shape: String) =
    val r = toModel(ns, smithy)
    val amazonSchema = Node.printJson(
      JsonSchemaConverter
        .builder()
        .model(r)
        .build()
        .convertShape(
          r.expectShape(software.amazon.smithy.model.shapes.ShapeId.from(s"$ns#$shape"))
        )
        .toNode()
    )
    amazonSchema
  end awsSmithyToSchema

  def smithy4sToSchema(ns: String, smithy: String, shape: String) =
    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get // .getSchema(ShapeId(ns, "Foo"))
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shape)).get

    val jsVisitor = new JsonSchemaVisitor {}

    val generatedSchema: Document = Document.DObject(jsVisitor(mySchema).make)
    com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)
  end smithy4sToSchema

  test("aws simple struct schema") {

    val ns = "test"
    val smithy = """namespace test
        |
        |structure Foo { @required s: String }
        |""".stripMargin

    val awsVersion = awsSmithyToSchema(ns, smithy, "Foo")

    val smithy4sVersion = smithy4sToSchema(ns, smithy, "Foo")

    val smithyParsed = io.circe.parser.parse(smithy4sVersion)
    val awsParsed = io.circe.parser.parse(awsVersion)

    assertEquals(awsParsed, smithyParsed)
  }

  test("aws simple enum schema") {

    val ns = "test"
    val smithy = """$version: "2"
        |namespace test
        |
        |enum Suit {
        |   CLUB = "club"
        |    DIAMOND = "diamond"
        |    HEART = "heart"
        |    SPADE = "spade"
        |}
        |""".stripMargin

    val awsVersion = awsSmithyToSchema(ns, smithy, "Suit")

    val smithy4sVersion = smithy4sToSchema(ns, smithy, "Suit")
    val truncatedSmithyVersion = smithy4sVersion.tail.dropRight(1)
    println(smithy4sVersion)
    assert(awsVersion.contains(truncatedSmithyVersion))
  }

  test("aws docs hints") {

    val ns = "test"
    val smithy = """$version: "2"
        |namespace test
        |
        |@documentation("A latitude and longitude")
        |structure LatLong {
        |    @documentation("Latitude") @httpLabel @required lat: Double,
        |    @documentation("Longditude") @httpLabel @required long: Double
        |}
        |""".stripMargin

    val awsVersion = awsSmithyToSchema(ns, smithy, "LatLong")

    val smithy4sVersion = smithy4sToSchema(ns, smithy, "LatLong")

    val smithyParsed = io.circe.parser.parse(smithy4sVersion)
    val awsParsed = io.circe.parser.parse(awsVersion)

    assertEquals(smithyParsed, awsParsed)
  }

  test("recursive definitions ") {

    val ns = "test"
    val smithy = """$version: "2"
        |namespace test
        |
        |string PersonId
        |
        |
        |structure Person {
        |    @documentation("The id of this person") @required id: PersonId,
        |    mother: Person,
        |    father: Person,
        |    @documentation("Childeren of this person") childeren: People
        |}
        |
        |list People {
        |    member: Person
        |}
        |
        |
        |""".stripMargin

    val awsVersion = awsSmithyToSchema(ns, smithy, "Person")

    val smithy4sVersion = smithy4sToSchema(ns, smithy, "Person")
    val smithyParsed = io.circe.parser.parse(smithy4sVersion)
    // TODO : defs?
    // It's interesting, that AWS resulits are not self-contained.
    val awsParsed = io.circe.parser.parse(awsVersion.replace("/definitions/Person", ""))

    assertEquals(smithyParsed, awsParsed)

  }

  test("defaults and simple types") {
    val ns = "test"
    val smithy = """$version: "2"
        |namespace test
        |list StringList {
        |  member: String
        |}
        |
        |map DefaultStringMap {
        |  key: String
        |  value: String
        |}
        |
        |structure DefaultTest {
        |  one: Integer = 1
        |  two: String = "test"
        |  three: StringList = []
        |  @default
        |  four: StringList
        |  @default
        |  five: String
        |  @default
        |  six: Integer
        |  @default
        |  seven: Document
        |  eight: Document
        |  @default
        |  nine: Short
        |  @default
        |  ten: Double
        |  @default
        |  eleven: Float
        |  @default
        |  twelve: Long
        |  @default
        |  thirteen: Timestamp
        |  @default
        |  @timestampFormat("http-date")
        |  fourteen: Timestamp
        |  @default
        |  @timestampFormat("date-time")
        |  fifteen: Timestamp
        |  @default
        |  sixteen: Byte
        |  @default
        |  eighteen: Boolean
        |}
        |
        |""".stripMargin

    val awsVersion = awsSmithyToSchema(ns, smithy, "DefaultTest")
    // TODO - is this a bug in smithy4s? Default int should be 1 and not 1.0
    val smithy4sVersion = smithy4sToSchema(ns, smithy, "DefaultTest").replace("1.0", "1")

    val smithyParsed = io.circe.parser.parse(smithy4sVersion)
    val awsParsed = io.circe.parser.parse(awsVersion)

    //val str = s"[$awsVersion, $smithy4sVersion]"

    assertEquals(smithyParsed, awsParsed)

  }

  test("aws map") {

    val ns = "test"
    val smithy = """$version: "2"
        |namespace test
        |
        |map IntegerMap {
        |    key: String
        |    value: Integer
        |}
        |
        |""".stripMargin

    val awsVersion = awsSmithyToSchema(ns, smithy, "IntegerMap")

    val smithy4sVersion = smithy4sToSchema(ns, smithy, "IntegerMap")

    val smithyParsed = io.circe.parser.parse(smithy4sVersion)
    val awsParsed = io.circe.parser.parse(awsVersion)

    val str = s"[$awsVersion, $smithy4sVersion]"

    assertEquals(smithyParsed, awsParsed)
  }

end AwsSuite
