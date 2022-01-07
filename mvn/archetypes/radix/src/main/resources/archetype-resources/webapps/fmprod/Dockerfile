FROM tomcat:8.0-slim

# Environment Variables (should not be modified)
ENV FMPROD_HOME="${CATALINA_HOME}/webapps/fmprod"

# Environment Variables (should be user specified)
ENV FILEMGR_URL=""

# Steps to Extract Source
ARG SRC_FILE
ADD target/${SRC_FILE} ${CATALINA_HOME}/webapps/fmprod.war

# Volumes (You can mount these directories from the host machine)

# Start