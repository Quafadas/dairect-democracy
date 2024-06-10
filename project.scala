//> using scala 3.4.2
//> using repository sonatype:snapshots
//> using dep org.typelevel::toolkit::0.1.26
//> using dep com.disneystreaming.smithy4s::smithy4s-http4s::0.18.21
//> using dep is.cir::ciris:3.6.0
//> using dep com.lihaoyi::pprint::0.9.0
//> using dep tech.neander::smithy4s-deriving::0.0.2
//> using plugin tech.neander::smithy4s-deriving-compiler::0.0.2
//> using option -experimental
//> using dep software.amazon.smithy:smithy-jsonschema:latest.release

//// Use circe to avoid ordering issues in schema testing

//> using test.dep io.circe::circe-parser::0.14.5
//> using test.dep io.circe::circe-literal::0.14.5
//> using test.dep com.disneystreaming.alloy:alloy-core:0.2.3
//> using test.dep com.disneystreaming.smithy4s::smithy4s-dynamic::0.18.21
//> using test.dep software.amazon.smithy:smithy-jsonschema:1.35.0
//> using test.dep org.scalameta::munit::1.0.0
