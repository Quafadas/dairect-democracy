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
import java.awt.Shape

class FindStructsSuite extends munit.FunSuite:

  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  val ns = "test"

  test("find structs") {

    val shapeName = "Foo"
    val shapeName2 = "Bar"
    val shapeName3 = "Baz"
    val shapeName4 = "Qux"
    val shapeName5 = "Quax"
    val shapeName6 = "Quaxx"

    val smithy = s"""namespace $ns
        |
        |structure $shapeName {f1: $shapeName, f2: $shapeName2}
        |
        |structure $shapeName2 {s:String, f3: $shapeName3}
        |
        |structure $shapeName3 {f5: MyUnion, a:Age}
        |
        |union MyUnion {
        |  i: AgeMap
        |  s: AList
        |  u: $shapeName4
        |}
        |
        |list AList {
        |  member: $shapeName5
        |}
        |structure $shapeName4 {f5: Integer}
        |structure $shapeName5 {f5: Integer}
        |structure $shapeName6 {f5: Integer}
        |
        |map AgeMap {
        |  key: String
        |  value: $shapeName6
        |}
        |
        |integer Age
        |
        |""".stripMargin

    val myModel = toModel(ns, smithy)
    val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get // .getSchema(ShapeId(ns, "Foo"))
    val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shapeName)).get
    val findStructs = FindStructsVisitor()
    findStructs(mySchema)
    val found = findStructs.getStructs.toList.sortBy(_.name)

    val shouldBew = List(shapeName, shapeName2, shapeName3, shapeName4, shapeName5, shapeName6).map(ShapeId(ns, _)).sortBy(_.name)

    assertEquals(found, shouldBew)

  }


end FindStructsSuite
