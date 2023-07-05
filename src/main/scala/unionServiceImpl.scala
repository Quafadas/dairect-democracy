package union

import cats.effect.*
import unions.UnionService
import unions.PlaceCardDescriptionUntaggedOutput
import unions.UntaggedPlayingCard
import unions.PlaceCardDescriptionOutput
import unions.PlayingCard

object unionServiceImpl extends UnionService[IO]:

  override def placeCardDescription(value: PlayingCard): IO[PlaceCardDescriptionOutput] = ???

  override def placeCardDescriptionUntagged(value: UntaggedPlayingCard): IO[PlaceCardDescriptionUntaggedOutput] = ???

end unionServiceImpl
