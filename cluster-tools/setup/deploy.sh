#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#Variables are in the env_vars.sh file
ENVS=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/env-vars.sh
if [ ! -f ./env-vars.sh ]
then
    echo "${ENVS} not found. Please use ${ENVS}.tmpl as a template to create it."
    exit 1
fi
. ${ENVS}

#Tell us what ya going to do
echo "Deploying the cluster components:"
echo "  Host file: ${HOSTS_FILE:-ERROR: Host file not set}"
echo "  Temporary directory: ${TMP_DIR:-ERROR: Temp dir not set}"
echo "  Install directory:   ${INSTALL_DIR:-ERROR: Install dir not set}"

#Checking versions
if [ -z ${INSTALL_DIR} ] || [ -z ${TMP_DIR} ] || [ -z ${HOSTS_FILE} ]
then
    echo "ERROR: Needed variables not set. Did you set the environment files?"
    exit 1
fi
#Check directories exit
if [ ! -d ${INSTALL_DIR} ] || [ ! -d ${TMP_DIR} ] || [ ! -f ${HOSTS_FILE} ]
then
    echo "ERROR: Needed directories or files don't exist"
    exit 1
fi
#Cannot find scripts
if [ ! -d ../scripts ]
then
    echo "ERROR: Cannot find scripts directory: ../scripts"
    exit 1
fi
#Check space
if [ -e ${INSTALL_DIR} ] && (( $(df -k ${INSTALL_DIR} | awk 'NR==3 {print $3}') < 5000000 ))
then
    echo "ERROR: Not enough space (5GB) to install in: ${INSTALL_DIR}."
    exit 1 
fi
#Check space
if (( $(df -k ${TMP_DIR} | awk 'NR==3 {print $3}') < 3000000 ))
then
    echo "ERROR: Not enough tmp space (3GB): ${TMP_DIR}."
    exit 1
fi

while true; do
    read -p "Continue installing(y/n)? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Please answer y or n.";;
    esac
done
#Put on all hosts
for host in $(cat ${HOSTS_FILE} | grep -v "^#" | tail -n +2 )
do
    echo "Deploying to: ${host}"
    ssh ${host} "mkdir -p ${INSTALL_DIR};" \
    ||{ \
         echo "WARNING: Cannot create ${INSTALL_DIR} on ${host} Deploy manually."; \
         continue;\
      }
    rsync -avz ${INSTALL_DIR}/* "${host}:${INSTALL_DIR}"
done
