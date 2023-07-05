package enums


import cats.effect.*

import cats.syntax.option.*

object enumServiceImpl extends EnumService[IO]:

    override def placeCardPicture(faceCard: FaceCard, suit: Suit): IO[PlaceCardPictureOutput] = ???

    override def isFaceCard(cardValue: CardValue, suit: Suit): IO[IsFaceCardOutput] = ???


end enumServiceImpl