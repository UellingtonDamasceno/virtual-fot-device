FROM openjdk:8u275-slim
LABEL maintainer="UDamasceno <udamasceno@ecomp.uefs.br>"
WORKDIR /opt/
COPY target/Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/app.jar
EXPOSE 1883
ENTRYPOINT ["java", "-jar", "app.jar"]

