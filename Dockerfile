FROM node:alpine AS node
RUN adduser -D ametist
USER ametist
WORKDIR /home/ametist
COPY --chown=ametist package.json package-lock.json ./
RUN npm run ci

FROM clojure:openjdk-19-tools-deps AS clojure
RUN adduser ametist
USER ametist
WORKDIR /home/ametist
COPY --chown=ametist ./deps.edn ./
RUN clojure -M:dev -P && clojure -P --report stderr
COPY --chown=ametist . .
COPY --from=node --chown=ametist /home/ametist/node_modules node_modules
RUN clojure -M:dev -m ametist.build

FROM openjdk:19-jdk
RUN adduser ametist
USER ametist
WORKDIR /home/ametist
COPY --from=clojure --chown=ametist /home/ametist/target/ametist.jar ./
CMD java \
  -Dametist.server.http-port="$PORT" \
  -Dametist.server.atm-db-url="$DATABASE_URL" \
  -jar ametist.jar
