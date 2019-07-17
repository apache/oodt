FROM openjdk:8-jre-alpine

# Environment Variables (should not be modified)
ENV OODT_HOME="/oodt"
ENV FILEMGR_HOME="/oodt/filemgr"

# Environment Variables (should be user specified)
ENV SOLR_URL=""

# Steps to Extract Source
WORKDIR /oodt
ARG SRC_FILE
ADD target/${SRC_FILE} .

# Volumes (You can mount these directories from the host machine)
# * /oodt/filemgr/policy (Policy Files)
# * /oodt/data/catalog (Lucene Index Path)
# * /oodt/data/archive (GenericFile Repository Path)
# * /oodt/filemgr/logs (Logs)
# * /tmp (Temporary Files)

# Start
WORKDIR /oodt/filemgr
EXPOSE 9000
CMD java -Djava.ext.dirs="${PWD}/lib" -Djava.util.logging.config.file="${PWD}/etc/logging.properties" -Dorg.apache.oodt.cas.filemgr.properties="${PWD}/etc/filemgr.properties" -Djava.io.tmpdir="/tmp" org.apache.oodt.cas.filemgr.system.XmlRpcFileManager --portNum 9000
