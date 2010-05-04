

<!--  end main content -->
<%if(request.getParameter("showNavigation") == null || (request.getParameter("showNavigation") != null && !request.getParameter("showNavigation").equals("false"))){ %>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>

<table border="1" align="center">
  <tr>
    <td><a href="./index.jsp">Home</a></td>
    <td><a href="./viewWorkflowInstances.jsp">View Workflow Instances</a></td>
    <td><a href="./viewWorkflows.jsp">View Workflow Descriptions</a></td>
    <td><a href="./viewEventToWorkflowMap.jsp">View Event Workflow Map</a></td>
  </tr>
</table>
<%} %>

<%if(request.getParameter("showCopyright") == null || (request.getParameter("showCopyright") != null && !request.getParameter("showCopyright").equals("false"))){ %>
<div align="center">
  <p>Copyright &copy; 2006 California Institute of Technology.</p>
</div>
<%} %>

</body>
</html>