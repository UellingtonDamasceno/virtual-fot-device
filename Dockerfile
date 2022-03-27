FROM ubuntu:bionic
LABEL maintainder="UDamasceno <udamasceno@ecomp.uefs.br>"

WORKDIR /opt

RUN apt-get update -y && apt-get upgrade -y && apt-get autoremove -y\ 
	&& apt-get install openjdk-11-jdk maven wget -y\
	&& wget https://github.com/UellingtonDamasceno/virtual-fot-device/archive/refs/tags/latest.tar.gz\
	&& tar -xvzf latest.tar.gz\
	&& cd virtual-fot-device-latest\
	&& mvn clean install\
	&& mv target/Virtual-FoT-Device-1.0-SNAPSHOT-jar-with-dependencies.jar ../device.jar\
	&& cd ..\
	&& rm -f latest.tar.gz\
	&& rm -r -f virtual-fot-device-latest\
	&& apt-get purge wget -y\
	&& apt-get purge maven -y\
	&& apt-get autoremove -y\
	&& apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENTRYPOINT ["java", "-jar", "device.jar"]