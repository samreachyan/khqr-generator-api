#FROM openjdk:8
FROM openjdk:8-jdk-alpine

ENV APP_FILE khqr-0.0.1-SNAPSHOT.jar
ENV APP_HOME /app

COPY target/$APP_FILE $APP_HOME/
COPY src/main/resources/report $APP_HOME/

WORKDIR $APP_HOME

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]

EXPOSE 8080