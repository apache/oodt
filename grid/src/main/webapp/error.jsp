<%@ page language="java" session="true" contentType="text/html; charset=UTF-8" info="Config" isErrorPage="true" 
  import="org.apache.oodt.grid.AuthenticationRequiredException,java.io.StringWriter,java.io.PrintWriter"
%>
<!--
Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE.txt file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
-->
<jsp:useBean id="cb" scope="session" class="org.apache.oodt.grid.ConfigBean"/>
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>
  <head>
    <title>Web Grid</title>
    <link rel='stylesheet' type='text/css' href='style.css'/>
  </head>
  <body>
    <%! StringWriter s = new StringWriter();
    PrintWriter w = new PrintWriter(s); %>
    <% exception.printStackTrace(w); w.close(); %>

    <h1>Web Grid</h1>
    <% if (cb.getMessage().length() > 0) { %>
      <div class='error'><jsp:getProperty name='cb' property='message'/></div>
    <% } %>

    <% if (exception instanceof AuthenticationRequiredException) { %>
      <form action='login' method='post'>
        <fieldset>
	  <legend>Log In</legend>
	  <div class='field'>
	    <label for='pw'>Administrator password:</label>
	    <div class='fieldHelp'>
	      Passwords are case sensitve; check your CAPS LOCK key, if necessary.
	    </div>
	    <input id='pw' type='password' name='password'/>
	  </div>
	  <div class='formControls'>
	    <input type='submit' name='submit' value='Log In'/>
          </div>
        </fieldset>
      </form>
   <% } else { %>
     <h1>Error</h1>
     <div>Exception <code><%= exception.getClass().getName() %></code>: <%= exception.getMessage() %></div>
     <pre><%= s.getBuffer().toString() %></pre>
   <% } %>
  </body>
</html>
