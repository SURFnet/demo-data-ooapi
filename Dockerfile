FROM clojure:openjdk-11-lein as builder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN lein uberjar

FROM gcr.io/distroless/java:11
MAINTAINER SURF <edu-beheer@surfnet.nl>

COPY --from=builder /usr/src/app/target/demo-data-server-standalone.jar .

COPY production.logback.xml /
ENV JDK_JAVA_OPTIONS="-Dlogback.configurationFile=production.logback.xml"

ENV HOST=$HOST
ENV PORT=$PORT
ENV SEED=$SEED
EXPOSE $PORT
CMD ["demo-data-server-standalone.jar"]
