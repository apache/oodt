<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    	import="gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient"   
    	import="gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType"
    	import="java.util.List"
    	import="java.net.URL"
    	import="java.net.MalformedURLException"
    	import="java.util.Iterator"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>File Manager Web Application</title>
<script src="js/filemgr.js" type="text/javascript">
</script>
<link rel="stylesheet" href="css/main.css" type="text/css"/>

<%
            
	  XmlRpcFileManagerClient fClient = null;
	  String fileManagerUrl = application.getInitParameter("filemgr.url") != null ? application.getInitParameter("filemgr.url"):"http://localhost:9000";

		boolean clientConnect=true;
		try{
			  fClient = new XmlRpcFileManagerClient(new URL(fileManagerUrl));
		}
		catch(Exception e){
			  System.out.println("Exception when communicating with file manager, errors to follow: message: "+e.getMessage());
			  clientConnect=false;
		}
        
		
	   if(clientConnect){
  
  List productTypes = fClient.getProductTypes();
           

%>
<%
  for(Iterator i = productTypes.iterator(); i.hasNext(); ){
	  ProductType type = (ProductType)i.next();
	  
	  %>
<link rel="alternate" type="application/rss+xml" title="<%=type.getName() %>" href="./viewRecent?channel=<%=type.getName()%>&id=<%=type.getProductTypeId() %>">	  
	  <%
  }
%>
   
</head>
<body>

<!-- begin main content -->

  <h3>View Recent Products By Product Type</h3>
  
  
  <p>Click on any of the links below to obtain a feed for each product type:
    <ul>
     
     <% 
           for(Iterator i = productTypes.iterator(); i.hasNext(); ){
        	      ProductType type = (ProductType)i.next();
        	      
        	      %>
        	       <li><a href="./viewRecent?channel=<%=type.getName()%>&id=<%=type.getProductTypeId() %>">Recent <%=type.getName() %>s</a> 
        	      <%
        	      
           }
           
           %>
           <li><a href="./viewRecent?channel=ALL">All Recent Products</a></li>
           <%
           
               
           
     %>
    </ul>
    
 <%} %>
   
<jsp:include page="inc/footer.jsp" />