FROM ubuntu:xenial
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"

WORKDIR /opt

RUN apt-get update -y && apt-get upgrade -y && apt-get autoremove -y\ 
	&& apt-get install openjdk-8-jdk wget -y\
	&& wget https://github.com/UellingtonDamasceno/virtual-fot-device/raw/main/target/Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar\
	&& apt-get remove wget -y\
	&& apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

#CMD ["java", "-jar", "Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar"]
#ENTRYPOINT ["java", "-jar", "Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar"]
