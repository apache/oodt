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
    	import="org.apache.oodt.cas.workflow.structs.WorkflowCondition"
    	%>
    	
<jsp:include page="inc/header.jsp"/>
<%
     String workflowMgrUrl = application.getInitParameter("org.apache.oodt.cas.workflow.webapp.mgr.url");
     XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(new URL(workflowMgrUrl));
     
     WorkflowTask task = null;
     String taskId = request.getParameter("taskId");

     
     if(taskId != null){
    	  try{
       	   task = client.getTaskById(taskId);

       	     if(task != null){
       	    	 
       	    	    %>
       	    	      <table>
       	    	        <tr>
       	    	          <td>Task ID</td>
       	    	          <td><%=task.getTaskId() %></td>
       	    	        </tr>
       	    	        <tr>       	    	          
       	    	          <td>Name</td>
       	    	          <td><%=task.getTaskName() %></td>
       	    	        </tr>
       	    	        <tr>
       	    	          <td>Implementation Class</td>
       	    	          <td><%=task.getTaskInstanceClassName() %></td>
       	    	        </tr>
       	    	        <tr>
       	    	          <td>Configuration</td>
       	    	          <td>
       	    	            <table>
       	    	             <tr>
       	    	               <td>Property</td>
       	    	               <td>Value</td>
       	    	             </tr>
       	    	              <%
       	    	               for(Iterator i = task.getTaskConfig().getProperties().keySet().iterator(); i.hasNext(); ){
       	    	            	   String propName = (String)i.next();
       	    	            	   String propValue = (String)task.getTaskConfig().getProperties().get(propName);
       	    	            	   
       	    	            	   %>
       	    	            	   <tr>
       	    	            	     <td><%=propName %></td>
       	    	            	     <td><%=propValue %></td>
       	    	            	   </tr>
       	    	            	   <%
       	    	            	   
       	    	               }
       	    	              %>
       	    	            </table>
       	    	           </td>
       	    	          </tr>
       	    	          <tr>
       	    	            <td>Conditions</td>
       	    	            <td>
       	    	              <table>
       	    	                <%
       	    	                 for(Iterator i = task.getConditions().iterator(); i.hasNext(); ){
       	    	                	WorkflowCondition c = (WorkflowCondition)i.next();
       	    	                	
       	    	                	%>
       	    	                	 <tr>
       	    	                	   <td><a href="javascript:popWin('./viewCondition.jsp?conditionId=<%=c.getConditionId()%>', '<%=c.getConditionId() %>');"><%=c.getConditionName() %></a></td>
       	    	                	 </tr>
       	    	                	<% 
       	    	                 }
       	    	                
       	    	                %>
       	    	              
       	    	              
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
