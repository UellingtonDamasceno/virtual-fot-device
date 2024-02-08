FROM ubuntu:bionic as builder
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"

WORKDIR /opt

RUN apt-get update -y && apt-get upgrade -y && apt-get autoremove -y\ 
	&& apt-get install wget -y\
	&& wget https://github.com/UellingtonDamasceno/virtual-fot-device/archive/refs/tags/latest.zip\
	&& apt-get install unzip -y\
	&& unzip latest.zip\
	&& rm latest.zip\
	&& mv virtual-fot-device-latest vfd\
	&& cd vfd\
	&& apt-get install openjdk-11-jdk maven -y\
	&& mvn clean install\
	&& apt-get purge maven -y\
	&& apt-get autoremove -y\
	&& apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

FROM adoptopenjdk/openjdk11:jre-11.0.18_10-alpine as vfd-run
WORKDIR /opt
RUN apk add --update --no-cache \
	bash \
	tcpdump \
	iperf \
	busybox-extras \
	iproute2 \
	iputils

LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=builder /opt/vfd/target/virtual-fot-device-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/device.jar
ENTRYPOINT ["java", "-jar", "device.jar"]

