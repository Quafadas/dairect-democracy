package people

import cats.effect.*
import cats.syntax.option.*
import java.util.UUID

object peopleImpl extends PeopleService[IO]:

  def familyTreeDepth(
      id: people.PersonId,
      mother: Option[people.Person],
      father: Option[people.Person],
      childeren: Option[List[people.Person]]
  ): cats.effect.IO[people.FamilyTreeDepthOutput] = IO.pure(
    FamilyTreeDepthOutput(
      0.some
    )
  )

  def getChilderen(
      id: people.PersonId,
      mother: Option[people.Person],
      father: Option[people.Person],
      childeren: Option[List[people.Person]]
  ): cats.effect.IO[people.GetChilderenOutput] = IO.pure(
    GetChilderenOutput(
      childeren
    )
  )

  val fakePersonId = UUID.fromString("00000000-0000-0000-0000-000000000000")
  override def getPerson(id: PersonId): IO[GetPersonOutput] = IO.pure(
    GetPersonOutput(
      Person(
        PersonId(fakePersonId),
        None
      )
    )
  )

end peopleImpl
