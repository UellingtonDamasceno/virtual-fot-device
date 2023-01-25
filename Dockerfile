FROM vegardit/graalvm-maven:latest-java17 AS builder
WORKDIR /opt
COPY . .
RUN mvn -Pnative -DskipTests package \
	&& mv target/virtual-fot-device ../virtual-fot-device
	
FROM ubuntu:bionic
WORKDIR /opt
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=builder /opt/virtual-fot-device /opt/
ENTRYPOINT ["./virtual-fot-device"]

