<%@ page language="java" session="true" contentType="text/html; charset=UTF-8" info="Config" errorPage="error.jsp" 
  import="java.util.Map,java.util.Iterator,org.apache.oodt.grid.Server,org.apache.oodt.grid.Utility,java.net.URL" %>
<%--
Copyright 2005 California Institute of Technology. ALL RIGHTS
RESERVED. U.S. Government Sponsorship acknowledged.

$Id: config.jsp,v 1.4 2005/06/14 20:00:40 kelly Exp $
--%>
<jsp:useBean id="cb" scope="session" class="org.apache.oodt.grid.ConfigBean"/>
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>
  <head>
    <title>Web Grid Configuration</title>
    <link rel='stylesheet' type='text/css' href='style.css'/>
  </head>
  <body>
    <h1>Web Grid Configuration</h1>

    <% if (cb.getMessage().length() > 0) { %>
      <div class='error'><jsp:getProperty name='cb' property='message'/></div>
    <% } %>

    <form action='conf' method='post'>

      <fieldset>
        <legend>Administrative Settings</legend>
	<div class='field'>
          <label for='passwordField'>New Administrator Password</label>
          <div class='fieldHelp'>
            Leave blank to leave password unchanged.
          </div>
	  <input id='passwordField' type='password' name='password'/>
        </div>

	<div class='field'>
	  <label for='lhf'>Require Administrative Access from Local Host Only</label>
	  <div class='fieldHelp'>
	    Click Yes to enable administrative access from browsers
	    running on the local web-server host only.  Click No to
	    allow any host to access these administrative pages.
	  </div>
	  <input id='lhf' type='radio' name='localhost' value='on' <%= cb.isLocalhostRequired()? "checked='checked'" : "" %> />Yes
	  <input type='radio' name='localhost' value='off' <%= !cb.isLocalhostRequired()? "checked='checked'" : "" %> />No
	</div>

        <div class='field'>
          <label for='httpsField'>Require HTTPS for Administrative Access</label>
          <div class='fieldHelp'>
	    Click Yes to require HTTPS for access to these administrative pages. Note that this will require
	    your web server to also support HTTPS. If you're not sure, <strong>click No</strong>.
          </div>
          <input id='httpsField' type='radio' name='https' value='on' <%= cb.isHttpsRequired()? "checked='checked'" : "" %> />Yes
          <input type='radio' name='https' value='off' <%= !cb.isHttpsRequired()? "checked='checked'" : "" %> />No
        </div>
      </fieldset>

      <fieldset>
	<legend>System Properties</legend>
	<div class='field'>
	  <div class='fieldHelp'>To add a new system property, enter
	    its key and value at the bottom.  You can change the values
	    of existing properties. To delete an existing property,
	    check the box to its right.</div>
	  <table>
	    <thead><tr><th>#</th><th>Key</th><th>Value</th><th>Delete?</th></tr></thead>
	    <tbody>
	    <% int row = 1; for (Iterator i = cb.getProperties().entrySet().iterator(); i.hasNext(); ++row) {
	       Map.Entry entry = (Map.Entry) i.next();
	       String key = (String) entry.getKey();
	       String val = (String) entry.getValue();
	    %>
	      <tr class='<%= row % 2 == 0? "evenRow" : "oddRow"%>'>
		<td><%= row %>.</td>
		<td><span class='key'><%= Utility.esc(key) %></span></td>
		<td><input type='text' name='val-<%= Utility.esc(key) %>' value='<%= Utility.esc(val) %>'/></td>
		<td><input type='checkbox' name='del-<%= Utility.esc(key) %>'/></td>
	      </tr>
	    <% } %>
	      <tr class='newRow'>
		<td>(New)</td>
		<td><input type='text' id='newkey' name='newkey'/></td>
		<td><input type='text' id='newval' name='newval'/></td>
		<td>&#x00a0;</td>
	      </tr>
	    </tbody>
	  </table>
	</div>
      </fieldset>

      <fieldset>
	<legend>Code Bases</legend>
	<div class='fieldHelp'>Specify a URL for each code base.  URLs
	  to files are assumed to be jar files.  URLs that end in a
	  <code>/</code> refer to directories containing class files.
	  Check the box to delete a code base.
	</div>

	<table>
	  <thead><tr><th>#</th><th>Code Base</th><th>Delete?</th></thead>
	  <tbody>
	  <% row = 0; for (Iterator i = cb.getConfiguration().getCodeBases().iterator(); i.hasNext(); ++row) {
	    URL codeBaseURL = (URL) i.next();
	  %>
	    <tr class='<%= row+1 % 2 == 0? "evenRow" : "oddRow"%>'>
	      <td><%= row+1 %>.</td>
	      <td><%= Utility.esc(codeBaseURL.toString()) %></td>
	      <td><input type='checkbox' name='delcb-<%= row %>'/></td>
	    </tr>
	  <% } %>
	    <tr class='newRow'>
	      <td>(New)</td>
	      <td><input type='text' name='newcb'/></td>
	      <td>&#x00a0;</td>
	    </tr>
	  </tbody>
	</table>
      </fieldset>

      <table>
        <caption>Query Handlers</caption>
        <tbody>
          <tr style="vertical-align: top;">
            <td>
	      <fieldset>
		<legend>Product Query Handlers</legend>
		<div class='field'>
		  <div class='fieldHelp'>Specify the class name for each
		    product query handler; the class must implement the
		    <code>jpl.eda.product.QueryHandler</code> interface.</div>
		  <table>
		    <thead><tr><th>#</th><th>Class Name</th><th>Delete?</th></tr></thead>
		    <tbody>
		    <% row = 0; for (Iterator i = cb.getProductServers().iterator(); i.hasNext(); ++row) {
		      Server server = (Server) i.next();
		      String className = server.getClassName();
		    %>
		      <tr class='<%= row % 2 == 0? "oddRow" : "evenRow" %>'>
			<td><%= row+1 %>.</td>
			<td><code><%= Utility.esc(className) %></code></td>
			<td><input type='checkbox' name='drm-<%= row %>'/></td>
		      </tr>
		    <% } %>
		      <tr class='newRow'>
			<td>(New)</td>
			<td><input type='text' name='d-newcn'/></td>
			<td>&#x00a0;</td>
		      </tr>
		    </tbody>
		  </table>
		</div>
	      </fieldset>
            </td>
            <td>
	      <fieldset>
		<legend>Profile Query Handlers</legend>
		<div class='field'>
		  <div class='fieldHelp'>Specify the class name for each
		    profile query handler; the class must implement the
		    <code>jpl.eda.profile.handlers.ProfileHandler</code> interface.</div>
		  <table>
		    <thead><tr><th>#</th><th>Class Name</th><th>Delete?</th></tr></thead>
		    <tbody>
		    <% row = 0; for (Iterator i = cb.getProfileServers().iterator(); i.hasNext(); ++row) {
		      Server server = (Server) i.next();
		      String className = server.getClassName();
		    %>
		      <tr class='<%= row % 2 == 0? "oddRow" : "evenRow" %>'>
			<td><%= row+1 %>.</td>
			<td><code><%= Utility.esc(className) %></code></td>
			<td><input type='checkbox' name='mrm-<%= row %>'/></td>
		      </tr>
		    <% } %>
		      <tr class='newRow'>
			<td>(New)</td>
			<td><input type='text' name='m-newcn'/></td>
			<td>&#x00a0;</td>
		      </tr>
		    </tbody>
		  </table>
		</div>
	      </fieldset>
            </td>
          </tr>
        </tbody>
      </table>

      <div class='formControls'>
        <input type='submit' name='submit' value='Save Changes'/>
      </div>

    </form>

  </body>
</html>
