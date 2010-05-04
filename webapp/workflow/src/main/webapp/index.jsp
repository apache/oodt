<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<jsp:include page="inc/header.jsp"/>

  <h3>Welcome to the <%= application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.display.name") %>!</h3>
  
  
  <p>You can:
    <ul>
      <li><a href="./viewWorkflowInstances.jsp">View active Workflows</a></li>
      <li><a href="./viewWorkflows.jsp">View what Workflow Descriptions are available.</a></li>
      <li><a href="./viewEventToWorkflowMap.jsp">View what Workflows are associated with different Events</a></li>
    </ul>
   
<jsp:include page="inc/footer.jsp">
	<jsp:param name="showNavigation" value="false"/>
</jsp:include>