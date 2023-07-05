$version: "2"

namespace unions

use alloy#simpleRestJson
use alloy#untagged
use smithy4s.meta#packedInputs

@simpleRestJson
service UnionService {
    operations: [
      PlaceCardDescription,
      PlaceCardDescriptionUntagged
    ]
}

@documentation("A description of a card from a stanard deck of playing cards")
@readonly
@http(method: "POST", uri: "/playingCardDescription", code: 200)
operation PlaceCardDescription {
  input:= {
    @required value: PlayingCard
  },
  output := {
    s : String
  }
}

@documentation("A description of a card from a stanard deck of playing cards, but untagged")
@readonly
@http(method: "POST", uri: "/playingCardDescriptionUntagged", code: 200)
operation PlaceCardDescriptionUntagged {
  input:= {
    @required value: UntaggedPlayingCard
  },
  output := {
    s : String
  }
}

@range(min: 2, max: 10)
integer NormalCardValue

intEnum FaceCard {
    JACK = 11
    QUEEN = 12
    KING = 13
    ACE = 1
    JOKER = 0
}

union CardValue {
    n: NormalCardValue
    f: FaceCard
}

@untagged
union CardValueUntagged {
    n: NormalCardValue
    f: FaceCard
}


structure UntaggedPlayingCard {
    @required
    cardValue: CardValueUntagged
}


structure PlayingCard {
    @required
    cardValue: CardValue
}

