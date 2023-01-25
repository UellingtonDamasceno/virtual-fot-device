FROM vegardit/graalvm-maven:latest-java17 AS builder
WORKDIR /opt
COPY . .
RUN mvn -Pnative -DskipTests package \
	&& mv target/Virtual-FoT-Device ../virtual-fot-device \
	&& cd .. \
	&& rm -r -f virtual-fot-device-slim

FROM ubuntu:bionic
WORKDIR /opt
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=builder /opt/target/virtual-fot-device /opt/device
ENTRYPOINT ["./device"]

