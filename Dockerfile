FROM clojure:tools-deps as builder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN clojure -A:depstar -m hf.depstar.uberjar analyzer.jar -C -m analyzer.server

FROM gcr.io/distroless/java:11
MAINTAINER Jelmer de Ronde <jelmer.deronde@surfnet.nl>

COPY --from=builder /usr/src/app/analyzer.jar .

EXPOSE 8080
CMD ["analyzer.jar"]
