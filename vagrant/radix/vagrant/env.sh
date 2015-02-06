#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# CHANGE BELOW FOR YOUR PROJECT NEEDS!
export PROJECT_GROUP_ID=com.mycompany.mydms
export PROJECT_ARTIFACT_ID=mycompany-myoodt
export BUILD_FLAGS=-Pfm-solr-catalog # NOTE: more flag options at http://s.apache.org/CJL
export OODT_DEPLOYMENT_HOME=/usr/local/oodt

# General configuration (probably don't need to modify)
export OODT_SRC_REPO=https://svn.apache.org/repos/asf/oodt/trunk # NOTE: This should match the below OODT version
export OODT_VERSION=0.7-SNAPSHOT
export JAVA_HOME=/usr/lib/jvm/default-java

