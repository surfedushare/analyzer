FROM clojure:tools-deps as builder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN clojure -A:depstar -m hf.depstar.uberjar analyzer.jar

FROM openjdk:14-alpine
MAINTAINER Jelmer de Ronde <jelmer.deronde@surfnet.nl>

COPY --from=builder /usr/src/app/analyzer.jar .

EXPOSE 8080
CMD ["java", "-cp", "analyzer.jar", "clojure.main", "-m", "analyzer.server"]
