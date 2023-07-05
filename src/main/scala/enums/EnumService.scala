package enums

import smithy4s.Endpoint
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.StreamingSchema
import smithy4s.Transformation
import smithy4s.kinds.PolyFunction5
import smithy4s.kinds.toPolyFunction5.const5

trait EnumServiceGen[F[_, _, _, _, _]] {
  self =>

  /** A description of a card from a stanard deck of playing cards */
  def placeCardPicture(faceCard: FaceCard, suit: Suit): F[PlaceCardPictureInput, Nothing, PlaceCardPictureOutput, Nothing, Nothing]
  /** Checks if a card is a face card
    * @param cardValue
    *   The face card
    * @param suit
    *   The suit
    */
  def isFaceCard(cardValue: CardValue, suit: Suit): F[PlayingCard, Nothing, IsFaceCardOutput, Nothing, Nothing]

  def transform: Transformation.PartiallyApplied[EnumServiceGen[F]] = Transformation.of[EnumServiceGen[F]](this)
}

object EnumServiceGen extends Service.Mixin[EnumServiceGen, EnumServiceOperation] {

  val id: ShapeId = ShapeId("enums", "EnumService")
  val version: String = ""

  val hints: Hints = Hints(
    alloy.SimpleRestJson(),
  )

  def apply[F[_]](implicit F: Impl[F]): F.type = F

  object ErrorAware {
    def apply[F[_, _]](implicit F: ErrorAware[F]): F.type = F
    type Default[F[+_, +_]] = Constant[smithy4s.kinds.stubs.Kind2[F]#toKind5]
  }

  val endpoints: List[smithy4s.Endpoint[EnumServiceOperation, _, _, _, _, _]] = List(
    EnumServiceOperation.PlaceCardPicture,
    EnumServiceOperation.IsFaceCard,
  )

  def endpoint[I, E, O, SI, SO](op: EnumServiceOperation[I, E, O, SI, SO]) = op.endpoint
  class Constant[P[-_, +_, +_, +_, +_]](value: P[Any, Nothing, Nothing, Nothing, Nothing]) extends EnumServiceOperation.Transformed[EnumServiceOperation, P](reified, const5(value))
  type Default[F[+_]] = Constant[smithy4s.kinds.stubs.Kind1[F]#toKind5]
  def reified: EnumServiceGen[EnumServiceOperation] = EnumServiceOperation.reified
  def mapK5[P[_, _, _, _, _], P1[_, _, _, _, _]](alg: EnumServiceGen[P], f: PolyFunction5[P, P1]): EnumServiceGen[P1] = new EnumServiceOperation.Transformed(alg, f)
  def fromPolyFunction[P[_, _, _, _, _]](f: PolyFunction5[EnumServiceOperation, P]): EnumServiceGen[P] = new EnumServiceOperation.Transformed(reified, f)
  def toPolyFunction[P[_, _, _, _, _]](impl: EnumServiceGen[P]): PolyFunction5[EnumServiceOperation, P] = EnumServiceOperation.toPolyFunction(impl)

}

sealed trait EnumServiceOperation[Input, Err, Output, StreamedInput, StreamedOutput] {
  def run[F[_, _, _, _, _]](impl: EnumServiceGen[F]): F[Input, Err, Output, StreamedInput, StreamedOutput]
  def endpoint: (Input, Endpoint[EnumServiceOperation, Input, Err, Output, StreamedInput, StreamedOutput])
}

object EnumServiceOperation {

  object reified extends EnumServiceGen[EnumServiceOperation] {
    def placeCardPicture(faceCard: FaceCard, suit: Suit) = PlaceCardPicture(PlaceCardPictureInput(faceCard, suit))
    def isFaceCard(cardValue: CardValue, suit: Suit) = IsFaceCard(PlayingCard(cardValue, suit))
  }
  class Transformed[P[_, _, _, _, _], P1[_ ,_ ,_ ,_ ,_]](alg: EnumServiceGen[P], f: PolyFunction5[P, P1]) extends EnumServiceGen[P1] {
    def placeCardPicture(faceCard: FaceCard, suit: Suit) = f[PlaceCardPictureInput, Nothing, PlaceCardPictureOutput, Nothing, Nothing](alg.placeCardPicture(faceCard, suit))
    def isFaceCard(cardValue: CardValue, suit: Suit) = f[PlayingCard, Nothing, IsFaceCardOutput, Nothing, Nothing](alg.isFaceCard(cardValue, suit))
  }

  def toPolyFunction[P[_, _, _, _, _]](impl: EnumServiceGen[P]): PolyFunction5[EnumServiceOperation, P] = new PolyFunction5[EnumServiceOperation, P] {
    def apply[I, E, O, SI, SO](op: EnumServiceOperation[I, E, O, SI, SO]): P[I, E, O, SI, SO] = op.run(impl) 
  }
  final case class PlaceCardPicture(input: PlaceCardPictureInput) extends EnumServiceOperation[PlaceCardPictureInput, Nothing, PlaceCardPictureOutput, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: EnumServiceGen[F]): F[PlaceCardPictureInput, Nothing, PlaceCardPictureOutput, Nothing, Nothing] = impl.placeCardPicture(input.faceCard, input.suit)
    def endpoint: (PlaceCardPictureInput, smithy4s.Endpoint[EnumServiceOperation,PlaceCardPictureInput, Nothing, PlaceCardPictureOutput, Nothing, Nothing]) = (input, PlaceCardPicture)
  }
  object PlaceCardPicture extends smithy4s.Endpoint[EnumServiceOperation,PlaceCardPictureInput, Nothing, PlaceCardPictureOutput, Nothing, Nothing] {
    val id: ShapeId = ShapeId("enums", "PlaceCardPicture")
    val input: Schema[PlaceCardPictureInput] = PlaceCardPictureInput.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[PlaceCardPictureOutput] = PlaceCardPictureOutput.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Documentation("A description of a card from a stanard deck of playing cards"),
      smithy.api.Http(method = smithy.api.NonEmptyString("POST"), uri = smithy.api.NonEmptyString("/playingCardDescription"), code = 200),
      smithy.api.Readonly(),
    )
    def wrap(input: PlaceCardPictureInput) = PlaceCardPicture(input)
    override val errorable: Option[Nothing] = None
  }
  final case class IsFaceCard(input: PlayingCard) extends EnumServiceOperation[PlayingCard, Nothing, IsFaceCardOutput, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: EnumServiceGen[F]): F[PlayingCard, Nothing, IsFaceCardOutput, Nothing, Nothing] = impl.isFaceCard(input.cardValue, input.suit)
    def endpoint: (PlayingCard, smithy4s.Endpoint[EnumServiceOperation,PlayingCard, Nothing, IsFaceCardOutput, Nothing, Nothing]) = (input, IsFaceCard)
  }
  object IsFaceCard extends smithy4s.Endpoint[EnumServiceOperation,PlayingCard, Nothing, IsFaceCardOutput, Nothing, Nothing] {
    val id: ShapeId = ShapeId("enums", "IsFaceCard")
    val input: Schema[PlayingCard] = PlayingCard.schema.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[IsFaceCardOutput] = IsFaceCardOutput.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Documentation("Checks if a card is a face card"),
      smithy.api.Http(method = smithy.api.NonEmptyString("GET"), uri = smithy.api.NonEmptyString("/isFaceCard/{cardValue}/{suit}"), code = 200),
      smithy.api.Readonly(),
    )
    def wrap(input: PlayingCard) = IsFaceCard(input)
    override val errorable: Option[Nothing] = None
  }
}

