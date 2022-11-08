FROM ubuntu:bionic AS download
WORKDIR /opt

RUN apt-get update -y && apt-get autoremove -y\ 
	&& apt-get install --no-install-recommends wget -y\
	&& wget https://github.com/UellingtonDamasceno/virtual-fot-device/archive/refs/tags/slim.tar.gz --no-check-certificate\
	&& tar -xvzf slim.tar.gz

FROM vegardit/graalvm-maven:latest-java17 AS builder
WORKDIR /opt
COPY --from=download /opt/virtual-fot-device-slim /opt/virtual-fot-device-slim
RUN cd virtual-fot-device-slim\
	&& mvn -Pnative -DskipTests package\
	&& mv target/Virtual-FoT-Device ../virtual-fot-device\
	&& cd ..\
	&& rm -r -f virtual-fot-device-slim

FROM ubuntu:bionic
WORKDIR /opt
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=builder /opt/virtual-fot-device /opt/device
ENTRYPOINT ["./device"]
