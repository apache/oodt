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
#Source environment and utilites
ENVS=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/../setup/env-vars.sh
. ${ENVS}
if ! isScript
then
    printError "Cannot source this file."
    return 255
fi

#Setup host to command map
starts=()
starts+=("mm ${MESOS_MASTER_IP} '$(printScriptLocation)/start-up/mesos-master.bash'")
starts+=("rm ${RESOURCE_HOST}   '$(printScriptLocation)/start-up/resource.bash'")
#Slaves
let n=0
for host in $(cat ${HOSTS_FILE} | grep -v "^#" | tail -n +2 )
do
    starts+=("s${n} ${host} '$(printScriptLocation)/start-up/mesos-slave.bash'")
    let n=n+1
done

#Setup screen if existing
if [[ "${SCREEN}" != "" ]]
then
    initScreen "${SCREEN}"
fi
#Start Hadoop HDFS 
${HADOOP_HOME}/sbin/start-dfs.sh

#Run all start-up commands
for elem in "${starts[@]}"
do
    set -- ${elem}
    tab=${1}
    host=${2}
    cmd=${3}
    echo "Running: ${cmd} on ${host} via ${SCREEN:-ssh} (${tab})"
    #Run in screen if set, or ssh if not set
    if [[ "${SCREEN}" != "" ]]
    then
        screenr ${tab} ${host} "${cmd}" || errorAndExit "Could not run ${cmd} on ${host} via ${SCREEN}"
    else
        sshc ${host} "${cmd}" true || errorAndExit "Could not run ${cmd} on ${host} via ssh"
    fi
    sleep 1
done


