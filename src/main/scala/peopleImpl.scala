package people

import cats.effect.*
import java.util.UUID

object peopleImpl extends PeopleService[IO]:

  val fakePersonId = UUID.fromString("00000000-0000-0000-0000-000000000000")
  override def getPerson(id: String): IO[GetPersonOutput] = IO.pure(
    GetPersonOutput(
      Person(
        PersonId(fakePersonId),
        None
      )
    )
  )

end peopleImpl
