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

	String step = "choosePolicy";	// This is the default step
	
	// If the request includes a step, override the default
	if (request.getParameter("step") != null) {
		step = (String) request.getParameter("step");
	}
	
	// If the request includes a dataset collection, add it to the session
	if (request.getParameter("dsCollection") != null) {
		session.setAttribute("dsCollection",request.getParameter("dsCollection"));
	} 
	
	// If the request includes a dataset, add it to the session 
	if (request.getParameter("ds") != null) {
		session.setAttribute("ds",request.getParameter("ds"));
	}
	
	//Determine the path to the requested view
	String viewRequested = (String) ("views/addData/" + step + ".jsp");
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
			"Ingest Data:/ingestData.jsp",
			"Add Data Products:/addData.jsp",
			step});
	
	// Build the page
%>
<jsp:include page="views/common/cas-curator-header.jsp" />
<jsp:include page="<%=viewRequested%>"/>
<jsp:include page="views/common/cas-curator-footer.jsp"  />
