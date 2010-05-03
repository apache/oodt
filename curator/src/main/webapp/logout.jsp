
<%@page import="gov.nasa.jpl.oodt.cas.curation.util.SSOUtils"%>
<%@page import="gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn"%>
<%

//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

// use new single sign on API
SingleSignOn auth = SSOUtils.getWebSingleSignOn(application, request,response);

String refererUrl = request.getParameter("from");
auth.logout();
response.sendRedirect(refererUrl);
%>
