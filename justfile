set windows-shell := ["pwsh.exe", "-NoLogo", "-Command"]

compileApi:
  mill -j 0 api.__.compile

formatApi:
  mill -j 0 api.__.reformat

format:
  mill -j 0 mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources

fix:
  mill -j 0 api.__.fix
  mill -j 0 agentic.fix
  mill -j 0 site.fix

site:
  mill -j 0 -w site.live

test:
  mill -j 0 __.test

update: 
  mill -j 0 mill.scalalib.Dependency/showUpdates

demo:
  scala-cli run . --main-class io.github.quafadas.dairect.Showcase

# serveSite:
#   cs launch --contrib sjsls -- --build-tool none --path-to-index-html {{justfile_directory()}}/out/site/live.dest/site

serveSite:
  mill site.browserSync