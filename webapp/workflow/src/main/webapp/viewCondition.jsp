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
    	import="org.apache.oodt.cas.workflow.structs.WorkflowCondition"
    	%>
    	
<jsp:include page="inc/header.jsp"/>

<%
     String workflowMgrUrl = application.getInitParameter("org.apache.oodt.cas.workflow.webapp.mgr.url");
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
