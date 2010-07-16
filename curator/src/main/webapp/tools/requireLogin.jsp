
<%@page import="org.apache.oodt.cas.curation.util.SSOUtils"%>
<%@page import="org.apache.oodt.security.sso.SingleSignOn"%>
<%
SingleSignOn auth = SSOUtils.getWebSingleSignOn(application, request,response);
String refererUrl = request.getRequestURI();

 if(!auth.isLoggedIn()){
   response.sendRedirect("login.jsp?from="+refererUrl);
 }
%>
