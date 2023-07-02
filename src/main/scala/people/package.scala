package object people {
  type PeopleService[F[_]] = smithy4s.kinds.FunctorAlgebra[PeopleServiceGen, F]
  val PeopleService = PeopleServiceGen

  type PersonId = people.PersonId.Type
  type People = people.People.Type

}