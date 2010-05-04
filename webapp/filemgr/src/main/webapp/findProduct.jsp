<!--
Copyright (c) 2005, California Institute of Technology.
ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.

$Id$
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="java.util.Iterator" import="java.net.URL"
    import="java.net.MalformedURLException" import="java.util.List"
    import="java.util.Collections" import="java.util.Comparator"
    import="gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient"
    import="gov.nasa.jpl.oodt.cas.filemgr.structs.Product"
    import="gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType"
    	%>
    	
    	
<%@page import="java.util.Comparator"%>
<jsp:include page="inc/header.jsp"/>
<%
			XmlRpcFileManagerClient fClient = null;
			String fileManagerUrl = 
				application.getInitParameter("filemgr.url") != null ? 
						application.getInitParameter("filemgr.url"):"http://localhost:9000";
			
			boolean clientConnect=true;
			try{
				  fClient = new XmlRpcFileManagerClient(new URL(fileManagerUrl));
			}
			catch(Exception e){
				  System.out.println("Exception when communicating with file manager, errors to follow: message: "+e.getMessage());
				  clientConnect=false;
			}
            
			
		   if(clientConnect){
			   List productTypes = fClient.getProductTypes();
			   
	            if(productTypes != null){

	                %>
	                <h3>Product Types</h3>
	                
	                <table border="1" cellspacing="2" cellpadding="2" width="800">
	                <%
	                int count = 0;
	                
	                // sort the product types first by their name
	                Collections.sort(productTypes, new Comparator(){
	                    public int compare(Object o1, Object o2){
	                        ProductType p1 = (ProductType)o1;
	                        ProductType p2 = (ProductType)o2;
	                        
	                        String p1Name = p1.getName();
	                        String p2Name = p2.getName();
	                        
	                        return p1Name.compareTo(p2Name);
	                        
	                    }
	                });
	                
	                
	               for(Iterator i = productTypes.iterator(); i.hasNext(); ){
	            	     ProductType type = (ProductType)i.next();
	                  String productTypeName = type.getName();
	                  
	                  
	                  if(count % 2 == 0){
	                        %>
	                        <tr>
	                        <%
	                  }

	                  
	                  int productCount = -1;
	                  
	                  productCount = fClient.getNumProducts(type);
	                  String productTypeId = type.getProductTypeId();

	                  %>
	                  <td><%=productTypeName %>&nbsp;(<a href="./findProductsByType.jsp?page=1&cq=y&typeId=<%=productTypeId%>&typeName=<%=productTypeName %>"><%=productCount%></a>)</td>
	                  <%

	                  count++;
	                  if(count % 2 == 0){
	                    %>
	                    </tr>
	                    <%
	                  }

	               }

	               %>
	               </tr>
	               </table>
	               <%
	 
	            }
	            else{

	                %>
	                <h3>No Products Found!</h3>
	              <%
	            }			   
		   }

              
  %>

<jsp:include page="inc/footer.jsp" />