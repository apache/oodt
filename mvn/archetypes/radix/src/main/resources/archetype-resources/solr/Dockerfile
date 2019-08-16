FROM solr:5-alpine

# Using root user, as tar extraction was giving permission denied errors
USER root
ARG SRC_FILE
ADD target/${SRC_FILE} ${SOLR_HOME}

# Change back to solr user
USER solr
