/*
 *  Copyright 2021-2022 Disney Streaming
 *
 *  Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://disneystreaming.github.io/TOST-1.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Aggressively copy - pasted from
// https://github.com/disneystreaming/smithy4s/blob/5902d2fd81d1eee6703daaab6e2ad95eb69684ab/modules/test-utils/src/smithy4s/tests/DefaultSchemaVisitor.scala#L4

package weather

import smithy4s.*
import smithy4s.Document.*

import cats.syntax.all.*
import cats.MonadThrow
import smithy4s.kinds.*
import cats.Applicative

import smithy4s.schema.*
import smithy4s.schema.Primitive.*
import cats.Id
import java.util.UUID
import io.circe.Json
import smithy4s.http.json.JCodec

/** These are toy interpreters that turn services into json-in/json-out functions, and vice versa.
  *
  * Created for testing purposes.
  */
class JsonProtocolF[F[_]](implicit F: MonadThrow[F]):

  // needed for document parsing
  implicit val jc: JCodec[Document] = JCodec.fromSchema(Schema.document)

  def dummy[Alg[_[_, _, _, _, _]]](
      service: Service[Alg]
  ): Document => F[Document] =
    implicit val S: Service[Alg] = service
    toJsonF[Alg](DummyService[F].create[Alg])
  end dummy

  def redactingProxy[Alg[_[_, _, _, _, _]]](
      jsonF: Document => F[Document],
      service: Service[Alg]
  ): Document => F[Document] =
    implicit val S: Service[Alg] = service.service
    toJsonF[Alg](fromJsonF[Alg](jsonF)) andThen (_.map(redact))
  end redactingProxy

  def redact(document: Document): Document = document match
    case DString("sensitive") => DString("*****")
    case DArray(array)        => DArray(array.map(redact))
    case DObject(map)         => DObject(map.fmap(redact))
    case other                => other

  def fromJsonF[Alg[_[_, _, _, _, _]]](
      jsonF: Document => F[Document]
  )(implicit S: Service[Alg]): S.Impl[F] = fromLowLevel(S)(jsonF)

  def toJsonF[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): Document => F[Document] =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val jsonEndpoints =
      S.endpoints.map(ep => ep.name -> toLowLevel(transformation, ep)).toMap
    (d: Document) =>
      d match
        case Document.DObject(m) if m.size == 1 =>
          val (method, payload) = m.head
          jsonEndpoints.get(method) match
            case Some(jsonEndpoint) => jsonEndpoint(payload)
            case None               => F.raiseError(NotFound)
          end match
        case _ => F.raiseError(NotFound)
  end toJsonF

  def openAiFunctionDispatch[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): ujson.Value => F[Document] =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val jsonEndpoints =
      S.endpoints.map(ep => ep.name -> toLowLevel(transformation, ep)).toMap

    (d: ujson.Value) =>
      val start = d("choices")(0)("message").objOpt.flatMap(_.get("function_call"))
      val endpointName = start.map(jsonRaw => jsonRaw("name").str)
      val arguments = start.map(jsonRaw => ujson.read(jsonRaw("arguments").str))
      println(arguments)
      (endpointName, arguments) match
        case (Some(epName), Some(args)) =>
          println(args)
          val fctConfig: Document = com.github.plokhotnyuk.jsoniter_scala.core.readFromString(args.toString)
          println(fctConfig)
          val ep = jsonEndpoints.get(epName)
          ep match
            case Some(jsonEndpoint) => jsonEndpoint(fctConfig)
            case None               => F.raiseError(NotFound)
          end match

        case _ => F.raiseError(NotFound)
      end match
  end openAiFunctionDispatch

  def extractDocHint(hints: Hints): Map[String, Document] =
    hints
      .get(smithy.api.Documentation)
      .map(desc => Map("description" -> Document.fromString(desc.toString())))
      .getOrElse(Map.empty[String, Document])
  end extractDocHint

  def toJsonSchema[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): Document =
    val transformation = S.toPolyFunction[Kind1[F]#toKind5](alg)
    val serviceName = S.id.name
    val hints = S.service.hints
    val docHint = hints.get(smithy.api.Documentation)

    val s: IndexedSeq[Document] = S.endpoints
      .map((ep: Endpoint[S.Operation, ?, ?, ?, ?, ?]) =>
        val hints = ep.hints.get(smithy.api.Documentation)
        val docHint = ep.hints.get(smithy.api.Documentation)
        val description = docHint
          .map(desc => Map("description" -> Document.fromString(desc.toString())))
          .getOrElse(Map.empty[String, Document])

        val epDesc = Map[String, Document](
          "name" -> Document.fromString(ep.name)
        ) ++ description
        val endpointfields = ep.input.compile(new JsonSchemaVisitor {})
        println(endpointfields)
        val schema = epDesc ++ endpointfields.make

        Document.DObject(schema)
      )
      .toIndexedSeq
    Document.DArray(s)
  end toJsonSchema

  def stringSchema[Alg[_[_, _, _, _, _]]](
      alg: FunctorAlgebra[Alg, F]
  )(implicit S: Service[Alg]): String =
    val asSchema = toJsonSchema(alg)
    "hello"
  end stringSchema

  private def fromLowLevel[Alg[_[_, _, _, _, _]]](service: Service[Alg])(
      jsonF: Document => F[Document]
  ): service.Impl[F] = service.impl {
    new service.FunctorEndpointCompiler[F]:
      def apply[I, E, O, SI, SO](
          ep: service.Endpoint[I, E, O, SI, SO]
      ): I => F[O] =
        implicit val encoderI: Document.Encoder[I] =
          Document.Encoder.fromSchema(ep.input)
        val decoderO: Document.Decoder[O] =
          Document.Decoder.fromSchema(ep.output)

        val decoderE: Document.Decoder[F[Nothing]] =
          ep.errorable match
            case Some(errorableE) =>
              Document.Decoder
                .fromSchema(errorableE.error)
                .map(e => F.raiseError(errorableE.unliftError(e)))
            case None =>
              new Document.Decoder[F[Nothing]]:
                def decode(
                    document: Document
                ): Either[smithy4s.http.PayloadError, F[Nothing]] =
                  Right(
                    F.raiseError(
                      smithy4s.http
                        .PayloadError(PayloadPath.root, "Nothing", "Nothing")
                    )
                  )
        implicit val decoderFoutput = new Document.Decoder[F[O]]:
          def decode(
              document: Document
          ): Either[smithy4s.http.PayloadError, F[O]] =
            document match
              case Document.DObject(map) if (map.contains("error")) =>
                decoderE.decode(map("error")).map(_.asInstanceOf[F[O]])
              case other => decoderO.decode(other).map(F.pure(_))

        (i: I) =>
          jsonF(Document.obj(ep.name -> Document.encode(i)))
            .flatMap(_.decode[F[O]].liftTo[F].flatten)
      end apply
  }

  private def toLowLevel[Op[_, _, _, _, _], I, E, O, SI, SO](
      polyFunction: PolyFunction5[Op, Kind1[F]#toKind5],
      endpoint: Endpoint[Op, I, E, O, SI, SO]
  ): Document => F[Document] =
    implicit val decoderI = Document.Decoder.fromSchema(endpoint.input)
    implicit val encoderO = Document.Encoder.fromSchema(endpoint.output)
    implicit val encoderE: Document.Encoder[E] =
      endpoint.errorable match
        case Some(errorableE) =>
          Document.Encoder.fromSchema(errorableE.error)
        case None =>
          new Document.Encoder[E]:
            def encode(e: E): Document = Document.DNull
    (document: Document) =>
      for
        input <- document.decode[I].liftTo[F]
        op = endpoint.wrap(input)
        output <- (polyFunction(op): F[O]).map(encoderO.encode).recover { case endpoint.Error((_, e)) =>
          Document.obj("error" -> encoderE.encode(e))
        }
      yield output
  end toLowLevel

  case object NotFound extends Throwable
end JsonProtocolF

object DummyService:

  def apply[F[_]]: PartiallyApplied[F] = new PartiallyApplied[F]

  class PartiallyApplied[F[_]]:
    def create[Alg[_[_, _, _, _, _]]](implicit
        service: Service[Alg],
        F: Applicative[F]
    ): FunctorAlgebra[Alg, F] =
      type Op[I, E, O, SI, SO] = service.Operation[I, E, O, SI, SO]
      service.fromPolyFunction[Kind1[F]#toKind5] {
        service.opToEndpoint.andThen[Kind1[F]#toKind5](
          new PolyFunction5[service.Endpoint, Kind1[F]#toKind5]:
            def apply[I, E, O, SI, SO](
                ep: Endpoint[Op, I, E, O, SI, SO]
            ): F[O] =
              F.pure(ep.output.compile(DefaultSchemaVisitor))
        )
      }
    end create
  end PartiallyApplied
end DummyService

/*
 *  Copyright 2021-2022 Disney Streaming
 *
 *  Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://disneystreaming.github.io/TOST-1.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

object DefaultSchemaVisitor extends SchemaVisitor[Id]:

  override def primitive[P](
      shapeId: ShapeId,
      hints: Hints,
      tag: Primitive[P]
  ): Id[P] = tag match
    case PFloat      => 0: Float
    case PBigDecimal => 0: BigDecimal
    case PBigInt     => 0: BigInt
    case PBlob       => ByteArray(Array.emptyByteArray)
    case PDocument   => Document.DNull
    case PByte       => 0: Byte
    case PInt        => 0
    case PShort      => 0: Short
    case PString     => ""
    case PLong       => 0: Long
    case PDouble     => 0: Double
    case PBoolean    => true
    case PTimestamp  => Timestamp(0L, 0)
    case PUUID       => new UUID(0, 0)

  override def collection[C[_], A](
      shapeId: ShapeId,
      hints: Hints,
      tag: CollectionTag[C],
      member: Schema[A]
  ): Id[C[A]] = tag.empty

  override def map[K, V](
      shapeId: ShapeId,
      hints: Hints,
      key: Schema[K],
      value: Schema[V]
  ): Id[Map[K, V]] = Map.empty

  override def enumeration[E](
      shapeId: ShapeId,
      hints: Hints,
      tag: EnumTag,
      values: List[EnumValue[E]],
      total: E => EnumValue[E]
  ): Id[E] = values.head.value

  override def struct[S](
      shapeId: ShapeId,
      hints: Hints,
      fields: Vector[SchemaField[S, ?]],
      make: IndexedSeq[Any] => S
  ): Id[S] = make(fields.map(_.fold(new Field.Folder[Schema, S, Any]:
    def onRequired[A](label: String, instance: Schema[A], get: S => A): Any =
      apply(instance)

    def onOptional[A](
        label: String,
        instance: Schema[A],
        get: S => Option[A]
    ): Any =
      None
  )))

  override def union[U](
      shapeId: ShapeId,
      hints: Hints,
      alternatives: Vector[SchemaAlt[U, ?]],
      dispatch: Alt.Dispatcher[Schema, U]
  ): Id[U] =
    def processAlt[A](alt: Alt[Schema, U, A]) = alt.inject(apply(alt.instance))
    processAlt(alternatives.head)
  end union

  override def biject[A, B](
      schema: Schema[A],
      bijection: Bijection[A, B]
  ): Id[B] = bijection(apply(schema))

  override def refine[A, B](
      schema: Schema[A],
      refinement: Refinement[A, B]
  ): Id[B] = refinement.unsafe(apply(schema))

  override def lazily[A](suspend: Lazy[Schema[A]]): Id[A] = ???

  override def nullable[A](schema: Schema[A]): Id[Option[A]] = None
end DefaultSchemaVisitor
