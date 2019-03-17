# Welcome to Apache OODT  <http://oodt.apache.org/>

Apache Object Oriented Data Technology (OODT) is the smart way to integrate and archive your processes, your data, and its metadata. OODT allows you to:

- Generate Data
- Process Data
- Manage Your Data
- Distribute Your Data
- Analyze Your Data
Allowing for the integration of data, computation, visualization and other components.

OODT also allows for remote execution of jobs on scalable computational infrastructures so that computational and data-
intensive processing can be integrated into OODT’s data processing pipelines using cloud computing and high-performance 
computing environments.

![Overview of OODT Main Components](http://oodt.apache.org/img/oodt-diag.png "OODT Component Overview")


## Why OODT?
Traditional processing pipelines are commonly made up of custom UNIX shell scripts and fragile custom written glue code.
Apache OODT uses structured XML-based capturing of the processing pipeline that can be understood and modified by 
non-programmers to create, edit, manage and provision workflow and task execution.

It is being used on a number of successful projects at NASA's Jet Propulsion Laboratory/California 
Institute of Technology, and many other research institutions and universities, 
specifically those part of the:

- **National Cancer Institute's (NCI's) Early Detection Research Network (EDRN)
  project** - over 40+ institutions all performing research into discovering
  biomarkers which are early indicators of disease.
- **NASA's Planetary Data System (PDS)** - NASA's planetary data archive, a
  repository and registry for all planetary data collected over the past 30+
  years.
- Various Earth Science data processing missions, including
  **Seawinds/QuickSCAT**, the **Orbiting Carbon Observatory**, the **NPP Sounder PEATE
  project**, and the **Soil Moisture Active Passive (SMAP) mission**.
