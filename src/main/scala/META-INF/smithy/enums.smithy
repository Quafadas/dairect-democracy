$version: "2"

namespace enums

use alloy#simpleRestJson
use smithy4s.meta#packedInputs

@simpleRestJson
service EnumService {
    operations: [
      PlaceCardPicture,
      IsFaceCard
    ]
}

@documentation("A description of a card from a stanard deck of playing cards")
@readonly
@http(method: "POST", uri: "/playingCardDescription", code: 200)
operation PlaceCardPicture {
  input:= {
    @required faceCard: FaceCard,
    @required suit: Suit
  },
  output := {
    s : String
  }
}

@documentation("Checks if a card is a face card")
@readonly
@http(method: "GET", uri: "/isFaceCard/{cardValue}/{suit}", code: 200)
operation IsFaceCard {
  input: PlayingCard
  output := {
    b : Boolean
  }
}


@range(min: 0, max: 13)
integer CardValue

structure PlayingCard {
    @required
    @documentation("The face card")
    @httpLabel(label: "cardValue")
    cardValue: CardValue
    @required
    @documentation("The suit")
    @httpLabel(label: "suit")
    suit: Suit
}

intEnum FaceCard {
    JACK = 11
    QUEEN = 12
    KING = 13
    ACE = 1
    JOKER = 0
}

enum Suit {
    DIAMOND = "diamond"
    CLUB = "club"
    HEART = "heart"
    SPADE = "spade"
}