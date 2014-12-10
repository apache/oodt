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
#Now run commands
echo "Attempting to kill Mesos master on: ${MESOS_MASTER_IP}"
sshc ${MESOS_MASTER_IP} "pkill -u $(whoami) mesos-master" false \
                             || printError "Could not terminate mesos master"
#Loop through hosts and start slaves (except the first, which is master)
for host in $(cat ${HOSTS_FILE} | grep -v "^#" | tail -n +2 )
do
    echo "Attempting to kill Mesos slave on: ${host}"
    sshc ${host} "pkill -u $(whoami) mesos-slave" false \
                      || printError "Could not terminate mesos slave on ${host}"
done
sshc ${RESOURCE_HOST} \
    "pkill -u $(whoami) -f org.apache.oodt.cas.resource.system.XmlRpcResourceManager" false \
    || printError "Could not terminate resource manager on host: ${RESOURCE_HOST}"
#Shut down screen if using screen
if [[ "${SCREEN}" != "" ]]
then
    screen -X -S ${SCREEN} quit
fi
#Shutdown Hadoop HDFS
${HADOOP_HOME}/sbin/stop-dfs.sh
