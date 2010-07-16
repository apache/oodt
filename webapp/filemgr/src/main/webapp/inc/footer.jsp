<%--
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
<!--  end main content -->
<%if(request.getParameter("showNavigation") == null || (request.getParameter("showNavigation") != null && !request.getParameter("showNavigation").equals("false"))){ %>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>

<table border="1" align="center">
  <tr>
    <td><a href="./index.jsp">Home</a></td>
    <td><a href="./findProduct.jsp">Browse for Products</a></td>
    <td><a href="./viewRecentProducts.jsp">View Recent Products</a></td>
    <td><a href="./getCurrentTransfers.jsp">View Current Transfers</a></td>
    <td><a href="./freeTextFindProduct.jsp">Free text query for products</a></td>
  </tr>
</table>
<%} %>

<%if(request.getParameter("showCopyright") == null || (request.getParameter("showCopyright") != null && !request.getParameter("showCopyright").equals("false"))){ %>
<div align="center">
  <p>Copyright &copy; 2006 Apache Software Foundation.</p>
</div>
<%} %>

</body>
</html>
