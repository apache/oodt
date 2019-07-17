FROM openjdk:8-jre-alpine

# Environment Variables (should not be modified)
ENV OODT_HOME="/oodt"
ENV CRAWLER_HOME="/oodt/crawler"

# Environment Variables (should be user specified)
ENV FAILURE_DIR=""
ENV BACKUP_DIR=""
ENV WORKFLOW_URL=""
ENV FILEMGR_URL=""
ENV PUSHPULL_MET_FILE_EXT=""

# Steps to Extract Source
WORKDIR /oodt
ARG SRC_FILE
ADD target/${SRC_FILE} .

# Volumes (You can mount these directories from the host machine)
# * /oodt/crawler/policy (Policy Files)
# * /oodt/crawler/logs (Logs)
# * /tmp (Temporary Files)

# Start
WORKDIR /oodt/crawler
CMD java -Djava.ext.dirs="${PWD}/lib" -Djava.util.logging.config.file="${PWD}/etc/logging.properties" -Dorg.apache.oodt.cas.crawl.bean.repo="${PWD}/policy/crawler-config.xml" -Dorg.apache.oodt.cas.cli.action.spring.config="${PWD}/policy/cmd-line-actions.xml" -Dorg.apache.oodt.cas.cli.option.spring.config="${PWD}/policy/cmd-line-options.xml" org.apache.oodt.cas.crawl.CrawlerLauncher
