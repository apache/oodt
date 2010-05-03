<%
// Copyright (c) 2008, California Institute of Technology.
// ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

// $Id$
%>
<jsp:include page="views/common/cas-curator-header.jsp" />
<div id="page">
<h3> There was an error processing your request.</h3>
<%= session.getAttribute("errorMsg") %>
</div>
<jsp:include page="views/common/cas-curator-footer.jsp" />