FROM clojure:tools-deps as builder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN clojure -X:uberjar :jar analyzer.jar :main-class analyzer.server
##RUN clojure -A:depstar -X hf.depstar.uberjar analyzer.jar -C -m analyzer.server

FROM gcr.io/distroless/java:11
LABEL  org.opencontainers.image.authors="Jelmer de Ronde <jelmer.deronde@surfnet.nl>"

COPY --from=builder /usr/src/app/analyzer.jar .

EXPOSE 8080
CMD ["analyzer.jar"]
