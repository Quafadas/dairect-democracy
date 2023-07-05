package object unions {
  type UnionService[F[_]] = smithy4s.kinds.FunctorAlgebra[UnionServiceGen, F]
  val UnionService = UnionServiceGen

  type NormalCardValue = unions.NormalCardValue.Type

}