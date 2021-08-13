FROM tomcat:9.0.50-jdk8-openjdk-slim-buster

COPY target/webapps/workflow-services-1.10-SNAPSHOT.war /$CATALINA_HOME/webapps/workflow-services.war
COPY target/webapps/cas-product-1.10-SNAPSHOT.war /$CATALINA_HOME/webapps/filemgr-services.war

ENV FMPROD_HOME="${CATALINA_HOME}/webapps/filemgr-services"
