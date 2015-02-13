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
#This file represents utility functions used by other scripts.

#Is this bashfile script or sourced?
# $? = 0 if script
# $? = 1 if sourced
function isScript
{
   local let indx=${#BASH_SOURCE[@]}-1
   #Check bash source agains $0
   if [[ "${BASH_SOURCE[${indx}]}" == "${0}" ]]
   then
       return 0
   fi
   return 1
}
#Prints an error to stderr
# $1 - error message
function printError
{
    echo -e "ERROR:\n\t${1}" >&2
}
#Error and exit, if script..if "sourced" then prints error and does nothing.
function errorAndExit
{
    printError "${1}"
    if ! isScript
    then
        printError "You cannot exit from a sourced script...cascading failure."
        return
    fi
    exit ${2:-1}
}
#Create temp script to run command
#and echo its name/path.
# $1- command to run.
function createScript
{
    local name="/tmp/script-$(uuidgen).bash"
    local command="${1}"
    local tmp='$?'

cat > ${name} \
<<- EOF
#!/bin/bash
. ${ENV_VARS};
cd $(pwd);
${command}
if (( ${tmp} != 0 ))
then
   rm -f ${name}
   errorAndExit "Failed to run: ${command}"
fi
rm -f ${name}
EOF
    chmod 700 ${name}
    echo ${name}
}

#Runs a command over ssh, as bash, with env-vars.
# $1 - host
# $2 - command/script to run on host.
# $3 - backgroud- "true"-run in background, anything else does not.
function sshc
{
    local host="${1}"
    local command="${2}"
    local background="${3}"
    #Sanity checking host is valid
    host "${host:-#nohost#}" 2>&1 1>/dev/null
    if (( $? != 0 ))
    then
        printError "Invalid host: '${host}'"
        return 4
    fi
    #Check command not null
    if [[ "${command}" == "" ]]
    then
        printError "Invalid command: '${command}'"
        return 5
    fi
    #Setup background additions
    bkg=""
    nhp=""
    if [[ "${background}" == "true" ]]
    then
        bkg="</dev/null > /dev/null 2>&1 &"
        nhp="nohup "
    fi
    script=$(createScript "${nhp}${command}")
    scripttmp="${script}-tmp"
    mv ${script} ${scripttmp}
    #Create and copy script to remote host
    scp ${scripttmp} ${host}:${script} > /dev/null
    if (( $? != 0 ))
    then
        rm -f ${scripttmp}
        printError "Failed to scp script: ${scripttmp} to ${host}:${script}"
        return 5
    fi
    #Run script remotely
    ssh ${host} "${script} ${bkg}"
    if (( $? != 0 ))
    then
        rm -f  ${scripttmp}
        printError "Error running '${command}' on remote host: ${host}"
        return 5
    fi
    rm -f ${scripttmp}
}

#Print location of current sctipt
function printScriptLocation
{
    local let indx=${#BASH_SOURCE[@]}-1
    dirname $(readlink -f ${BASH_SOURCE[${indx}]} )
}

#Init screen session to run stuff in
# $1 - Name of the screen session to start
function initScreen
{
    local session=${1}
    if [[ "$( screen -ls | grep "${session}")" == "" ]]
    then
        screen -S ${session} -d -m -A
    fi
    echo ">>>> Using screen: ${session} for new processes <<<<"
    export SCREEN="${session}"
}
#Run a comman in screen.
# $1 - host
# $2 - command
function screenr
{
    local tab=${1}
    local host=${2}
    local cmd="${3}"
    script=$(createScript "sshc '${host}' '${cmd}' false")
    screen -S ${SCREEN} -X screen -t "${tab}" bash
    screen -S ${SCREEN} -X -p "${tab}" stuff "${script}"
}

