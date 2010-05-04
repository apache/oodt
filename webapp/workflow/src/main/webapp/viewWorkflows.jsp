<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.List" import="java.util.Iterator"
    import="java.net.URL"
    	import="gov.nasa.jpl.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient"
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.Workflow"
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTask"
    	%>
    	
<jsp:include page="inc/header.jsp"/>
<%
     String workflowMgrUrl = application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.mgr.url");
     XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(new URL(workflowMgrUrl));
     
     List workflows = null;
     
     try{
    	   workflows = client.getWorkflows();

    	     if(workflows != null){
    	    	 
    	    	    %>
    	    	      <table>
    	    	        <tr>
    	    	          <td>Workflow ID</td>
    	    	          <td>Name</td>
    	    	          <td>View</td>
    	    	        </tr>
    	    	    <%
    	    	 
    	    	       for(Iterator i = workflows.iterator(); i.hasNext(); ){
    	    	    	     Workflow workflow = (Workflow)i.next();
    	    	    	     
    	    	    	     %>
    	    	    	       <tr>
    	    	    	         <td><%=workflow.getId() %></td>
    	    	    	         <td><%=workflow.getName() %></td>
    	    	    	         <td><a href="javascript:popWin('./viewWorkflow.jsp?workflowId=<%=workflow.getId() %>', '<%=workflow.getId() %>');">Click here</a></td>
    	    	    	        </tr>

    	    	    	     
    	    	    	     <%
    	    	    	   
    	    	       }
    	    	 
    	    	   %>
    	    	   </table>
    	    	   
    	    	   <%
    	    	 
    	     }    	   
    	   
     }
     catch(Exception e){
    	   e.printStackTrace();
     }
     

%>
<jsp:include page="inc/footer.jsp"/>