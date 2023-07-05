package unions

import smithy4s.Endpoint
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.StreamingSchema
import smithy4s.Transformation
import smithy4s.kinds.PolyFunction5
import smithy4s.kinds.toPolyFunction5.const5

trait UnionServiceGen[F[_, _, _, _, _]] {
  self =>

  /** A description of a card from a stanard deck of playing cards */
  def placeCardDescription(value: PlayingCard): F[PlaceCardDescriptionInput, Nothing, PlaceCardDescriptionOutput, Nothing, Nothing]
  /** A description of a card from a stanard deck of playing cards, but untagged */
  def placeCardDescriptionUntagged(value: UntaggedPlayingCard): F[PlaceCardDescriptionUntaggedInput, Nothing, PlaceCardDescriptionUntaggedOutput, Nothing, Nothing]

  def transform: Transformation.PartiallyApplied[UnionServiceGen[F]] = Transformation.of[UnionServiceGen[F]](this)
}

object UnionServiceGen extends Service.Mixin[UnionServiceGen, UnionServiceOperation] {

  val id: ShapeId = ShapeId("unions", "UnionService")
  val version: String = ""

  val hints: Hints = Hints(
    alloy.SimpleRestJson(),
  )

  def apply[F[_]](implicit F: Impl[F]): F.type = F

  object ErrorAware {
    def apply[F[_, _]](implicit F: ErrorAware[F]): F.type = F
    type Default[F[+_, +_]] = Constant[smithy4s.kinds.stubs.Kind2[F]#toKind5]
  }

  val endpoints: List[smithy4s.Endpoint[UnionServiceOperation, _, _, _, _, _]] = List(
    UnionServiceOperation.PlaceCardDescription,
    UnionServiceOperation.PlaceCardDescriptionUntagged,
  )

  def endpoint[I, E, O, SI, SO](op: UnionServiceOperation[I, E, O, SI, SO]) = op.endpoint
  class Constant[P[-_, +_, +_, +_, +_]](value: P[Any, Nothing, Nothing, Nothing, Nothing]) extends UnionServiceOperation.Transformed[UnionServiceOperation, P](reified, const5(value))
  type Default[F[+_]] = Constant[smithy4s.kinds.stubs.Kind1[F]#toKind5]
  def reified: UnionServiceGen[UnionServiceOperation] = UnionServiceOperation.reified
  def mapK5[P[_, _, _, _, _], P1[_, _, _, _, _]](alg: UnionServiceGen[P], f: PolyFunction5[P, P1]): UnionServiceGen[P1] = new UnionServiceOperation.Transformed(alg, f)
  def fromPolyFunction[P[_, _, _, _, _]](f: PolyFunction5[UnionServiceOperation, P]): UnionServiceGen[P] = new UnionServiceOperation.Transformed(reified, f)
  def toPolyFunction[P[_, _, _, _, _]](impl: UnionServiceGen[P]): PolyFunction5[UnionServiceOperation, P] = UnionServiceOperation.toPolyFunction(impl)

}

sealed trait UnionServiceOperation[Input, Err, Output, StreamedInput, StreamedOutput] {
  def run[F[_, _, _, _, _]](impl: UnionServiceGen[F]): F[Input, Err, Output, StreamedInput, StreamedOutput]
  def endpoint: (Input, Endpoint[UnionServiceOperation, Input, Err, Output, StreamedInput, StreamedOutput])
}

object UnionServiceOperation {

  object reified extends UnionServiceGen[UnionServiceOperation] {
    def placeCardDescription(value: PlayingCard) = PlaceCardDescription(PlaceCardDescriptionInput(value))
    def placeCardDescriptionUntagged(value: UntaggedPlayingCard) = PlaceCardDescriptionUntagged(PlaceCardDescriptionUntaggedInput(value))
  }
  class Transformed[P[_, _, _, _, _], P1[_ ,_ ,_ ,_ ,_]](alg: UnionServiceGen[P], f: PolyFunction5[P, P1]) extends UnionServiceGen[P1] {
    def placeCardDescription(value: PlayingCard) = f[PlaceCardDescriptionInput, Nothing, PlaceCardDescriptionOutput, Nothing, Nothing](alg.placeCardDescription(value))
    def placeCardDescriptionUntagged(value: UntaggedPlayingCard) = f[PlaceCardDescriptionUntaggedInput, Nothing, PlaceCardDescriptionUntaggedOutput, Nothing, Nothing](alg.placeCardDescriptionUntagged(value))
  }

