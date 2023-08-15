FROM vegardit/graalvm-maven:latest-java17 AS build
WORKDIR /opt
COPY . .
RUN mvn -Pnative -DskipTests package && ls
	
FROM ubuntu:23.04
RUN apt-get update -y && apt-get upgrade -y && apt-get autoremove -y\ 
    && apt-get install net-tools -y\
	&& apt-get install iproute2 -y\
	&& apt-get install iputils-ping -y\
 	&& apt-get autoremove -y\
	&& apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"
COPY --from=build /opt/target/virtual-fot-device virtual-fot-device
ENTRYPOINT ["./virtual-fot-device"]

