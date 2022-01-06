FROM rhel8-openjdk11:latest
VOLUME /tmp
WORKDIR /
ARG JAR_FILE=target/drools-gateway-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
USER 1001
RUN mkdir /tmp/logs
RUN chmod 777
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

