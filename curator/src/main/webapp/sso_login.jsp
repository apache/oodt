
<%@page import="gov.nasa.jpl.oodt.cas.curation.util.SSOUtils"%>
<%@page import="gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn"%>
<%

//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

// use new single sign on API
SingleSignOn auth = SSOUtils.getWebSingleSignOn(application, request,response);
String ldapUser = request.getParameter("username");
String ldapPass = request.getParameter("password");
String refererUrl = request.getParameter("from");

if (auth.login(ldapUser, ldapPass)) {
	response.sendRedirect(refererUrl);
} else {
	if (auth.getLastConnectionStatus()) {
	    response.sendRedirect("login.jsp?loginFail=true&from="+refererUrl);
	} else {
		response.sendRedirect("login.jsp?loginConnectFail=true&from="+refererUrl);
	}
}
%>