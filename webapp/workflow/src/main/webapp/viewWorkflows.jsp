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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.List" import="java.util.Iterator"
    import="java.net.URL"
    	import="org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient"
    	import="org.apache.oodt.cas.workflow.structs.Workflow"
    	import="org.apache.oodt.cas.workflow.structs.WorkflowTask"
    	%>
    	
<jsp:include page="inc/header.jsp"/>
<%
     String workflowMgrUrl = application.getInitParameter("org.apache.oodt.cas.workflow.webapp.mgr.url");
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
