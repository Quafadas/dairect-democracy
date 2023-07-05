package object enums {
  type EnumService[F[_]] = smithy4s.kinds.FunctorAlgebra[EnumServiceGen, F]
  val EnumService = EnumServiceGen

  type CardValue = enums.CardValue.Type

}