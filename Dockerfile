FROM clojure:alpine as builder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

FROM openjdk:14-alpine
MAINTAINER Jelmer de Ronde <jelmer.deronde@surfnet.nl>

COPY --from=builder /usr/src/app/app-standalone.jar .

EXPOSE 8080
CMD ["java", "-jar", "app-standalone.jar"]
