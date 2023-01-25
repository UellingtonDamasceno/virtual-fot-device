FROM vegardit/graalvm-maven:latest-java17 AS builder
WORKDIR /opt
COPY . .
RUN mvn -Pnative -DskipTests package \
	&& ls \
	&& cd target \
	&& ls \
	&& cd .. \
	&& mv target/virtual-fot-device ../virtual-fot-device 

FROM ubuntu:bionic
WORKDIR /opt
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=builder /opt/target/virtual-fot-device /opt/virtual-fot-device
ENTRYPOINT ["./virtual-fot-device"]

