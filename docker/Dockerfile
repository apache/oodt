FROM ubuntu:latest
MAINTAINER Tom Barber

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/

RUN apt-get update
RUN apt-get install -y openjdk-8-jre-headless openjdk-8-jre maven curl
#RUN sudo apt-get install maven2
WORKDIR /usr/src

RUN curl -s http://svn.apache.org/repos/asf/oodt/trunk/mvn/archetypes/radix/src/main/resources/bin/radix | bash
RUN mv oodt oodt-src; cd oodt-src; mvn package
RUN mkdir /usr/src/oodt; tar -xvf /usr/src/oodt-src/distribution/target/oodt-distribution-0.1-bin.tar.gz -C /usr/src/oodt

EXPOSE 8080
EXPOSE 9000
EXPOSE 2001
EXPOSE 9001
EXPOSE 9200
EXPOSE 9002

CMD cd /usr/src/oodt/bin/ && ./oodt start && tail -f /usr/src/oodt/tomcat/logs/catalina.out 
