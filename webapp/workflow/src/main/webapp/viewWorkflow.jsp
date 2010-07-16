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
    import="java.util.Iterator"
    import="java.net.URL"
    	import="org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient"
    	import="org.apache.oodt.cas.workflow.structs.Workflow"
    	import="org.apache.oodt.cas.workflow.structs.WorkflowTask"
    	import="org.apache.oodt.cas.workflow.webapp.util.JspUtility"
%>
    	
<jsp:include page="inc/header.jsp"/>
<%
     String workflowMgrUrl = application.getInitParameter("org.apache.oodt.cas.workflow.webapp.mgr.url");
     XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(new URL(workflowMgrUrl));
     
     Workflow workflow = null;
     String workflowId = request.getParameter("workflowId");

     
     if(workflowId != null){
    	  try{
       	   workflow = client.getWorkflowById(workflowId);

       	     if(workflow != null){
       	    	 
       	    	    %>
       	    	      <table>
       	    	        <tr>
       	    	          <td>Workflow ID</td>
       	    	          <td><%=workflow.getId() %></td>
       	    	        </tr>
       	    	        <tr>       	    	          
       	    	          <td>Name</td>
       	    	          <td><%=workflow.getName() %></td>
       	    	        </tr>
       	    	        <tr>
       	    	          <td colspan="2">
 					 <table width="<%=((75*workflow.getTasks().size()) + (115*(workflow.getTasks().size()-1))) %>" height="75">
    	    	    	             <tr valign="middle">
    	    	    	               <%
    	    	    	                 for(Iterator j = workflow.getTasks().iterator(); j.hasNext(); ){
    	    	    	                	   WorkflowTask t = (WorkflowTask)j.next();
    	    	    	                	   
    	    	    	                	   %>
    	    	    	                	     <td nowrap align="center" width="75" height="75" background="images/task_circle.jpg"><a href="javascript:popWin('./viewTask.jsp?taskId=<%=t.getTaskId()%>', '<%=t.getTaskId()%>');"><%=JspUtility.summarizeWords(t.getTaskName(), 7, 16)%></a></td>
    	    	    	                	   <%
    	    	    	                	   
    	    	    	                	   if(j.hasNext()){
    	    	    	                		   %>
    	    	    	                		    <td align="center" width="115" height="16"><img src="images/task_arrow.jpg"></td>
    	    	    	                		   <%
    	    	    	                	   }
    	    	    	                 }
    	    	    	               %>
    	    	    	              </tr>
    	    	    	           </table>      	    	          
       	    	          
       	    	          </td>
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
