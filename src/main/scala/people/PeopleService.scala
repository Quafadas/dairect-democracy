package people

import smithy4s.Endpoint
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.StreamingSchema
import smithy4s.Transformation
import smithy4s.kinds.PolyFunction5
import smithy4s.kinds.toPolyFunction5.const5
import smithy4s.Errorable

trait PeopleServiceGen[F[_, _, _, _, _]] {
  self =>

  /** Get the information about a person
    * @param id
    *   The id of the person
    */
  def getPerson(id: String): F[GetPersonInput, Nothing, GetPersonOutput, Nothing, Nothing]

  def transform: Transformation.PartiallyApplied[PeopleServiceGen[F]] = Transformation.of[PeopleServiceGen[F]](this)
}

object PeopleServiceGen extends Service.Mixin[PeopleServiceGen, PeopleServiceOperation] {

  val id: ShapeId = ShapeId("people", "PeopleService")
  val version: String = ""

  val hints: Hints = Hints(
    alloy.SimpleRestJson(),
  )

  def apply[F[_]](implicit F: Impl[F]): F.type = F

  object ErrorAware {
    def apply[F[_, _]](implicit F: ErrorAware[F]): F.type = F
    type Default[F[+_, +_]] = Constant[smithy4s.kinds.stubs.Kind2[F]#toKind5]
  }

  val endpoints: List[smithy4s.Endpoint[PeopleServiceOperation, _, _, _, _, _]] = List(
    PeopleServiceOperation.GetPerson,
  )

  def endpoint[I, E, O, SI, SO](op: PeopleServiceOperation[I, E, O, SI, SO]) = op.endpoint
  class Constant[P[-_, +_, +_, +_, +_]](value: P[Any, Nothing, Nothing, Nothing, Nothing]) extends PeopleServiceOperation.Transformed[PeopleServiceOperation, P](reified, const5(value))
  type Default[F[+_]] = Constant[smithy4s.kinds.stubs.Kind1[F]#toKind5]
  def reified: PeopleServiceGen[PeopleServiceOperation] = PeopleServiceOperation.reified
  def mapK5[P[_, _, _, _, _], P1[_, _, _, _, _]](alg: PeopleServiceGen[P], f: PolyFunction5[P, P1]): PeopleServiceGen[P1] = new PeopleServiceOperation.Transformed(alg, f)
  def fromPolyFunction[P[_, _, _, _, _]](f: PolyFunction5[PeopleServiceOperation, P]): PeopleServiceGen[P] = new PeopleServiceOperation.Transformed(reified, f)
  def toPolyFunction[P[_, _, _, _, _]](impl: PeopleServiceGen[P]): PolyFunction5[PeopleServiceOperation, P] = PeopleServiceOperation.toPolyFunction(impl)

}

sealed trait PeopleServiceOperation[Input, Err, Output, StreamedInput, StreamedOutput] {
  def run[F[_, _, _, _, _]](impl: PeopleServiceGen[F]): F[Input, Err, Output, StreamedInput, StreamedOutput]
  def endpoint: (Input, Endpoint[PeopleServiceOperation, Input, Err, Output, StreamedInput, StreamedOutput])
}

object PeopleServiceOperation {

  object reified extends PeopleServiceGen[PeopleServiceOperation] {
    def getPerson(id: String) = GetPerson(GetPersonInput(id))
  }
  class Transformed[P[_, _, _, _, _], P1[_ ,_ ,_ ,_ ,_]](alg: PeopleServiceGen[P], f: PolyFunction5[P, P1]) extends PeopleServiceGen[P1] {
    def getPerson(id: String) = f[GetPersonInput, Nothing, GetPersonOutput, Nothing, Nothing](alg.getPerson(id))
  }

  def toPolyFunction[P[_, _, _, _, _]](impl: PeopleServiceGen[P]): PolyFunction5[PeopleServiceOperation, P] = new PolyFunction5[PeopleServiceOperation, P] {
    def apply[I, E, O, SI, SO](op: PeopleServiceOperation[I, E, O, SI, SO]): P[I, E, O, SI, SO] = op.run(impl)
  }
  case class GetPerson(input: GetPersonInput) extends PeopleServiceOperation[GetPersonInput, Nothing, GetPersonOutput, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: PeopleServiceGen[F]): F[GetPersonInput, Nothing, GetPersonOutput, Nothing, Nothing] = impl.getPerson(input.id)
    def endpoint: (GetPersonInput, smithy4s.Endpoint[PeopleServiceOperation,GetPersonInput, Nothing, GetPersonOutput, Nothing, Nothing]) = (input, GetPerson)
  }
  object GetPerson extends smithy4s.Endpoint[PeopleServiceOperation,GetPersonInput, Nothing, GetPersonOutput, Nothing, Nothing] {

    override def errorable: Option[Errorable[Nothing]] = None

    val id: ShapeId = ShapeId("people", "GetPerson")
    val input: Schema[GetPersonInput] = GetPersonInput.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[GetPersonOutput] = GetPersonOutput.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Documentation("Get the information about a person"),
      smithy.api.Http(method = smithy.api.NonEmptyString("GET"), uri = smithy.api.NonEmptyString("/people/{id}"), code = 200),
      smithy.api.Readonly(),
    )
    def wrap(input: GetPersonInput) = GetPerson(input)
  }
}
