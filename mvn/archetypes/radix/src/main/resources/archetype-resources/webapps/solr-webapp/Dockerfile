FROM tomcat:8.0-slim
MAINTAINER Yasith Jayawardana <yasith.jayawardana@icloud.com>

# Environment Variables (should not be modified)

# Environment Variables (should be user specified)

# Steps to Extract Source
ARG SRC_FILE
ADD target/${SRC_FILE} ${CATALINA_HOME}/webapps/solr.war

# Volumes (You can mount these directories from the host machine)

# Start