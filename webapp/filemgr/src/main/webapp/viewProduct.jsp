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
    import="java.util.List" import="java.util.Iterator"
    import="java.text.NumberFormat"
    	import="java.net.URL" import="java.net.MalformedURLException"
    	import="java.io.File" import="java.net.URI"
    	import="org.apache.oodt.cas.metadata.Metadata"
    	import="org.apache.oodt.cas.filemgr.structs.Product"
    	import="org.apache.oodt.cas.filemgr.structs.ProductType"
    	import="org.apache.oodt.cas.filemgr.structs.Reference"
    	import="org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient"    
%>
    	
<jsp:include page="inc/header.jsp"/>
<%
		  XmlRpcFileManagerClient fClient = null;
		  String fileManagerUrl = application.getInitParameter("filemgr.url") != null ? application.getInitParameter("filemgr.url"):"http://localhost:9000";
		  
			boolean clientConnect=true;
			try{
				  fClient = new XmlRpcFileManagerClient(new URL(fileManagerUrl));
			}
			catch(Exception e){
				  System.out.println("Exception when communicating with file manager, errors to follow: message: "+e.getMessage());
				  clientConnect=false;
			}
            
			
		   if(clientConnect){
      
            //get the Product ID, and the product Type ID
            String productID = request.getParameter("product_id");
            String productTypeID = request.getParameter("product_type_id");            
            
            if(productID == null || productTypeID == null){
            	  //do nothing
            }
            else{
                
                Product product = fClient.getProductById(productID);
                ProductType productType = fClient.getProductTypeById(productTypeID);
                product.setProductType(productType);
                Metadata m = fClient.getMetadata(product);
               
                if(m != null && m.getHashtable().size()  > 0){
                	
               
                %>
                <h3>Product: <%=m.getMetadata("CAS.ProductName") %></h3>
                <p>Percent Transferred: <%=NumberFormat.getPercentInstance().format(fClient.getProductPctTransferred(product)) %></p>
                <a href="#References">Jump to References</a>
                
                <h3>Metadata</h3>
                  <table>
                    <%
                      for(Iterator i = m.getHashtable().keySet().iterator(); i.hasNext(); ){
                    	    String elemName = (String)i.next();
                    	    List elemValues = m.getAllMetadata(elemName);
                    	    %>
                    	      <tr>
                    	        <td><%=elemName %></td>
                    	        <td>
							  <table>
							    <%for(Iterator j = elemValues.iterator(); j.hasNext(); ){
							    	   String elemValue = (String)j.next();
							    	%>
							    	<tr>
							    	  <td><%=elemValue %></td>
							    	</tr>
							    	<%
							    }
							    %>
							  </table>
							</td>
                    	      </tr>
                    	    <%
                    	  
                      }
                    
                    %>
                    
                    
                  
                  </table>
                <%
                
                }
                else{
                	
                	  %>
                	   <h3>No Product Metadata!</h3>
                	  <%
                }
                
                
                List references = fClient.getProductReferences(product);
                
                if(references != null && references.size() > 0){
                	
               
                %>
                <p>&nbsp;</p>
                <p>&nbsp;</p>
                <a name="References"></a>
                <h3>References</h3>
                  <table>
                    <tr>
                      <td>File Location</td>
                      <td>File Size</td>
                      <td>Percent Transferred</td>
                    </tr>
                    
                    <%
                      for(Iterator i = references.iterator(); i.hasNext(); ){
                    	    Reference r = (Reference)i.next();
                    	    String filePath = null;
                    	    try{
                    	        filePath = new File(new URI(r.getDataStoreReference())).getAbsolutePath();
                    	    }
                    	    catch(Exception ignore){}
                    	    
                    	    %>
                    	      <tr>
                    	        <td><a href="<%=r.getDataStoreReference() %>"><%=filePath %></a></td>
                    	        <td><%=r.getFileSize() %></td>
                    	        <td><%=NumberFormat.getPercentInstance().format(fClient.getRefPctTransferred(r)) %></td>
                    	      </tr>
                    	    <%
                    	  
                      }
                    
                    %>
                    
                    
                  
                  </table>
                <%
                
                }
                else{
                	
                	  %>
                	   <h3>No Product References!</h3>
                	  <%
                }
            }
		   }

            
%>

<jsp:include page="inc/footer.jsp"/>
