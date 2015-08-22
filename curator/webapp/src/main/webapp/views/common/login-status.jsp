<%@page import="org.apache.oodt.security.sso.SingleSignOn"%>
<%@page import="org.apache.oodt.cas.curation.util.SSOUtils"" %>
<%
// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

 SingleSignOn auth = SSOUtils.getWebSingleSignOn(application, request,response);
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
