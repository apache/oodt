FROM openjdk:8-jre-alpine

ADD target/cas-workflow-1.9-SNAPSHOT.tar.gz /
RUN mv cas-workflow-1.9-SNAPSHOT /workflowmgr

WORKDIR /workflowmgr/bin

ENTRYPOINT ["sh", "./wmgr", "run"]