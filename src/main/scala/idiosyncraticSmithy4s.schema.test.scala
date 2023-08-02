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

class IdiosyncraticSmithy4s extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val ns = "test"

  def toModel(ns: String, smithy: String) = Model
    .assembler()
    .addUnparsedModel(
      s"$ns.smithy",
      smithy
    )
    .discoverModels()
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


  def singleShapeEquivalence(name: String, smithySpec: String) =
    val awsVersion = awsSmithyToSchema(ns, smithySpec, name)
    val smithy4sVersion = smithy4sToSchema(ns, smithySpec, name)
    val smithyParsed = io.circe.parser.parse(smithy4sVersion)
    val awsParsed = io.circe.parser.parse(awsVersion)
    // Paste str into a text editor for debugging
    val str = s"[$awsVersion, $smithy4sVersion]"
    assertEquals(smithyParsed, awsParsed)

  end singleShapeEquivalence

  test("simple struct") {
    val shapeName = "FooUntaggedUnion"
    val smithy = s"""namespace $ns
        |
        |use alloy#untagged
        |
        |@untagged
        |union $shapeName {
        |    i32: Integer,
        |
        |    string: String,
        |
        |    time: Timestamp,
        |
        |}
        |""".stripMargin

    val smithy4sSchema = smithy4sToSchema(ns, smithy, shapeName)

    assert(false)
  }


end IdiosyncraticSmithy4s
