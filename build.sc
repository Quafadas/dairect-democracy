import $ivy.`com.disneystreaming.smithy4s::smithy4s-mill-codegen-plugin::0.18.23`
import $ivy.`com.goyeau::mill-scalafix::0.4.0`
import $ivy.`io.github.quafadas::millSite::0.0.26`
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import $ivy.`com.github.lolgab::mill-crossplatform::0.2.4`

import smithy4s.codegen.mill._
import de.tobiasroeser.mill.vcs.version._
import com.goyeau.mill.scalafix.ScalafixModule
import mill._, scalalib._, scalafmt._
import mill.scalajslib._, mill.scalanativelib._
import mill.scalalib.publish._
import io.github.quafadas.millSite.SiteModule
import com.github.lolgab.mill.crossplatform._

trait Common extends ScalaModule with ScalafmtModule with ScalafixModule with PublishModule {
  def scalaVersion = "3.5.0"
  def scalacOptions = Seq("-experimental", "-Wunused:all")
  override def pomSettings = T {
    PomSettings(
      description = "Open AI API and examples",
      organization = "io.github.quafadas",
      url = "https://github.com/Quafadas/dairect-democracy",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("quafadas", "dairect-democracy"),
      developers = Seq(
        Developer("quafadas", "Simon Parten", "https://github.com/quafadas")
      )
    )
  }
  def publishVersion = VcsVersion.vcsState().format()
}

trait CommonJS extends ScalaJSModule {
  def scalaJSVersion = "1.16.0"
}

trait CommonTests extends TestModule.Munit {
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.scalameta::munit::1.0.1"
  )
}

trait CommonNative extends ScalaNativeModule {
  def ivyDeps = super.ivyDeps() ++ Agg(
  )
  def scalaNativeVersion: mill.T[String] = "0.4.16"
}


val http4s = "0.23.27"


object api extends CrossPlatform {
  trait Shared extends CrossPlatformScalaModule with Common with Smithy4sModule {
    def ivyDeps = Agg(
      ivy"com.disneystreaming.smithy4s::smithy4s-http4s::$http4s",
      ivy"tech.neander::smithy4s-deriving::0.0.3",
      ivy"org.http4s::http4s-ember-client::$http4s",
      ivy"is.cir::ciris::3.6.0"
    )

    trait SharedTests extends CommonTests {
      // common `core` test settings here
    }
  }

  object jvm extends Shared {
    object test extends ScalaTests with SharedTests
  }

  object js extends Shared with CommonJS {
    object test extends ScalaJSTests with SharedTests
  }

  // object native extends Shared with CommonNative {
    // object test extends ScalaNativeTests with SharedTests
  // }
}

object agentic extends Common {

  override def moduleDeps = Seq(api.jvm)
  override def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"software.amazon.smithy:smithy-jsonschema:1.50.0",
    ivy"com.lihaoyi::os-lib::0.10.5",
    ivy"com.lihaoyi::pprint::0.9.0",
    ivy"com.disneystreaming.smithy4s::smithy4s-dynamic::$http4s"


  )
  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::1.0.1",
      ivy"org.typelevel::munit-cats-effect::2.0.0"
    )
  }
}

object site extends SiteModule {

  def scalaVersion = agentic.scalaVersion  

  override def scalacOptions: Target[Seq[String]] = agentic.scalacOptions

  override def moduleDeps = Seq(agentic)

  override def scalaMdocVersion: T[String] = "2.5.3"

}