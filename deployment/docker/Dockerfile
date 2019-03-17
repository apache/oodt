FROM maven:3.5
MAINTAINER Tom Barber

#Setting the Work Directory for OODT in Docker Container
WORKDIR /usr/src

RUN mvn archetype:generate -DarchetypeGroupId=org.apache.oodt -DarchetypeArtifactId=radix-archetype -DarchetypeVersion=0.9 -Doodt=1.2.3 -DgroupId=com.mycompany -DartifactId=oodt -Dversion=0.1 && mv oodt oodt-src; cd oodt-src; mvn package && mkdir /usr/src/oodt; tar -xvf /usr/src/oodt-src/distribution/target/oodt-distribution-0.1-bin.tar.gz -C /usr/src/oodt && cd /usr/src/oodt-src && mvn clean && rm -rf ~/.m2
# Maven archetype generation command to make an oodt project.
# Fix Below Parameters before build the docker image
# groupId = specify your company's namespace
# artifactId = pecify a short name of your project
# version = initial version label for your project
# oodt = the version of OODT that you want your project to be built on
RUN mvn archetype:generate -DarchetypeGroupId=org.apache.oodt \
    -DarchetypeArtifactId=radix-archetype -DarchetypeVersion=0.9 \
    -Doodt=1.2.5 -DgroupId=com.mycompany \
    -DartifactId=oodt -Dversion=0.1 \
    && mv oodt oodt-src; \
    cd oodt-src; \
    mvn package \
    && mkdir /usr/src/oodt; \
    tar -xvf distribution/target/oodt-distribution-0.1-bin.tar.gz -C /usr/src/oodt \
    && mkdir /usr/src/oodt/tomcat/server/webapps/host-manager /usr/src/oodt/tomcat/server/webapps/manager \
    && mvn clean && rm -rf ~/.m2

#Exposing required ports to local
EXPOSE 8080
EXPOSE 9000
EXPOSE 2001
EXPOSE 9001
EXPOSE 9200
EXPOSE 9002

# Starting OODT and Loging catalina log
CMD cd /usr/src/oodt/bin/ && ./oodt start && tail -f /usr/src/oodt/tomcat/logs/catalina.out