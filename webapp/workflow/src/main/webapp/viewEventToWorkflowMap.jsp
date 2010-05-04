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
    	%>
    	
<jsp:include page="inc/header.jsp"/>
<%
     String workflowMgrUrl = application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.mgr.url");
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