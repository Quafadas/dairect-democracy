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

class CountShapeIds extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val ns = "test"

  def toModel(ns: String, smithy: String) = Model
    .assembler()
    .addUnparsedModel(
      s"$ns.smithy",
      smithy
    )
    .assemble()
    .unwrap()

  test("count simple struct") {
    val shapeName = "Foo"
    val shapeName2 = "DoubleFoo"
    val smithy = s"""namespace $ns
        |
        |structure $shapeName { @required s: String }
        |
        |structure $shapeName2 {f1: $shapeName, f2: $shapeName}
        |
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get // .getSchema(ShapeId(ns, "Foo"))
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName2)).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)

    println(countVisitor.getCounts)

    val foo = ShapeId("test", shapeName)
    val doubleFoo = ShapeId("test", shapeName2)

    assert(countVisitor.getCounts(foo) == 2.0)
    assert(countVisitor.getCounts(doubleFoo) == 1.0)
  }

  test("count enum") {
    val smithy = s"""$$version: "2"
        |namespace $ns
        |
        |enum Suit {
        |   CLUB = "club"
        |    DIAMOND = "diamond"
        |    HEART = "heart"
        |    SPADE = "spade"
        |}
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get // .getSchema(ShapeId(ns, "Foo"))
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, "Suit")).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)
    val foo = ShapeId("test", "Suit")

    assert(countVisitor.getCounts(foo) == 1.0)
  }

  test("docs hints") {
    val shapeName = "LatLong"
    val smithy = s"""$$version: "2"
        |namespace $ns
        |
        |@documentation("A latitude and longitude")
        |structure $shapeName {
        |    @documentation("Latitude") @httpLabel @required lat: Double,
        |    @documentation("Longditude") @httpLabel @required long: Double
        |}
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get // .getSchema(ShapeId(ns, "Foo"))
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName)).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)
    val foo = ShapeId("test", shapeName)

    assert(countVisitor.getCounts(foo) == 1.0)
  }

  test("recursive definitions ") {
    val shapeName = "Person"
    val smithy = s"""$$version: "2"
        |namespace $ns
        |
        |string PersonId
        |
        |
        |structure $shapeName {
        |    @documentation("The id of this person") @required id: PersonId,
        |    mother: $shapeName,
        |    father: $shapeName,
        |    @documentation("Childeren of this person") childeren: People
        |}
        |
        |list People {
        |    member: $shapeName
        |}
        |
        |
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get // .getSchema(ShapeId(ns, "Foo"))
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName)).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)
    println(countVisitor.getCounts)
    val foo = ShapeId("test", shapeName)

    assert(countVisitor.getCounts(foo) == Double.PositiveInfinity)

  }

  test("map") {
    val shapeName = "MyMap"
    val smithy = s"""$$version: "2"
        |namespace $ns
        |
        |map $shapeName {
        |    key: String
        |    value: Integer
        |}
        |
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName)).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)
    val foo = ShapeId("test", shapeName)
    val s = ShapeId("smithy.api", "String")
    val i = ShapeId("smithy.api", "Integer")
    assert(countVisitor.getCounts(foo) == 1.0)
    assert(countVisitor.getCounts(s) == 1.0)
    assert(countVisitor.getCounts(i) == 1.0)
  }


  test("Something messy") {

    val shapeName = "Person"
    val smithy = s"""namespace $ns
        |
        |string Country
        |
        |structure Person {
        |    spouse: Person,
        |    children: People,
        |    employer: Company,
        |    @required s: String,
        |    @required e: Eyes,
        |    birthLoc: Country
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
        |structure Eyes {
        |   leftColour: String,
        |   rightColour: String
        |   @range(min: 0, max: 2)
        |   @required count : Integer
        |}
        |
        |structure Location {
        |    country: Country,
        |    company: Company,
        |
        |}
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName)).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)
    val foo = ShapeId("test", shapeName)
    val s = ShapeId("smithy.api", "String")
    val i = ShapeId("smithy.api", "Integer")
    val sl = ShapeId("test", "Country")
    val s2 = ShapeId("test", "Eyes")
    assert(countVisitor.getCounts(foo).isInfinite())
    assert(countVisitor.getCounts(s) == 3.0)
    assert(countVisitor.getCounts(i) == 1.0)
    assert(countVisitor.getCounts(sl) == 1.0)
    assert(countVisitor.getCounts(s2) == 1.0)

  }

  test("tagged union") {
    val shapeName = "MyUnion"
    val smithy = s"""$$version: "2"
        |namespace $ns
        |
        |list StringList {
        |  member: String
        |}
        |
        |union $shapeName {
        |    i32: Integer,
        |
        |    string: String,
        |
        |    time: Timestamp,
        |
        |    slist: StringList,
        |}
        |
        |""".stripMargin
    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName)).get
    val countVisitor = new ShapeCountSchemaVisitor {}
    countVisitor(mySchema)
    val foo = ShapeId("test", shapeName)
    val s = ShapeId("smithy.api", "String")
    val i = ShapeId("smithy.api", "Integer")
    val sl = ShapeId("test", "StringList")
    assert(countVisitor.getCounts(foo) == 1.0)
    assert(countVisitor.getCounts(s) == 2.0)
    assert(countVisitor.getCounts(i) == 1.0)
    assert(countVisitor.getCounts(sl) == 1.0)
  }

end CountShapeIds
