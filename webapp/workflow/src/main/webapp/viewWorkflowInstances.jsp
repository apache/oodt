<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.List" import="java.util.Iterator"
    import="java.net.URL"
    import="java.text.NumberFormat"
    	import="gov.nasa.jpl.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient"
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance"
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTask"
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowStatus"
    	import="gov.nasa.jpl.oodt.cas.metadata.Metadata"
    	import="gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstancePage"
    	import="gov.nasa.jpl.oodt.cas.workflow.webapp.util.WorkflowInstanceMetMap"
    	import="gov.nasa.jpl.oodt.cas.workflow.webapp.util.WorkflowInstanceMetadataReader"
    	import="gov.nasa.jpl.oodt.cas.workflow.webapp.util.JspUtility"
    	import="gov.nasa.jpl.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager"%>
<jsp:include page="inc/header.jsp">
	<jsp:param name="metaRefresh" value="30"/>
</jsp:include>
<%
     String workflowMgrUrl = application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.mgr.url");
     XmlRpcWorkflowManagerClient client = new XmlRpcWorkflowManagerClient(new URL(workflowMgrUrl));
     
     WorkflowLifecycleManager lifecycleMgr = 
         new WorkflowLifecycleManager(application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.lifecycleFilePath"));
     
     WorkflowInstancePage instPage = null;  
     int pageSize = 20;
     int pageNum = Integer.parseInt(request.getParameter("page") != null ? 
             request.getParameter("page"):"1");
     String status = request.getParameter("status");
   
     
     try{
           if(status != null){
               instPage = client.paginateWorkflowInstances(pageNum, status);
           }
           else{
               instPage = client.paginateWorkflowInstances(pageNum);
           }
           
           int numInsts = -1;
           
           if(instPage.getTotalPages() == 1){
          	     numInsts = instPage.getPageWorkflows().size();
             }
             else if(instPage.getTotalPages() == 0){
          	     numInsts = 0;
             }
             else{
          	     numInsts = (instPage.getTotalPages()-1)*pageSize;
          	     
          	     //get the last page
          	     WorkflowInstancePage lastPage = null;
          	     
          	     try{
          	    	   lastPage = client.paginateWorkflowInstances(instPage.getTotalPages());
          	    	   numInsts+=lastPage.getPageWorkflows().size();
          	     }
          	     catch(Exception ignore){}
             }
             int endIdx = numInsts != 0 ? Math.min(numInsts, (pageSize)*(pageNum)):0;
             int startIdx = numInsts != 0 ? ((pageNum-1)*pageSize)+1:0;

    	    %>
    	    <p>Filter Workflows by Status:<br> |
    	    	     <%
    	    	      String statusListStr = application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.inst.statuses");
    	    	      if(statusListStr == null){
    	    	          statusListStr = "STARTED, FINISHED, METMISS, PAUSED"; /* default statuses */
    	    	      }
    	    	      
    	    	      String [] statuses = statusListStr.split(",");
    	    	      if(statuses != null && statuses.length > 0){
    	    	          for(int i=0; i < statuses.length; i++){
    	    	              %>
    	    	              <a href="./viewWorkflowInstances.jsp?status=<%=statuses[i] %>"><%=statuses[i] %></a>&nbsp;|
    	    	              <%
    	    	          }
    	    	      }
    	    	      
    	    	     %>
    	    <a href="./viewWorkflowInstances.jsp">ALL</a>&nbsp;|	    
    	    </p>
    	    <%

    	     if(instPage.getPageWorkflows() != null && instPage.getPageWorkflows().size() > 0){
    	         String metMapFilePath = application.getInitParameter("gov.nasa.jpl.oodt.cas.workflow.webapp.inst.metFields.filePath");
    	         WorkflowInstanceMetMap wInstMetMap = WorkflowInstanceMetadataReader.parseMetMapFile(metMapFilePath);
    	           
    	    	 
    	    	    %>
    	    	    <p>Workflows <b><%=(startIdx)%></b>-<b><%=(endIdx)%></b> of <b><%=numInsts%></b> total</p>
    	    	      <table cellspacing="3" cellpadding="4">
    	    	        <tr>
    	    	          <td>Workflow</td>
    	    	          <td align="center">Progress</td>
    	    	          <td align="center">Status</td>
    	    	          <td align="center">Execution Time (min)</td>
    	    	          <td align="center">Current Task Execution Time (min)</td>
    	    	          <td align="center">Current Task</td>
    	    	        </tr>
    	    	    <%
    	    	 
    	    	       for(Iterator i = instPage.getPageWorkflows().iterator(); i.hasNext(); ){
    	    	    	     WorkflowInstance inst = (WorkflowInstance)i.next();
    	    	    	     
    	    	    	     %>
    	    	    	       <tr>
    	    	    	         <td><a href="javascript:popWin('./viewWorkflow.jsp?workflowId=<%=inst.getWorkflow().getId()%>', '<%=inst.getWorkflow().getId() %>');"><%=inst.getWorkflow().getName() %></a>
    	    	    	         <br/>
    	    	    	         <p style="font-size:9px;">
    	    	    	         <%
    	    	    	         Metadata instMetadata = client.getWorkflowInstanceMetadata(inst.getId());
    	    	    	         List wInstFields = wInstMetMap.getFieldsForWorkflow(inst.getWorkflow().getId())
    	    	    	                         != null ? wInstMetMap.getFieldsForWorkflow(inst.getWorkflow().getId()):
    	    	    	                             wInstMetMap.getDefaultFields();
    	    	    	         StringBuffer metStrBuf = new StringBuffer();
    	    	    	         
    	    	    	         if(wInstFields != null && wInstFields.size() > 0){
    	    	    	             for(int j=0; j < wInstFields.size(); j++){
    	    	    	               metStrBuf.append((String)wInstFields.get(j));
    	    	    	               metStrBuf.append(":");
    	    	    	               metStrBuf.append(instMetadata.getMetadata((String)wInstFields.get(j)));
    	    	    	               metStrBuf.append(",");
    	    	    	             }
    	    	    	             
    	    	    	             metStrBuf.deleteCharAt(metStrBuf.length()-1);
    	    	    	             
    	    	    	             %>
    	    	    	             <%=metStrBuf.toString() %>
    	    	    	             
    	    	    	         </p>
    	    	    	         <%
    	    	    	         }
    	    	    	         %>
    	    	    	         
    	    	    	         </td>
    	    	    	         <%
    	    	    	           String workflowPctComplete = 
    	    	    	                WorkflowLifecycleManager.formatPct(lifecycleMgr.getPercentageComplete(inst)*100.0);
    	    	    	         
    	    	    	         %>
    	    	    	         <td align="center"><script>display('workflow_<%=inst.getId()%>_progress', <%=workflowPctComplete%>, 1);</script></td>
    	    	    	         <td align="center"><%=inst.getStatus()%></td>
    	    	    	         <%
    	    	    	           NumberFormat fn = NumberFormat.getNumberInstance();
    	    	    	           fn.setMaximumFractionDigits(2);
    	    	    	           fn.setMinimumFractionDigits(2);
    	    	    	           
    	    	    	         %>
    	    	    	         <td align="center"><%=fn.format(client.getWorkflowWallClockMinutes(inst.getId())) %></td>
    	    	    	         <td align="center"><%=fn.format(client.getWorkflowCurrentTaskWallClockMinutes(inst.getId())) %></td>
    	    	    	         <td align="center"><a href="./viewTask.jsp?taskId=<%=inst.getCurrentTaskId()%>"><%=JspUtility.getTaskNameFromTaskId(inst, inst.getCurrentTaskId()) %></a></td>
    	    	    	        </tr>

    	    	    	     
    	    	    	     <%
    	    	    	   
    	    	       }
    	    	 
    	    	   %>
    	    	   </table>
    	       <p>&nbsp;</p>
               <p>&nbsp;</p>
               <hr width="*">
    	       <div align="center">
               <table cellspacing="3" width="*">
                 <tr>
                   <td width="100" nowrap>Result Page</td>
                   
                   <% 
                     int numPages = instPage.getTotalPages();
                     int currPage = instPage.getPageNum();
                     int windowSize = 10;
                     
                     int startPage = Math.max(1, (currPage-(windowSize / 2)));
                     int endPage = Math.min(currPage+(windowSize / 2), numPages);
                   
                     for(int i=startPage; i <= endPage; i++){
                    	 
                    	 %>
                          <td><%if(currPage == i){ %><b><%} %><a <%if(currPage == i){ %>style="color:red;"<%} %> href="./viewWorkflowInstances.jsp?page=<%=i %><%if(status != null && !status.equals("")){ %>&status=<%=status %><%} %>"><%=i %></a><%if(currPage == i){ %></b><%} %></td>                    	 
                    	 <%
                    	 
                     }
                     
                     %>
                     
                </tr>
                </table>
                </div>
    	    	   
    	    	       <p>&nbsp;</p>
  	       	    	   <p>&nbsp;</p>
  	       	    	   <p>&nbsp;</p>
  	       	    	   <p>&nbsp;</p>
    	    	   
    	    	   <%
    	    	 
    	     }    	   
    	   
     }
     catch(Exception e){
    	   e.printStackTrace();
     }
     

%>
<jsp:include page="inc/footer.jsp"/>