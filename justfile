set windows-shell := ["pwsh.exe", "-NoLogo", "-Command"]

compileApi:
  mill api.__.compile

formatApi:
  mill api.__.reformat

format:
  mill mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources

fix:
  mill api.__.fix
  mill agentic.fix
  mill site.fix

site:
  mill -w site.live

test:
  mill __.test

update:
  mill mill.scalalib.Dependency/showUpdates

demo:
  scala-cli run . --main-class io.github.quafadas.dairect.Showcase

# serveSite:
#   cs launch --contrib sjsls -- --build-tool none --path-to-index-html {{justfile_directory()}}/out/site/live.dest/site

serveSite:
  mill site.browserSync

ƒsetupSmithyLsp:
   mill smithy4s.codegen.LSP/updateConfig