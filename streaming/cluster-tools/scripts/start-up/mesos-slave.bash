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
#Wrapper to start-up mesos-slave.
ENVS=$(dirname $(readlink -f ${BASH_SOURCE[0]}))/../../setup/env-vars.sh
. ${ENVS}
#Don't source this
if ! isScript
then
    printError "Cannot source this file."
    return 255
fi
${MESOS_BUILD}/bin/mesos-slave.sh --master=${MESOS_MASTER_IP}:5050 --no-switch_user
