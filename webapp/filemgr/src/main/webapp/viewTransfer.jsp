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
    	import="org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient"   
    	import="org.apache.oodt.cas.filemgr.structs.FileTransferStatus"
    	import="org.apache.oodt.cas.filemgr.structs.Reference"
    	import="org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException"
    	import="java.util.List"
    	import="java.text.NumberFormat"
    	import="java.net.URL"
    	import="java.net.MalformedURLException"
    	import="java.util.Iterator"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>File Manager Web Application</title>
<script src="js/filemgr.js" type="text/javascript">
</script>
<link rel="stylesheet" href="css/main.css" type="text/css"/>

<%
            
	  XmlRpcFileManagerClient fClient = null;
	  String fileManagerUrl = application.getInitParameter("filemgr.url") != null ? application.getInitParameter("filemgr.url"):"http://localhost:9000";

		try{
			  fClient = new XmlRpcFileManagerClient(new URL(fileManagerUrl));
		}
		catch(Exception e){
			  System.out.println("Exception when communicating with file manager, errors to follow: message: "+e.getMessage());
		}
		
	  String fileRef = request.getParameter("ref");
	  long fileSize = Long.valueOf(request.getParameter("size")).longValue();
	     
%>    
</head>
<body>

<!-- begin main content -->

  <h3>View Transfer: <%=fileRef %></h3>
  
  <%
     if(fileRef != null){
         double pctComplete = 0.0;
         Reference ref = new Reference();
         ref.setOrigReference(fileRef);
         ref.setDataStoreReference("file:/foo/bar");
         ref.setFileSize(fileSize);
         
         
         try{
        	    pctComplete = fClient.getRefPctTransferred(ref);
         }
         catch(DataTransferException e){
        	     System.out.println("Error communicating with the file manager to obtain transfer status for ref: "+fileRef+": Message: "+e.getMessage());
         }
         
        %>
        	     <table>
        	       <tr>
        	         <td>File Reference</td>
        	         <td>Percent Complete</td>
        	      </tr>
        	        	       <tr>
        	        	         <td><%=fileRef %></td>
        	        	         <td><%=NumberFormat.getPercentInstance().format(pctComplete) %></td>
        	        	       </tr>
        	     </table>
        	   
        	   <%
         }     
 %>

   
<jsp:include page="inc/footer.jsp" />
