/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

OODT Balance Web Application README File

The OODT Balance web application skeleton is a quick way to get started 
developing a new Balance application. It provides the necessary directory 
structure and sane default configuration values that you can customize to 
suit your needs. These instructions assume you have the necessary access 
to install files into your web server's document root. These instructions 
will help you create an application called 'my-application' which will be 
accessed from http://localhost/my-application. Your environment and/or 
needs may differ, so adapt the following as necessary.


1. Installing the OODT Balance PEAR package ("Balance library")

The Balance library provides core classs that must be in place prior to 
developing a Balance application. The library only needs to be installed 
once per system. If you have previously installed the Balance library on 
your system, you can safely skip this step.

   a. Download the latest stable Balance library release (OODT_ROOT/balance/lib/pear)
      to a scratch directory on your system
   b. Build the pear package using the pear command line utility:
      $ pear package
   c. Install the resulting package (.tgz file) using the pear installer:
      $ pear install --force Org_Apache_Oodt_Balance-#.#.#.tgz

2. Setting up the web application skeleton

The files in this directory represent the Balance application skeleton. If
you have not already done so, move the directory containing this file to
your web server's directory root, noting the name you use for the path
(e.g.: /path/to/document/root/my-application). The instructions below assume
that you have named the directory 'my-application'. Adjust as necessary for 
your environment.

   a. In the config.ini file, set the url_base configuration variable to /my-application
   b. In the .htaccess file, set the RewriteBase directive to /my-application
   c. Visit http://localhost/my-application to see your skeletal Balance application. Note
      that the URL you use to visit your application may differ slightly from the above 
      if you have set up virtual hosting or have a domain name other than localhost.

3. Developing your web application

The following is a very high level introduction to developing a web application using Balance.

Balance operates on the concept of application 'views'. Each application url, e.g.: 
http://localhost/my-application/about, maps to exactly one view, e.g.: HOME/views/about.php, 
where HOME is the location of the Balance web application files. A view file is a plain vanilla
PHP file, which, in its simplest form, might only contain static HTML. For more involved tasks,
functions from the Balance core library can be invoked directly from within the view file.

Most sites incorporate common, site-wide content at the top (header) and bottom (footer) of
each page. Balance supports header and footer files, which get included with every view unless
the view file itself explicitly modifies this behavior. By default, the header and footer file
can be found at HOME/views/common/header.php and HOME/views/common/footer.php, respectively.

Occasionally, such as when a form gets submitted via POST, a script needs to be invoked to handle
the input. Scripts are generally located at HOME/scripts, and are named according to the url
that will be used to invoke them. Scripts are differentiated from views from a url standpoint in
that urls for scripts end in '.do' whereas urls for views generally do not have a file extension.
As a concrete example, imagine a login view providing inputs for username and password. It might
theoretically reside at HOME/views/login.php and be accessed via 
http://localhost/my-application/login. On submission, the login form's target might be set to a 
login script that processes the submitted user information. That script would reside at 
HOME/scripts/login.php and be accessed via http://localhost/my-application/login.do. In the 
event that the script is intended to handle GET parameters, they are appended after the .do using
the standard url notation (e.g.: ...myscript.do?param1=value1&param2=value2). The '.do' extension is 
merely a url routing convention meant to imply the execution of some action, the actual script 
file in HOME/scripts maintains the '.php' extension.

More information can be obtained by visiting the OODT website (http://oodt.apache.org) or posing
a question to the OODT user mailing list (user@oodt.apache.org).

