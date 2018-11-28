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

##################################################################################
#
# README.txt
#
# @author riverma@apache.org
#
# @description This README describes how to setup, install, and run radix-vagrant. 
# After completing these steps, you will have a fully configured Ubuntu Virtual 
# Machine* running a fresh version of OODT RADiX.
##################################################################################


Requirements:
1. 5GB of space to install VM on
2. Vagrant: see http://www.vagrantup.com/
3. VirtualBox: see https://www.virtualbox.org/wiki/Downloads

Setup:
1. Modify the contents of vagrant/env.sh to reflect your desired configuration
   - add your project name etc here
   - configure which type of RADiX build you want here (e.g. solr-enabled)
2. Modify the Vagrantfile VM_NAME to name your VM (VM names must be unique)
3. vagrant up
NOTE: The above command will take up to 15 minutes the first time you run it.
NOTE: All source files will be placed within /usr/local/src within the VM.

Start:
* NOTE: The provisioning script auto starts OODT, so you may skip to step 2
1. (optional) Launch OODT if not already running below
   > cd $OODT_HOME/bin
   > ./oodt start
2. Navigate to: http://localhost:8080/opsui

-----
* You can learn more about how to use your newly created virtual machine at: 
http://www.vagrantup.com/
