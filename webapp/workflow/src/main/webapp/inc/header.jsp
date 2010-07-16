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
