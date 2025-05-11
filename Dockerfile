FROM eclipse-temurin:21-jdk
VOLUME /tmp
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
COPY target/sia-maat-1.0.0-SNAPSHOT.jar sia-maat.jar
EXPOSE 8082
#ENTRYPOINT exec java $JAVA_OPTS -jar helloworld.jar
# For Spring-Boot project, use the entrypoint below to reduce Tomcat startup time.
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar sia-maat.jar
