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
ENV RELATIVE_PRODUCT_PATH=""

# Steps to Extract Source
WORKDIR /oodt
ARG SRC_FILE
ADD target/${SRC_FILE} .

# Volumes (You can mount these directories from the host machine)
# * /oodt/crawler/policy (Policy Files)
# * /oodt/crawler/logs (Logs)
# * /tmp (Temporary Files)

# Start
WORKDIR /oodt/crawler/bin
CMD java -Djava.ext.dirs="../lib" -Djava.util.logging.config.file="../etc/logging.properties" -Dorg.apache.oodt.cas.crawl.bean.repo="../policy/crawler-config.xml" -Dorg.apache.oodt.cas.cli.action.spring.config="../policy/cmd-line-actions.xml" -Dorg.apache.oodt.cas.cli.option.spring.config="../policy/cmd-line-options.xml" org.apache.oodt.cas.crawl.CrawlerLauncher --operation --launchAutoCrawler --filemgrUrl $FILEMGR_URL --clientTransferer org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory --productPath /oodt/data/$RELATIVE_PRODUCT_PATH --mimeExtractorRepo "../policy/extractor/mime-extractor-map.xml"
