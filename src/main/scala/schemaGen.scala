package io.github.quafadas.dairect

import software.amazon.smithy.model.Model
import software.amazon.smithy.jsonschema.JsonSchemaConverter
import software.amazon.smithy.jsonschema.JsonSchemaConfig
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import software.amazon.smithy.model.shapes.*
import software.amazon.smithy.model.node.*
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.jsonschema.SchemaDocument
import scala.util.chaining.*
import software.amazon.smithy.jsonschema.Schema
import software.amazon.smithy.model.loader.ModelDiscovery
import smithy4s.dynamic.DynamicSchemaIndex
import scala.jdk.CollectionConverters.*
import software.amazon.smithy.model.Model
import smithy4s.dynamic.*
import smithy4s.deriving.API
import smithy4s.Service
import scala.util.chaining.*
// // import smithy4s.dynamic.DynamicSchemaIndex

// @main
// def testy() =

//   // def getModelForShape(modelName: String, smithyString: String) = Model
//   //   .assembler()
//   //   .addUnparsedModel(modelName, smithyString)
//   //   .assemble()
//   //   .unwrap()

//   // val modelString = """|$version: "2"
//   //                      |
//   //                      |namespace foo
//   //                      |
//   //                      |@documentation("some Foo operation")
//   //                      |operation Foo {
//   //                      |  input := {
//   //                      |    @required
//   //                      |    query: Query
//   //                      |  }
//   //                      |  output := {
//   //                      |    @required
//   //                      |    result: String
//   //                      |  }
//   //                      |}
//   //                      |
//   //                      |structure Query {
//   //                     |@documentation("a query")
//   //                      |  @required
//   //                      |  text: String
//   //                      |}
//   //                      |""".stripMargin

//   // val model = getModelForShape("foo.smithy", modelString)
//   val weatherApi = API[openai.WeatherService]
//   val unvalidatedModel = DynamicSchemaIndex.builder.addAll(Service[weatherApi.Free]).build().toSmithyModel
//   val openaiSchema = schemaFromModel(unvalidatedModel)
//   println(Node.prettyPrintJson(openaiSchema))

// end testy

object schemaFromModel:

  def apply(model: Model) =
    val (jsonSchemaConverter, jsonSchema, definitions) = prepareForInterogation(model)

    Node.fromNodes(
      model
        .getOperationShapes()
        .asScala
        .map(shape => openaiOperation(shape, model, jsonSchemaConverter, jsonSchema))
        .toSeq*
    )
  end apply

  def prepareForInterogation(model: Model) = JsonSchemaConverter
    .builder()
    .pipe { b =>
      // b.config(jsc)
      b.model(model)
      b.build()
    }
    .pipe { converter =>
      val schema = converter.convert()
      (converter, schema, schema.getDefinitions().asScala.toMap)
    }

  object InlineVisitor:
    def apply(forJsonSchema: SchemaDocument) = new InlineVisitor(forJsonSchema)
  end InlineVisitor

  // Inline the references by looking their definitions
  class InlineVisitor(val forJsonSchema: SchemaDocument) extends NodeVisitor[Node]:
    def nullNode(node: NullNode): Node = node
    def booleanNode(node: BooleanNode): Node = node
    def numberNode(node: NumberNode): Node = node
    def stringNode(node: StringNode): Node = node

    def arrayNode(node: ArrayNode): Node =
      Node.fromNodes(node.getElements().asScala.map(_.accept(this)).asJava)

    def objectNode(node: ObjectNode): Node =
      node
        .getStringMember("$ref")
        .toScala
        .flatMap { pointer =>
          forJsonSchema
            .getDefinition(pointer.getValue())
            .toScala
            .map(_.toNode().accept(this))
        }
        .getOrElse {
          Node.objectNode(
            node
              .getMembers()
              .asScala
              .toVector
              .map { case (key, value) =>
                (key, value.accept(this))
              }
              .toMap
              .asJava
          )
        }
  end InlineVisitor

  def openaiOperation(
      operationShape: OperationShape,
      model: Model,
      jsonSchemaConverter: JsonSchemaConverter,
      jsonSchema: SchemaDocument
  ): Node =
    val input: Shape = model.expectShape(operationShape.getInputShape())
    val inputNode =
      jsonSchemaConverter.convertShape(input).toNode().accept(InlineVisitor(jsonSchema))

    Node
      .objectNodeBuilder()
      .withMember("type", "function")
      .withMember(
        "function",
        Node
          .objectNodeBuilder()
          .withMember("name", operationShape.getId().getName())
          .withMember(
            "description",
            operationShape
              .getTrait(classOf[DocumentationTrait])
              .map(_.getValue())
              .toScala
              .getOrElse(operationShape.getId().getName())
          )
          .withMember("parameters", inputNode)
          .build
      )
      .build()
  end openaiOperation

end schemaFromModel
