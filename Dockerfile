FROM openjdk:11-jdk-slim

WORKDIR /src
COPY . /src

RUN apt-get update
RUN apt-get install -y dos2unix
RUN dos2unix gradlew

RUN bash gradlew buildFatJar

WORKDIR /run
RUN cp /src/build/libs/*.jar /run/ru.earl.gpns-all.jar

EXPOSE 8080

CMD java -jar /run/ru.earl.gpns-all.jar