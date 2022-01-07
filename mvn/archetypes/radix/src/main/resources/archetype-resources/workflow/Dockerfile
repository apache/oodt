FROM openjdk:8-jre-alpine
MAINTAINER Yasith Jayawardana <yasith.jayawardana@icloud.com>

# Environment Variables (should not be modified)
ENV OODT_HOME="/oodt"
ENV WORKFLOW_HOME="/oodt/workflow"

# Environment Variables (should be user specified)
ENV RESMGR_URL=""

# Steps to Extract Source
WORKDIR /oodt
ARG SRC_FILE
ADD target/${SRC_FILE} .

# Volumes (You can mount these directories from the host machine)
# * /oodt/workflow/policy (Policy Files)
# * /oodt/workflow/logs (Logs)
# * /tmp (Temporary Files)

# Start
WORKDIR /oodt/workflow/bin
EXPOSE 9002
CMD java -Djava.ext.dirs="${WORKFLOW_HOME}/lib" -Djava.util.logging.config.file="${WORKFLOW_HOME}/etc/logging.properties" -Dorg.apache.oodt.cas.workflow.properties="${WORKFLOW_HOME}/etc/workflow.properties" -Djava.io.tmpdir="/tmp" -Dorg.apache.oodt.cas.pge.task.metkeys.legacyMode="true" -Dorg.apache.oodt.cas.pge.task.status.legacyMode="true" org.apache.oodt.cas.workflow.system.WorkflowManagerStarter --portNum 9002
