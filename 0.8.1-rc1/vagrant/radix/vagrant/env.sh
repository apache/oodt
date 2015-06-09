#!/bin/bash

# CHANGE BELOW FOR YOUR PROJECT NEEDS!
export PROJECT_GROUP_ID=com.mycompany.mydms
export PROJECT_ARTIFACT_ID=mycompany-myoodt
export BUILD_FLAGS=-Pfm-solr-catalog # NOTE: more flag options at http://s.apache.org/CJL
export OODT_DEPLOYMENT_HOME=/usr/local/oodt

# General configuration (probably don't need to modify)
export OODT_SRC_REPO=https://svn.apache.org/repos/asf/oodt/trunk # NOTE: This should match the below OODT version
export OODT_VERSION=0.7-SNAPSHOT
export JAVA_HOME=/usr/lib/jvm/default-java

