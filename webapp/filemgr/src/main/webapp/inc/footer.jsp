

<!--  end main content -->
<%if(request.getParameter("showNavigation") == null || (request.getParameter("showNavigation") != null && !request.getParameter("showNavigation").equals("false"))){ %>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>

<table border="1" align="center">
  <tr>
    <td><a href="./index.jsp">Home</a></td>
    <td><a href="./findProduct.jsp">Browse for Products</a></td>
    <td><a href="./viewRecentProducts.jsp">View Recent Products</a></td>
    <td><a href="./getCurrentTransfers.jsp">View Current Transfers</a></td>
    <td><a href="./freeTextFindProduct.jsp">Free text query for products</a></td>
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