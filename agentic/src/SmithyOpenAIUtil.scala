package io.github.quafadas.dairect

import cats.MonadThrow
import cats.syntax.all.*
import smithy4s.*
import smithy4s.Document.*
import smithy4s.dynamic.DynamicSchemaIndex
import smithy4s.json.Json
import smithy4s.kinds.*
import smithy4s.schema.*
import software.amazon.smithy.model.node.Node

// format: off

// val ioToolGen = new SmithyOpenAIUtil[IO]

extension [Alg[_[_, _, _, _, _]], F[_]](alg: FunctorAlgebra[Alg, F])(using F: MonadThrow[F])

  def assistantTools(using S: Service[Alg]): List[AssistantTool] =
    val allfcts = alg.toJsonSchema
    allfcts.asInstanceOf[Document.DArray].value.toIndexedSeq.map( fct =>
      AssistantTool.function(
        fct.asInstanceOf[Document.DObject].value.get("function").get.decode[AssistantToolFunction].failFast
      )
    ).toList
  end assistantTools


  def toJsonSchema(using S: Service[Alg])  =
    val unvalidatedModel = DynamicSchemaIndex.builder.addAll(S).build().toSmithyModel
    val docOpt = Json.readDocument(Node.prettyPrintJson(schemaFromModel(unvalidatedModel)))
    docOpt.toOption.get
  end toJsonSchema

  def dispatcher(implicit S: Service[Alg]): FunctionCall => F[Document] =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val jsonEndpoints =
      S.endpoints.map(ep => ep.name -> toLowLevel(transformation, ep)).toMap

    (m: FunctionCall) =>
      smithy4s.json.Json.readDocument(m.arguments.get) match
        case Left(error) =>
          F.raiseError(new Throwable(s"Failed to parse arguments for $m .\n Error: $error"))
        case Right(fctConfig) =>
          jsonEndpoints.get(m.name) match
            case Some(jsonEndpoint) =>
              jsonEndpoint(fctConfig)
            case None => F.raiseError(new Throwable(s"Function $m not found"))
      end match
  end dispatcher

// format: off
  private def toLowLevel[Op[_, _, _, _, _], I, E, O, SI, SO](
      polyFunction: PolyFunction5[Op, Kind1[F]#toKind5],
      endpoint: Endpoint[Op, I, E, O, SI, SO]
  ): Document => F[Document] =
    given decoderI: Decoder[I] = Document.Decoder.fromSchema(endpoint.input)
    given encoderO: Encoder[O] = Document.Encoder.fromSchema(endpoint.output)
    given encoderE: Document.Encoder[E] =
      endpoint.errorschema match
        case Some(errorableE) =>
          Document.Encoder.fromSchema(errorableE.schema)
        case None =>
          new Document.Encoder[E]:
            def encode(e: E): Document = Document.DNull
    (document: Document) =>
      // println(Json.writeDocumentAsPrettyString(document))
      for
        input <- document.decode[I].liftTo[F]
        op = endpoint.wrap(input)
        output <- (polyFunction(op): F[O])
          .map { in =>
            encoderO.encode(in) match
              case Document.DObject(fields) => fields.get("value").getOrElse(???)
              case _                        => ???

          }
          .recover { case endpoint.Error((_, e)) =>
            Document.obj("error" -> encoderE.encode(e))
          }
      yield
      // println(Json.writeDocumentAsPrettyString(output))
      output
  end toLowLevel
end extension

// format: off
class SmithyOpenAIUtil[F[_]](using F: MonadThrow[F]):

  // def assistantTool[Alg[_[_, _, _, _, _]]](
  //     alg: FunctorAlgebra[Alg, F]
  // )(implicit S: Service[Alg]) =
  //   val schema = toJsonSchema(alg)
  //   println(schema)
  //   AssistantTool.function(
  //   function = AssistantToolFunction(
  //     name = alg.toJsonSchema
  //   )
  // )

  @deprecated("use the extension method")
  def toJsonSchema[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): Document =
    val unvalidatedModel = DynamicSchemaIndex.builder.addAll(S).build().toSmithyModel
    val docOpt = Json.readDocument(Node.prettyPrintJson(schemaFromModel(unvalidatedModel)))
    docOpt.toOption.get
  end toJsonSchema

  @deprecated("use the extension method")
  def openAiSmithyFunctionDispatch[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): FunctionCall => F[Document] =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val jsonEndpoints =
      S.endpoints.map(ep => ep.name -> toLowLevel(transformation, ep)).toMap

    (m: FunctionCall) =>
      smithy4s.json.Json.readDocument(m.arguments.get) match
        case Left(error) =>
          F.raiseError(new Throwable(s"Failed to parse arguments for $m .\n Error: $error"))
        case Right(fctConfig) =>
          jsonEndpoints.get(m.name) match
            case Some(jsonEndpoint) =>
              jsonEndpoint(fctConfig)
            case None => F.raiseError(new Throwable(s"Function $m not found"))
      end match
  end openAiSmithyFunctionDispatch



  private def toLowLevel[Op[_, _, _, _, _], I, E, O, SI, SO](
      polyFunction: PolyFunction5[Op, Kind1[F]#toKind5],
      endpoint: Endpoint[Op, I, E, O, SI, SO]
  ): Document => F[Document] =
    given decoderI: Decoder[I] = Document.Decoder.fromSchema(endpoint.input)
    given encoderO: Encoder[O] = Document.Encoder.fromSchema(endpoint.output)
    given encoderE: Document.Encoder[E] =
      endpoint.errorschema match
        case Some(errorableE) =>
          Document.Encoder.fromSchema(errorableE.schema)
        case None =>
          new Document.Encoder[E]:
            def encode(e: E): Document = Document.DNull
    (document: Document) =>
      // println(Json.writeDocumentAsPrettyString(document))
      for
        input <- document.decode[I].liftTo[F]
        op = endpoint.wrap(input)
        output <- (polyFunction(op): F[O])
          .map { in =>
            encoderO.encode(in) match
              case Document.DObject(fields) => fields.get("value").getOrElse(???)
              case _                        => ???

          }
          .recover { case endpoint.Error((_, e)) =>
            Document.obj("error" -> encoderE.encode(e))
          }
      yield
      // println(Json.writeDocumentAsPrettyString(output))
      output
  end toLowLevel

end SmithyOpenAIUtil
// format: on
