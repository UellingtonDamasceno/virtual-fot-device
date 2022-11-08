FROM ubuntu:bionic AS download
WORKDIR /opt

RUN apt-get update -y && apt-get autoremove -y\ 
	&& apt-get install --no-install-recommends wget -y\
	&& wget https://github.com/UellingtonDamasceno/virtual-fot-device/archive/refs/tags/latest.tar.gz --no-check-certificate\
	&& tar -xvzf latest.tar.gz

FROM maven:3.8.6-eclipse-temurin-11-alpine AS builder
WORKDIR /opt
COPY --from=download /opt/virtual-fot-device-latest /opt/virtual-fot-device-latest
RUN cd virtual-fot-device-latest\
	&& mvn clean install\
	&& mv target/Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar ../device.jar\
	&& cd ..\
	&& rm -r -f virtual-fot-device-latest


FROM openjdk:11.0.16-jre-slim
WORKDIR /opt
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=builder /opt/device.jar /opt/device.jar
ENTRYPOINT ["java", "-jar", "device.jar"]
