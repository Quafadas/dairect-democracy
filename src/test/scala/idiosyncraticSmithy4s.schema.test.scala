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

  test("Untagged unions") {
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
