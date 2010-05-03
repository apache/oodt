<%@page import="gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn"%>
<%
//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

 SingleSignOn auth = new SingleSignOn(response, request);
 String refererUrl = request.getRequestURI();
 
 if(auth.isLoggedIn()){
   %>
    Logged in as <%=auth.getCurrentUsername() %>. <a href="<%=request.getContextPath() %>/logout.jsp?from=<%=refererUrl%>">Log Out</a>
   <%
  }
  else{
   %>
    Not logged in. <a href="<%=request.getContextPath() %>/login.jsp?from=<%=refererUrl%>">Log In</a>
   <%
  }
%>