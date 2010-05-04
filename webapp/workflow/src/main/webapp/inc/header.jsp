<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    	%>
    	
    	
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%if(request.getParameter("metaRefresh") != null){ %>
<meta http-equiv="refresh" content="<%=request.getParameter("metaRefresh") %>">
<%} %>
<title>Workflow Manager Monitor Web Application</title>
<script src="js/workflow.js" type="text/javascript">
</script>
<script src="js/prototype/prototype.js" type="text/javascript">
</script>
<script src="js/progress/progress.js" type="text/javascript">
</script>
<link rel="stylesheet" href="css/main.css" type="text/css"/>
<link rel="stylesheet" href="css/progress.css" type="text/css" media="screen"/>
</head>
<body>

<!-- begin main content -->