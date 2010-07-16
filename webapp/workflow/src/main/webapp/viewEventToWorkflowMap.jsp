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
    	%>
    	
<jsp:include page="inc/header.jsp"/>
<%
     String workflowMgrUrl = application.getInitParameter("org.apache.oodt.cas.workflow.webapp.mgr.url");
     XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(new URL(workflowMgrUrl));
     
     List events = null;
     String eventName = request.getParameter("event_name");
     
     try{
    	   events = client.getRegisteredEvents();
    	     
    	     if(events != null){
    	    	   %>
    	    	   <h3>Event to Workflow Map</h3>
    	    	   <p>Select an event, and then click on "Get Workflows" to obtain a list of workflows associated
    	    	   with the selected event.</p>
    	    	   
    	    	   <form method="POST" action="./viewEventToWorkflowMap.jsp" name="f1" id="f1">
    	    	    <table>
    	    	       <tr>
    	    	         <td>Event</td>
    	    	         <td><select name="event_name" id="event_name">
    	    	           <%
    	    	             for(Iterator i = events.iterator(); i.hasNext(); ){
    	    	            	   String event = (String)i.next();
    	    	            	   
    	    	            	   %>
    	    	            	    <option value="<%=event %>" <%if(eventName != null){if(eventName.equals(event)){ %>selected<%}}%>><%=event %></option>
    	    	            	   <%
    	    	             }
    	    	           
    	    	           %>
    	    	         
    	    	         </select>
    	    	         </td>
    	    	         <td><input type="submit" name="getWorflows" value="Get Worfklows!"></td>
    	    	       </tr>
    	    	    </table>
    	    	    </form>
    	    	    
    	    	    
    	    	   
    	    	   <%
    	     }
    	     
    	     
    	     if(eventName != null){
    	    	 List workflows = client.getWorkflowsByEvent(eventName);
    	    	 
    	    	 %>
    	    	  <hr>
    	    	  <h4>Workflows for <%=eventName %> event</h4>
    	    	  
    	    	  <table>
    	    	    <tr>
    	    	     <td>Workflow</td>
    	    	    </tr>
    	    	 <%
    	    	 
    	    	 for(Iterator i = workflows.iterator(); i.hasNext(); ){
    	    		 Workflow w = (Workflow)i.next();
    	    		
    	    		 %>
    	    		  <tr>
    	    		    <td><a href="javascript:popWin('./viewWorkflow.jsp?workflowId=<%=w.getId() %>', '<%=w.getId() %>');"><%=w.getName() %></a></td>
    	    		  </tr>
    	    		 <%
    	    	 }
    	    	 
    	    	 %>
    	    	 </table>
    	    	 <%
    	     }
    	     %>
    	     
    	     <%
     }
     catch(Exception e){
     	   e.printStackTrace();
      }   


%>
<jsp:include page="inc/footer.jsp"/>
