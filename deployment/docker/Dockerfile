FROM maven:3.5
MAINTAINER Tom Barber

#ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64/

WORKDIR /usr/src

RUN mvn archetype:generate -DarchetypeGroupId=org.apache.oodt -DarchetypeArtifactId=radix-archetype -DarchetypeVersion=0.9 -Doodt=1.2.3 -DgroupId=com.mycompany -DartifactId=oodt -Dversion=0.1 && mv oodt oodt-src; cd oodt-src; mvn package && mkdir /usr/src/oodt; tar -xvf /usr/src/oodt-src/distribution/target/oodt-distribution-0.1-bin.tar.gz -C /usr/src/oodt && cd /usr/src/oodt-src && mvn clean && rm -rf ~/.m2

EXPOSE 8080
EXPOSE 9000
EXPOSE 2001
EXPOSE 9001
EXPOSE 9200
EXPOSE 9002

CMD cd /usr/src/oodt/bin/ && ./oodt start && tail -f /usr/src/oodt/tomcat/logs/catalina.out
