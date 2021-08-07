FROM tomcat:9.0.50-jdk8-openjdk-slim-buster

COPY wmservices/target/workflow-services-1.10-SNAPSHOT.war /$CATALINA_HOME/webapps/workflow-services.war
COPY fmprod/target/cas-product-1.10-SNAPSHOT.war /$CATALINA_HOME/webapps/filemgr-services.war

ENV FMPROD_HOME="${CATALINA_HOME}/webapps/filemgr-services"
