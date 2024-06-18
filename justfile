
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


debug:
  scala-cli run . --main-class io.github.quafadas.dairect.Showcase --debug

