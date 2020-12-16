FROM openjdk:8u275-slim
LABEL maintainer="UDamasceno <udamasceno@ecomp.uefs.br>"
WORKDIR /opt/

RUN apt-get update -y && apt-get upgrade -y && apt-get install wget -y;\
    wget https://github.com/UellingtonDamasceno/virtual-fot-device/raw/main/target/Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar;\
    apt autoremove -y

ENTRYPOINT ["java", "-jar", "Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar"]

