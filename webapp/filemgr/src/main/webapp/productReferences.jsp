<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.List" import="java.util.Iterator"
    import="java.net.URI" import="java.io.File"
    import="java.net.URL" import="java.net.MalformedURLException"
    import="gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient"
    	import="gov.nasa.jpl.oodt.cas.filemgr.structs.Reference"
    	import="gov.nasa.jpl.oodt.cas.filemgr.structs.Product"
    	import="gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType"
    	import="java.text.NumberFormat"
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
                List references = fClient.getProductReferences(product);
                
                if(references != null && references.size() > 0){
                	
               
                %>
                  <table>
                    <tr>
                      <td>File Location</td>
                      <td>Size</td>
                      <td>Percent transferred</td>
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

<jsp:include page="inc/footer.jsp">
   <jsp:param name="showNavigation" value="false"/>
   <jsp:param name="showCopyright" value="false"/>
</jsp:include>
