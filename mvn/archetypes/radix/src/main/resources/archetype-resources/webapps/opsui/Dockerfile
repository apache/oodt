FROM tomcat:8.0-slim

# Environment Variables (should not be modified)
ENV WORKFLOW_HOME="/oodt/workflow"
ENV PCS_HOME="/oodt/pcs"

# Environment Variables (should be user specified)
ENV FILEMGR_URL=""
ENV RESMGR_URL=""
ENV WORKFLOW_URL=""
ENV GANGLIA_URL=""

# Steps to Extract Source
ARG SRC_FILE
ADD target/${SRC_FILE} ${CATALINA_HOME}/webapps/opsui.war

# Volumes (You can mount these directories from the host machine)
# * /oodt/workflow/policy (Workflow Manager Policy Files)
# * /oodt/pcs/policy (PCS Policy Files)

# Start