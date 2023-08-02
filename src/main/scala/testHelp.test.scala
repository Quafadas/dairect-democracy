
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

def smithy4sToSchema(ns: String, smithy: String, shape: String, defsOpt: Option[Set[ShapeId]] = None)(using jcd: JCodec[Document]) =
  val myModel = toModel(ns, smithy)
  val schemaUnderTest = DynamicSchemaIndex.loadModel(myModel).toTry.get
  val mySchema = schemaUnderTest.getSchema(ShapeId(ns, shape)).get

  val jsVisitor = new JsonSchemaVisitor {}
  val defs = defsOpt.getOrElse(Set[ShapeId]())
  val generatedSchema: Document = Document.DObject(jsVisitor(mySchema).makeWithDefs(defs))
  com.github.plokhotnyuk.jsoniter_scala.core.writeToString(generatedSchema)
end smithy4sToSchema


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

def awsSmithyCompleteSchema(ns: String, smithy: String) =
  val r = toModel(ns, smithy)
  val amazonSchema = Node.printJson(
    JsonSchemaConverter
      .builder()
      .model(r)
      .build()
      .convert()
      .toNode()
  )
  amazonSchema
end awsSmithyCompleteSchema