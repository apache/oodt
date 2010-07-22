<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<%@ page
	import="java.io.File"
	import="java.util.List"
	import="java.util.ArrayList"
	import="java.util.Iterator"
	import="java.util.Map"
	import="org.apache.oodt.cas.curation.policymgr.CurationPolicyManager"
%>
<%@ include file="tools/requireLogin.jsp"%>
<%
	//Get the base path of the application
	String deployedBase   = application.getRealPath("/");

	String step = "chooseDatasetCollection";	// This is the default step
	
	// If the request includes a step, override the default
	if (request.getParameter("step") != null) {
		step = (String) request.getParameter("step");
	}
	
	//	 If the request includes a dataset collection, add it to the session
	if (request.getParameter("dsCollection") != null) {
		session.setAttribute("dsCollection",request.getParameter("dsCollection"));
	} 
	
	// If the request includes a dataset, add it to the session 
	if (request.getParameter("ds") != null) {
		session.setAttribute("ds",request.getParameter("ds"));
	}

	//Determine the path to the requested view
	String viewRequested = (String) ("views/manageDataset/" + step + ".jsp");
	String viewPath = deployedBase + "/" + viewRequested;

	//Redirect on error
	if ( ! (new File(viewPath).exists()) ) {
		session.setAttribute("errorMsg","404 - Not Found");
		response.sendRedirect("error.jsp");
		return;
	}
	
	// Build the breadcrumbs for the page
	// Breadcrumb specification format: label or label:url
	// if :url not provided, breadcrumb will not be clickable
	// if url begins with '/', context path will be prepended
	session.setAttribute("breadcrumbs",
		new String[] {
			"Manage Dataset Definitions:/manageDataset.jsp",
			step});
	
	// Build the page
%>
<jsp:include page="views/common/cas-curator-header.jsp" />
<jsp:include page="<%=viewRequested%>"/>
<jsp:include page="views/common/cas-curator-footer.jsp"  />