  def toPolyFunction[P[_, _, _, _, _]](impl: UnionServiceGen[P]): PolyFunction5[UnionServiceOperation, P] = new PolyFunction5[UnionServiceOperation, P] {
    def apply[I, E, O, SI, SO](op: UnionServiceOperation[I, E, O, SI, SO]): P[I, E, O, SI, SO] = op.run(impl) 
  }
  final case class PlaceCardDescription(input: PlaceCardDescriptionInput) extends UnionServiceOperation[PlaceCardDescriptionInput, Nothing, PlaceCardDescriptionOutput, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: UnionServiceGen[F]): F[PlaceCardDescriptionInput, Nothing, PlaceCardDescriptionOutput, Nothing, Nothing] = impl.placeCardDescription(input.value)
    def endpoint: (PlaceCardDescriptionInput, smithy4s.Endpoint[UnionServiceOperation,PlaceCardDescriptionInput, Nothing, PlaceCardDescriptionOutput, Nothing, Nothing]) = (input, PlaceCardDescription)
  }
  object PlaceCardDescription extends smithy4s.Endpoint[UnionServiceOperation,PlaceCardDescriptionInput, Nothing, PlaceCardDescriptionOutput, Nothing, Nothing] {
    val id: ShapeId = ShapeId("unions", "PlaceCardDescription")
    val input: Schema[PlaceCardDescriptionInput] = PlaceCardDescriptionInput.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[PlaceCardDescriptionOutput] = PlaceCardDescriptionOutput.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Documentation("A description of a card from a stanard deck of playing cards"),
      smithy.api.Http(method = smithy.api.NonEmptyString("POST"), uri = smithy.api.NonEmptyString("/playingCardDescription"), code = 200),
      smithy.api.Readonly(),
    )
    def wrap(input: PlaceCardDescriptionInput) = PlaceCardDescription(input)
    override val errorable: Option[Nothing] = None
  }
  final case class PlaceCardDescriptionUntagged(input: PlaceCardDescriptionUntaggedInput) extends UnionServiceOperation[PlaceCardDescriptionUntaggedInput, Nothing, PlaceCardDescriptionUntaggedOutput, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: UnionServiceGen[F]): F[PlaceCardDescriptionUntaggedInput, Nothing, PlaceCardDescriptionUntaggedOutput, Nothing, Nothing] = impl.placeCardDescriptionUntagged(input.value)
    def endpoint: (PlaceCardDescriptionUntaggedInput, smithy4s.Endpoint[UnionServiceOperation,PlaceCardDescriptionUntaggedInput, Nothing, PlaceCardDescriptionUntaggedOutput, Nothing, Nothing]) = (input, PlaceCardDescriptionUntagged)
  }
  object PlaceCardDescriptionUntagged extends smithy4s.Endpoint[UnionServiceOperation,PlaceCardDescriptionUntaggedInput, Nothing, PlaceCardDescriptionUntaggedOutput, Nothing, Nothing] {
    val id: ShapeId = ShapeId("unions", "PlaceCardDescriptionUntagged")
    val input: Schema[PlaceCardDescriptionUntaggedInput] = PlaceCardDescriptionUntaggedInput.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[PlaceCardDescriptionUntaggedOutput] = PlaceCardDescriptionUntaggedOutput.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Documentation("A description of a card from a stanard deck of playing cards, but untagged"),
      smithy.api.Http(method = smithy.api.NonEmptyString("POST"), uri = smithy.api.NonEmptyString("/playingCardDescriptionUntagged"), code = 200),
      smithy.api.Readonly(),
    )
    def wrap(input: PlaceCardDescriptionUntaggedInput) = PlaceCardDescriptionUntagged(input)
    override val errorable: Option[Nothing] = None
  }
}

