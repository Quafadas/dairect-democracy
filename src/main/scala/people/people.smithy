$version: "2"

namespace people

use alloy#simpleRestJson
use alloy#uuidFormat

@simpleRestJson
service PeopleService {
    operations: [
      GetPerson,
    ]
}

@uuidFormat
string PersonId

@documentation("Get the information about a person")
@readonly
@http(method: "GET", uri: "/people/{id}")
operation GetPerson {
    input := {
        @httpLabel
        @required
        @documentation("The id of the person")
        id: String
    }
    output := {
        @required
        @documentation("A description of the weather in the city")
        person: Person
    }
}

structure Person {
    @documentation("The id of this person") @httpLabel @required id: PersonId,
    @documentation("Childeren of this person") childeren: People
}

list People {
    member: Person
}
