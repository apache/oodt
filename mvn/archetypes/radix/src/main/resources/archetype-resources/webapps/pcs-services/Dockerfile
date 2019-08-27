FROM tomcat:8.0-slim

# Environment Variables (should not be modified)
ENV PCS_HOME="/oodt/pcs"

# Environment Variables (should be user specified)
ENV FILEMGR_URL=""
ENV RESMGR_URL=""
ENV WORKFLOW_URL=""

# Steps to extract Source
ARG SRC_FILE
ADD target/${SRC_FILE} ${CATALINA_HOME}/webapps/pcs.war

# Volumes (You can mount these directories from the host machine)
# * /oodt/pcs/policy (PCS Policy Files)

# Start