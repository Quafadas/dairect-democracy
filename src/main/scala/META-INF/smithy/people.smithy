$version: "2"

namespace people

use alloy#simpleRestJson
use alloy#uuidFormat
use alloy#UUID

@simpleRestJson
service PeopleService {
    operations: [
      GetPerson,
      FamilyTreeDepth,
      GetChilderen
    ]
}

@uuidFormat
string PersonId

structure Pet {
  @required id: UUID,
  name: String,
  owner: Person

}

structure PetIdInput {
  @httpLabel @required id: UUID
}



@documentation("Get the pet with id")
@readonly
@http(method: "GET", uri: "/pet/{id}", code: 200)
operation GetPet {
  input: PetIdInput,
  output: Pet
}


// This forces a check for recursive types

@documentation("Find number of childeren at each depth of the family tree")
@readonly
@http(method: "POST", uri: "/familyTreeDepth", code: 200)
operation FamilyTreeDepth {
  input: Person,
  output := {
    depth : Integer
  }
}

@documentation("Find number of childeren at each depth of the family tree")
@readonly
@http(method: "POST", uri: "/childeren", code: 200)
operation GetChilderen {
  input: Person,
  output := {
    childeren : People
  }
}


@documentation("Get the information about a person")
@readonly
@http(method: "GET", uri: "/people/{id}")
operation GetPerson {
    input := {
        @httpLabel
        @required
        @documentation("The id of the person")
        id: PersonId
    }
    output := {
        @required
        @documentation("A description of the weather in the city")
        person: Person
    }
}

structure Person {
    @documentation("The id of this person") @required id: PersonId,
    mother: Person,
    father: Person,
    @documentation("Childeren of this person") childeren: People
}

list People {
    member: Person
}
