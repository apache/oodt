FROM openjdk:8-jre-alpine

ADD target/cas-filemgr-1.9-SNAPSHOT-dist.tar.gz /
RUN mv cas-filemgr-1.9-SNAPSHOT /filemgr

WORKDIR /filemgr/bin

ENTRYPOINT ["sh", "./filemgr", "run"]