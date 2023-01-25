FROM vegardit/graalvm-maven:latest-java17 AS build
WORKDIR /opt
COPY . .
RUN mvn -Pnative -DskipTests package && ls
	
FROM ubuntu:bionic
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=build /opt/target/virtual-fot-device virtual-fot-device
ENTRYPOINT ["./virtual-fot-device"]

