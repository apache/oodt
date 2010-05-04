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
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowCondition"
    	%>
    	
<jsp:include page="inc/header.jsp"/>

<%
     String workflowMgrUrl = application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.mgr.url");
     XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(new URL(workflowMgrUrl));
     
     WorkflowCondition condition = null;
     String conditionId = request.getParameter("conditionId");

     
     if(conditionId != null){
    	  try{
       	   condition = client.getConditionById(conditionId);

       	     if(condition != null){
       	    	 
       	    	    %>
       	    	      <table>
       	    	        <tr>
       	    	          <td>Condition ID</td>
       	    	          <td><%=condition.getConditionId() %></td>
       	    	        </tr>
       	    	        <tr>       	    	          
       	    	          <td>Name</td>
       	    	          <td><%=condition.getConditionName() %></td>
       	    	        </tr>
       	    	        <tr>
       	    	          <td>Implementation Class</td>
       	    	          <td><%=condition.getConditionInstanceClassName() %></td>
       	    	        </tr>
       	          </table>
       	    	       <%
       	     }
       	    	          
        }
        catch(Exception e){
       	   e.printStackTrace();
        }
            	 
     }
     
   

%>
<jsp:include page="inc/footer.jsp">
	<jsp:param name="showNavigation" value="false"/>
	<jsp:param name="showCopyright" value="false"/>
</jsp:include>