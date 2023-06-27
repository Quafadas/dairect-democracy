package weather

import cats.effect.IO
import cats.Id

class MySuite extends munit.FunSuite:
  test("hello"):
    //val default = JsonProtocolF[IO].toJsonSchema(DefaultSchemaVisitor)
    // println(default)
    val something = JsonProtocolF[IO].toJsonSchema(weatherServiceImpl)
    println(something)
    val arg = something.get("GetWeather").get

    println(arg.childeren)

end MySuite
