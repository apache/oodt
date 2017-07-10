# Welcome to Apache OODT  <http://oodt.apache.org/>

OODT is a grid middleware framework used on a number of successful projects at
NASA's Jet Propulsion Laboratory/California Institute of Technology, and many
other research institutions and universities, specifically those part of the:

* National Cancer Institute's (NCI's) Early Detection Research Network (EDRN)
  project - over 40+ institutions all performing research into discovering
  biomarkers which are early indicators of disease.
* NASA's Planetary Data System (PDS) - NASA's planetary data archive, a
  repository and registry for all planetary data collected over the past 30+
  years.
* Various Earth Science data processing missions, including
  Seawinds/QuickSCAT, the Orbiting Carbon Observatory, the NPP Sounder PEATE
  project, and the Soil Moisture Active Passive (SMAP) mission.

OODT is a Top Level project of the Apache Software Foundation
<http://www.apache.org/>.

Getting Started
===============

OODT is primarily written in Java, with some components available in Python.
It requires Java 5 and uses the Maven 2 <http://maven.apache.org/> build
system.  To build the Java components of OODT, use the following command in
this directory:

    mvn clean install

For the Python components, see the "agility" subdirectory.

Contributing
============
To contribute a patch, follow these instructions (note that installing
[Hub](http://hub.github.com) is not strictly required, but is recommended).

```
0. Download and install hub.github.com
1. File JIRA issue for your fix at https://issues.apache.org/jira/browse/OODT
- you will get issue id OODT-xxx where xxx is the issue ID.
2. git clone http://github.com/apache/oodt.git
3. cd oodt
4. git checkout -b OODT-xxx
5. edit files
6. git status (make sure it shows what files you expected to edit)
7. git add <files>
8. git commit -m “fix for OODT-xxx contributed by <your username>”
9. git fork
10. git push -u <your git username> OODT-xxx
11. git pull-request
```


License (see also LICENSE.txt)
==============================

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

Export control
==============

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

Documentation
=============

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

Mailing Lists
=============

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

Issue Tracker
=============

If you encounter errors in OODT or want to suggest an improvement or a new
feature, please visit the OODT issue tracker at
https://issues.apache.org/jira/browse/OODT.  There you can also find the
latest information on known issues and recent bug fixes and enhancements.
