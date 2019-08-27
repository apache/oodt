FROM openjdk:8-jre-alpine

# Environment Variables (should not be modified)
ENV OODT_HOME="/oodt"
ENV RESMGR_HOME="/oodt/resmgr"

# Environment Variables (should be user specified)

# Steps to Extract Source
WORKDIR /oodt
ARG SRC_FILE
ADD target/${SRC_FILE} .

# Volumes (You can mount these directories from the host machine)
# * /oodt/resmgr/policy (Policy Files)
# * /oodt/resmgr/logs (Logs)
# * /tmp (Temporary Files)

# Start
WORKDIR /oodt/resmgr/bin
EXPOSE 9001
CMD java -Djava.ext.dirs="${RESMGR_HOME}/lib" -Djava.util.logging.config.file="${RESMGR_HOME}/etc/logging.properties" -Dorg.apache.oodt.cas.resource.properties="${RESMGR_HOME}/etc/resource.properties" -Djava.io.tmpdir="/tmp" org.apache.oodt.cas.resource.system.ResourceManagerMain --portNum 9001
