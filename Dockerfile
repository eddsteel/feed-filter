# https://node.mu/2018/11/27/multistage-docker-builds-scala/
FROM hseeberger/scala-sbt:11.0.6_1.3.9_2.13.1 as builder
WORKDIR /build
# Cache dependencies first
COPY project project
COPY build.sbt .
RUN sbt update
# Then build
COPY . .
RUN sbt stage

FROM openjdk:8u181-jre-slim
WORKDIR /app
COPY --from=builder /build/target/universal/stage/. .
EXPOSE 8080
CMD ["./bin/feed-filter"]
