
demo:
  scala-cli run . --main-class io.github.quafadas.dairect.Showcase

test:
  scala-cli test .

scala-cli-test:
  scala-cli run . --main-class io.github.quafadas.dairect.ScalaCliTest

autoCode:
  scala-cli run . --main-class io.github.quafadas.dairect.AutoCodeExample

autoStockPrices:
  scala-cli run . --main-class io.github.quafadas.dairect.StockPrices

initiatives:
  scala-cli run . --main-class io.github.quafadas.dairect.TryInitiatives

showcase:
  scala-cli run . --main-class io.github.quafadas.dairect.Showcase

research:
  scala-cli run . --main-class io.github.quafadas.dairect.Researcher

debug:
  scala-cli run . --main-class io.github.quafadas.dairect.Showcase --debug

