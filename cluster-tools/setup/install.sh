#!/bin/bash
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

#Variables are in the env_vars.sh file
ENVS=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/env-vars.sh
HOSTS=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/hosts
if [ ! -f ${ENVS} ]
then
    echo "${ENVS} not found. Please use ${ENVS}.tmpl as a template to create it."
    exit 1
elif [ ! -f ${HOSTS} ]
then
    echo "${HOSTS} not found. Please use create a hosts file."
    exit 1
fi
. ${ENVS}

#Tell us what ya going to do
echo "Installing the BDAS components:"
echo "  Mesos version:   ${APACHE_MESOS_VERSION:-ERROR: Version not set}"
echo "  Scala version:   ${SCALA_VERSION:-ERROR: Version not set}"
echo "  Kafka version:   ${KAFKA_VERSION:-ERROR: Version not set}"
echo "  Spark version:   ${SPARK_VERSION:-ERROR: Version not set}"
echo "  Tachyon version: ${TACHYON_VERSION:-ERROR: Version not set}"
echo "  Hadoop version:  ${HADOOP_VERSION:-ERROR: Version not set}"
echo "  Cluster Tools version:  ${CLUSTER_TOOLS_VERSION:-ERROR: Version not set}"
echo "  Temporary directory: ${TMP_DIR:-ERROR: Temp dir not set}"
echo "  Running Directory:   ${RUN_DIR:-ERROR: Running dir not set}"
echo " -----------------------------------------"
echo "  Install directory:   ${INSTALL_DIR:-ERROR: Install dir not set}"

#Checking versions
if [ -z ${APACHE_MESOS_VERSION} ] || [ -z ${SCALA_VERSION} ] || [ -z ${KAFKA_VERSION} ] || \
   [ -z ${SPARK_VERSION} ] || [ -z ${TACHYON_VERSION} ] || [ -z ${HADOOP_VERSION} ] || \
   [ -z ${CLUSTER_TOOLS_VERSION} ] || [ -z ${INSTALL_DIR} ] || [ -z ${TMP_DIR} ]
then
    echo "ERROR: Needed variables not set. Did you set the environment files?"
    exit 1
fi
#Check directories exit
if [ ! -d ${INSTALL_DIR} ] || [ ! -d ${RUN_DIR} ] || [ ! -d ${TMP_DIR} ]
then
    echo "ERROR: Needed directories don't exist"
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

#Start of installation
INSTALL_LOG=dependancy-installation-$(date +"%Y-%m-%dT%H:%M:%S").log
echo "Installing dependancies at $(date +"%Y-%m-%dT%H:%M:%S")" | tee ${INSTALL_LOG} 

#Downloads: wget and untar these 
APACHE_MIRROR=http://archive.apache.org/dist/
_KAFKA_VR=$(echo ${KAFKA_VERSION} | sed 's/^[^-]*-//')
DOWNLOADS[0]=http://www.scala-lang.org/files/archive/scala-${SCALA_VERSION}.tgz
DOWNLOADS[1]=${APACHE_MIRROR}/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz
DOWNLOADS[2]=${APACHE_MIRROR}/mesos/${APACHE_MESOS_VERSION}/mesos-${APACHE_MESOS_VERSION}.tar.gz
DOWNLOADS[3]=${APACHE_MIRROR}/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop2.4.tgz
DOWNLOADS[4]=${APACHE_MIRROR}/kafka/${_KAFKA_VR}/kafka_${KAFKA_VERSION}.tgz
DOWNLOADS[5]=https://github.com/amplab/tachyon/releases/download/v${TACHYON_VERSION}/tachyon-${TACHYON_VERSION}-bin.tar.gz

mkdir -p ${TMP_DIR}/download
mkdir -p ${INSTALL_DIR}
#Grab all the downloads
for url in "${DOWNLOADS[@]}"
do
    tarball=$(basename ${url})
    #Get directory name without .tar.gz and .tgz 
    dir=${tarball%%.tar.gz}
    dir=${dir%%.tgz}
    #Remove -bin for Tachyeon
    dir=${dir%%-bin}
    echo "Attempting to install: ${dir}" | tee -a ${INSTALL_LOG} 
    #If something
    if [ -e ${INSTALL_DIR}/${dir} ]
    then
        echo "${dir} installation detected."
    else
        rm -f ${TMP_DIR}/download/${tarball}
        #wget or fail and continue
        wget -nc -P ${TMP_DIR}/download ${url} &>> ${INSTALL_LOG} \
        ||{ \
            echo "WARNING: Failed to wget: ${url} Install manually." | tee -a ${INSTALL_LOG};\
            continue;\
          }
        #untar or fail and continue
        tar -xzf ${TMP_DIR}/download/${tarball} -C ${TMP_DIR}/download \
        ||{ \
            echo "WARNING: Failed to untar: ${tarball} Install manually." | tee -a ${INSTALL_LOG};\
            continue;\
          }
        #Move to installation directory or fail and continue
        mv ${TMP_DIR}/download/${dir} ${INSTALL_DIR} \
        ||{ \
            echo "WARNING: Failed to move: ${dir} to ${INSTALL_DIR} Install manually." | tee -a ${INSTALL_LOG};\
            continue;\
          }
        #Move to installation directory or fail and continue
        mv ${TMP_DIR}/download/${tarball} ${INSTALL_DIR}/${dir} \
        ||{ \
            echo "WARNING: Failed to move: ${tarball} to ${INSTALL_DIR} Install manually." | tee -a ${INSTALL_LOG};\
            continue;\
          }
    fi
done
#Install scripts
if [ -e ${INSTALL_DIR}/cluster-tools/scripts ]
then
    echo "Cluster tools already installed"
else
    echo "Exporting OODT-cluster tools" | tee -a ${INSTALL_LOG}
    svn export https://svn.apache.org/repos/asf/oodt/${CLUSTER_TOOLS_VERSION}/cluster-tools/ ${INSTALL_DIR} \
    ||{ \
         echo "WARNING: Failed to export cluster-tools: ${CLUSTER_TOOLS_VERSION} Install manually." | tee -a ${INSTALL_LOG};\
            continue;\
          }
    cp ${ENVS} ${ENV_VARS} 
    cp ${HOSTS} ${HOSTS_FILE}
fi
echo "Building messos. This may take awhile" | tee -a ${INSTALL_LOG}
#Mesos post processing
if [ -e ${INSTALL_DIR}/mesos-${APACHE_MESOS_VERSION}/build ]
then
    echo "Mesos already built at: ${INSTALL_DIR}/mesos-${APACHE_MESOS_VERSION}/build"
else
    _MESOS_BUILD=${INSTALL_DIR}/mesos-${APACHE_MESOS_VERSION}/build
    mkdir ${_MESOS_BUILD}
    cd ${_MESOS_BUILD}
    ../configure &>> ${INSTALL_LOG}
    make &>> ${INSTALL_LOG} 
fi

echo "All done at $(date +"%Y-%m-%dT%H:%M:%S")" | tee -a ${INSTALL_LOG}
