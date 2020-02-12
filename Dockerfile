FROM clojure:openjdk-11-lein
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
RUN git clone https://github.com/zeekat/surf-demodata.git # 2 - TODO: make public release of demodata repo
WORKDIR /usr/src/app/surf-demodata
RUN lein install
WORKDIR /usr/src/app
RUN rm -rf /usr/src/app/surf-demodata

COPY project.clj /usr/src/app/
RUN lein deps
COPY src /usr/src/app/src
COPY resources /usr/src/app/resources

ENV HOST=$HOST
ENV PORT=$PORT
ENV SEED=$SEED
EXPOSE $PORT
CMD lein run
