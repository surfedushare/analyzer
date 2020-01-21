repl:
  clj -A:dev:socket:rebl

build:
  clojure -A:depstar -m hf.depstar.uberjar analyzer.jar -C -m analyzer.server
