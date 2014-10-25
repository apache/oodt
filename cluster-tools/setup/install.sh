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
#!/bin/bash

# Usage statement.
# Pass in the name of the script.
function usage
{
   echo -e "Usage:\n\t${1:-install.sh} <install-dir> <run-dir> <host-list>" 1>&2
   echo -e "\n  install-dir:\n\tLocation to install all software." 1>&2
   echo -e "\n  run-dir:\n\tLocation to create needed running, log and work directories." 1>&2
   echo -e "\n  host-list:\n\tList of hosts used for this cluster." 1>&2
   echo 1>&2
   exit -1
}

#Create the envars file
# $1 - run directory
# $2 - install directory
# $3 - hosts list
function envars
{
   RUN=${1}
   INSTALL=${2}
   HOSTS=${3}
   MESOS_MASTER=$(cat ${HOSTS} | grep -v "^#" | head -1 | xargs host | awk '{print $4}')

   TARGET=${INSTALL}/scripts/env-vars.sh
   cp env-vars.sh.tmpl ${TARGET} || return 6
   #Fill in @something@ templates
   sed -i 's#@run_dir@#'"${RUN}"'#g' ${TARGET} || return 6
   sed -i 's#@install_dir@#'"${INSTALL}"'#g' ${TARGET} || return 6
   sed -i 's#@mesos_master_ip@#'"${MESOS_MASTER}"'#g' ${TARGET} || return 6
   sed -i 's#@hosts_file@#'"${HOSTS}"'#g' ${TARGET} || return 6
}

#Creare the needed running directories.
# $1 - Base running directory
function makeRunningDirs
{
    mkdir -p ${1} || return 5
    #Make directories
    for dir in "log/mesos" "log/resource" "run/mesos" 
    do
        mkdir -p  ${1}/${dir} || return 5
        chmod 774 ${1}/${dir} || return 5
    done
}


# ~~main~~ #
#Break if it is being sourced (loading functions)
if [[ "${BASH_SOURCE[0]}" != "${0}" ]]
then
    return
fi
# Print usage if input vars are not available
if (( $# != 3 ))
then
    usage ${0}
fi
THIS="$(pwd)/${0}"
#Setup variables
INSTALL=${1}
RUN=${2}
#Copy host file to install
HOSTS=${3:-./hosts}
cp ${HOSTS} ${INSTALL}/scripts/
HOSTS=${INSTALL}/scripts/$(basename ${HOSTS})

#Make running directories
###for host in $(cat ${HOSTS} | grep -v "^#")
###do
###    ssh ${host} "/bin/bash -c '. ${THIS}; makeRunningDirs ${RUN} || exit $?'" 
###    if (( $? != 0 ))
###    then
###        echo -e "ERROR:\n\tFailed to make run dir on ${host} at ${RUN}" 2>&1
###        exit 5
###    fi
###done
#Make env-vars.sh from template env-vars.sh.tmpl



envars ${RUN} ${INSTALL} ${HOSTS}
