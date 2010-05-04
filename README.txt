========================================================
Welcome to Apache OODT  <http://incubator.apache.org/oodt/>
========================================================

OODT is a grid middleware framework used on a number of successful projects 
at NASA's Jet Propulsion Laboratory/California Institute of Technology, and many 
other research institutions and universities, specifically those part of the:

    *National Cancer Institute's (NCI's) Early Detection Research Network (EDRN) 
     project - over 40+ institutions all performing research into discovering biomarkers 
     which are early indicators of disease.
    *NASA's Planetary Data System (PDS) - NASA's planetary data archive, a repository and 
    registry for all planetary data collected over the past 30+ years.
    *various Earth Science data processing missions, including Seawinds/QuickSCAT, the 
    Orbiting Carbon Observatory, the NPP Sounder PEATE project, and the Soil Moisture 
    Active Passive (SMAP) mission. 
    
OODT is a podling in the Apache Incubator <http://incubator.apache.org/>,
a project of the Apache Software Foundation <http://www.apache.org/>.

Getting Started
===============

OODT is based on Java 5 and uses the Maven 2 <http://maven.apache.org/>
build system. To build OODT, use the following command in this directory:

    mvn install

The build consists of a number of components.

License (see also LICENSE.txt)
==============================

Collective work: Copyright 2010 The Apache Software Foundation.

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

This distribution includes cryptographic software.  The country in  which
you currently reside may have restrictions on the import,  possession, use,
and/or re-export to another country, of encryption software.  BEFORE using
any encryption software, please  check your country's laws, regulations and
policies concerning the import, possession, or use, and re-export of
encryption software, to  see if this is permitted.  See
<http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity Control
Number (ECCN) 5D002.C.1, which includes information security software using
or performing cryptographic functions with asymmetric algorithms.  The form
and manner of this Apache Software Foundation distribution makes it eligible
for export under the License Exception ENC Technology Software Unrestricted
(TSU) exception (see the BIS Export Administration Regulations, Section
740.13) for both object code and source code.

The following provides more details on the included cryptographic software:

    Apache OODT uses Apache Tika which uses the Bouncy Castle generic encryption 
    libraries for extracting text content and metadata from encrypted PDF files.
    See http://www.bouncycastle.org/ for more details on Bouncy Castle.

Documentation
=============

You can build a local copy of the OODT documentation including JavaDocs
using the following Maven 2 command in the OODT source directory: 

    mvn site 

You can then open the OODT Documentation in a web browser: 

    ./target/site/index.html

Mailing Lists
=============

Discussion about OODT takes place on the following mailing lists:

    oodt-dev@incubator.apache.org    - About using OODT and developing OODT

Notification on all code changes are sent to the following mailing list:

    oodt-commits@incubator.apache.org

The mailing lists are open to anyone and publicly archived.

You can subscribe the mailing lists by sending a message to
oodt-<LIST>-subscribe@incubator.apache.org (for example oodt-dev-subscribe@...).
To unsubscribe, send a message to oodt-<LIST>-unsubscribe@incubator.apache.org.
For more instructions, send a message to oodt-<LIST>-help@incubator.apache.org.

Issue Tracker
=============

If you encounter errors in OODT or want to suggest an improvement or
a new feature, please visit the OODT issue tracker at
https://issues.apache.org/jira/browse/OODT. There you can also find the
latest information on known issues and recent bug fixes and enhancements.

