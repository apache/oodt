<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<jsp:include page="inc/header.jsp"/>

  <h3>Welcome to the <%= application.getInitParameter("gov.nasa.jpl.oodt.cas.filemgr.webapp.display.name") %>!</h3>
  
  
  <p>You can:
    <ul>
      <li><a href="./findProduct.jsp">Browse for a product in the file manager</a>.</li>
      <li><a href="./freeTextFindProduct.jsp">Find a product using a free text query</a>.</li>
      <li><a href="./viewRecentProducts.jsp">View recent products in the file manager.</a></li>
      <li><a href="./getCurrentTransfers.jsp">View current transfers to the file manager.</a></li>
    </ul>
   
<jsp:include page="inc/footer.jsp">
   <jsp:param name="showNavigation" value="false"/>
</jsp:include>