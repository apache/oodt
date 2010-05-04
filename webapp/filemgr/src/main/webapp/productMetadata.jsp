<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.List" import="java.util.Iterator"
    	import="java.net.URL" import="java.net.MalformedURLException"
    	import="gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient"
    	import="gov.nasa.jpl.oodt.cas.filemgr.structs.Product"
    	import="gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType"
    	import="gov.nasa.jpl.oodt.cas.metadata.Metadata"
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
                
                if(m != null && m.getHashtable().keySet().size()  > 0){
                	
               
                %>
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
            }
		   }

            
%>

<jsp:include page="inc/footer.jsp">
   <jsp:param name="showNavigation" value="false"/>
   <jsp:param name="showCopyright" value="false"/>
</jsp:include>