- [**Apache DRAT**](https://github.com/apache/drat) - A distributed release audit tool written on top of OODT's 
capabilities.

OODT is a Top Level project of the Apache Software Foundation
<http://www.apache.org/>.

***

# Getting Started

## Useful Resources
- [*OODT Wiki*](https://cwiki.apache.org/confluence/display/OODT/Home)
- [*Apache OODT platform: Use metadata as a first class citizen* by Tom Barber](https://jaxenter.com/tom-barber-nasa-interview-apache-oodt-127821.html)
- [RADiX Powered By OODT](https://cwiki.apache.org/confluence/display/OODT/RADiX+Powered+By+OODT)
- [*A Look into the Apache OODT Ecosystem* by Chris Mattmann](https://www.slideshare.net/chrismattmann/a-look-into-the-apache-oodt-ecosystem)

## Build from scratch

OODT is primarily written in Java, with some components available in Python.
It requires Java 8 and uses the Maven 3 <http://maven.apache.org/> build
system.  To build the Java components of OODT, use the following command in
this directory:

    mvn clean install

For the Python components, see the **agility** subdirectory.

## RADiX Powered By OODT

OODT isn’t an out of the box solution, but we do try to make it as easy as possible. For that, OODT provides the RADIX build system which will compile a fully operational OODT platform ready for development and deployment. Building the RADIX distribution is as simple as running the following commands:

```bash
export JAVA_HOME=/usr/lib/jvm… (adjust for your own JAVA_HOME)
curl -s "https://git-wip-us.apache.org/repos/asf?p=oodt.git;a=blob_plain;f=mvn/archetypes/radix/src/main/resources/bin/radix;hb=HEAD" | bash
 
mv oodt oodt-src; cd oodt-src; mvn install
mkdir ../oodt; tar -xvf distribution/target/oodt-distribution-0.1-bin.tar.gz -C ../oodt
cd ../oodt; ./bin/oodt start
./resmgr/bin/batch_stub 2001
```
Navigate to http://localhost:8080/opsui, you should see the default OPSUI system which provides system oversight and interrogation

***

# Contributing

To contribute a patch, follow these instructions.

1. File JIRA issue for your fix at https://issues.apache.org/jira/browse/OODT.
you will get issue id OODT-xxx where xxx is the issue ID.

2. [Fork the repo](http://help.github.com/fork-a-repo) on which you're working, clone your forked repo to your local computer, and set up the upstream remote:
    ```
    git clone https://github.com/<YourGitHubUserName>/oodt.git
    git remote add upstream https://github.com/apache/oodt.git
    ```
3. Go into oodt directory
    ```
    cd oodt
    ```
4. Checkout out a new local branch based on your master and update it to the latest.The convention is to name the branch after the current JIRA issue, e.g. OODT-xxx where xxx is the issue ID.
    ```
    git checkout -b OODT-xxx
    ```
5. Do the changes to the relavant files and keep your code clean. If you find another bug, you want to fix while being in a new branch, please fix it in a separated branch instead.

6. Add relevant files to the staging  area.
    ```
    git add <files>
    ```
7. For every commit please write a short (max 72 characters) summary of the change. Use markdown syntax for simple styling. Please include any JIRA issue numbers in your summary.
    ```
    git commit -m “[OODT-xxx] Put change summary here ”
    ```
    **NEVER leave the commit message blank!** Provide a detailed, clear, and complete description of your commit!

8. Before submitting a pull request, update your branch to the latest code.
    ```
    git checkout master
    git pull --rebase upstream master
    git checkout OODT-xxx
    git rebase -i master
    ```
9. Push the code to your forked repository
    ```
    git push origin OODT-xxx
    ```
10. In order to make a pull request,
  * Navigate to the OODT repository you just pushed to (e.g. https://github.com/your-user-name/oodt)
  * Click "Pull Request".
  * Write your branch name in the branch field (this is filled with "master" by default)
  * Click "Update Commit Range".
  * Ensure the changesets you introduced are included in the "Commits" tab.
  * Ensure that the "Files Changed" incorporate all of your changes.
  * Fill in some details about your potential patch including a meaningful title.
  * Click "Send pull request".
## Issue Tracker

If you encounter errors in OODT or want to suggest an improvement or a new
feature, please visit the OODT issue tracker 
https://issues.apache.org/jira/browse/OODT.  There you can also find the
latest information on known issues and recent bug fixes and enhancements.

## Documentation

You can find an enormous amount of useful documentation/resources related to OODT in 
[**OODT Confluence Wiki**](https://cwiki.apache.org/confluence/display/OODT/Home).

You can build a local copy of the OODT documentation including JavaDocs using
the following Maven 2 command in the OODT source directory:

    mvn site

You can then open the OODT Documentation in a web browser:

    ./target/site/index.html

Note: all OODT source files are encoded with UTF-8.  You must set your
MAVEN_OPTS environment variable to include "-Dfile.encoding=UTF-8" in order to
properly generate the web site and other artifacts from source.

Note: generating the documentation requires enormous amounts of memory.  More
than likely you'll need to add to the MAVEN_OPTS environment variable in order
to set the Java heap maximum size with "-Xmx512m" or larger before attempting
to run "mvn site".

***

# License (see also LICENSE.txt)

Collective work: Copyright 2010-2012 The Apache Software Foundation.

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Apache OODT includes a number of subcomponents with separate copyright
notices and license terms. Your use of these subcomponents is subject to
the terms and conditions of the licenses listed in the LICENSE.txt file.

***

# Export control

This distribution includes cryptographic software.  The country in which you
currently reside may have restrictions on the import, possession, use, and/or
re-export to another country, of encryption software.  BEFORE using any
encryption software, please check your country's laws, regulations and
policies concerning the import, possession, or use, and re-export of
encryption software, to see if this is permitted.  See
<http://www.wassenaar.org/> for more information.

The U.S.  Government Department of Commerce, Bureau of Industry and Security
(BIS), has classified this software as Export Commodity Control Number (ECCN)
5D002.C.1, which includes information security software using or performing
cryptographic functions with asymmetric algorithms.  The form and manner of
this Apache Software Foundation distribution makes it eligible for export
under the License Exception ENC Technology Software Unrestricted (TSU)
exception (see the BIS Export Administration Regulations, Section 740.13) for
both object code and source code.

The following provides more details on the included cryptographic software:

    Apache OODT uses Apache Tika which uses the Bouncy Castle generic
    encryption libraries for extracting text content and metadata from
    encrypted PDF files.  See http://www.bouncycastle.org/ for more details on
    Bouncy Castle.

***

# Mailing Lists

Discussion about OODT takes place on the following mailing lists:

    dev@oodt.apache.org    - About using OODT and developing OODT

Notification on all code changes are sent to the following mailing list:

    commits@oodt.apache.org

The mailing lists are open to anyone and publicly archived.

You can subscribe the mailing lists by sending a message to
<LIST>-subscribe@oodt.apache.org (for example
dev-subscribe@oodt...).  To unsubscribe, send a message to
<LIST>-unsubscribe@oodt.apache.org.  For more instructions, send a
message to <LIST>-help@oodt.apache.org.
