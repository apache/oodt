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
    import="org.apache.oodt.cas.filemgr.structs.Product"
    import="org.apache.oodt.cas.filemgr.structs.ProductPage"
    import="org.apache.oodt.cas.filemgr.structs.ProductType"
    import="org.apache.oodt.cas.filemgr.structs.Element"
    import="org.apache.oodt.cas.filemgr.structs.TermQueryCriteria"
    import="org.apache.oodt.cas.filemgr.structs.Query"
    import="org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException"
    import="org.apache.oodt.cas.metadata.Metadata"
    import="org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient"
    import="org.apache.oodt.cas.commons.pagination.PaginationUtils"
    import="java.net.URL"
    import="java.net.MalformedURLException"
    import="java.util.Iterator"
    import="java.util.List"
    import="java.text.NumberFormat"
    import="java.util.HashMap"
    	%>
    	
    	
<jsp:include page="inc/header.jsp"/>

<%
if(request.getParameter("cq") != null && request.getParameter("cq").equals("y")){
session.setAttribute("productQuery", null);
}
%>
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
		  

           String productTypeName = request.getParameter("typeName");
           String productTypeId = request.getParameter("typeId");
           ProductPage prodPage = null;  
           int pageSize = 20;
           int pageNum = -1;
           
           if(productTypeName != null && productTypeId != null){
           List products = null;        	   
           ProductType productType = fClient.getProductTypeById(productTypeId);
           Query query = null;

           if(session.getAttribute("productQuery") != null){
               query = (Query)session.getAttribute("productQuery");
           }
           else{
               query = new Query();
               session.setAttribute("productQuery", query);
           }


           //check to see if they wanted to add or remove criteria from it

           if(request.getParameter("CMD") != null){
                String cmd = request.getParameter("CMD");

                if(cmd.equals("REMOVE_CRITERIA")){
                    //need to know the index of the query criteria to remove
                    int removeIndex = Integer.valueOf(request.getParameter("remove_criteria_id")).intValue();
                    query.getCriteria().remove(removeIndex);
                }
                else if(cmd.equals("ADD_CRITERIA")){
                  //need to get the new criteria to add
                    String elementId = request.getParameter("element_id");
                    String elemValue = request.getParameter("element_value");

                    TermQueryCriteria c = new TermQueryCriteria();
                    c.setElementId(elementId);
                    c.setValue(elemValue);
                    query.getCriteria().add(c);
                    
                }
                
                pageNum = 1;
                
                try{
                    prodPage  = fClient.pagedQuery(query, productType, pageNum);
                    products = prodPage.getPageProducts();
                }
                catch(CatalogException ignore){}
           }
           else{
              pageNum = Integer.parseInt(request.getParameter("page"));
 
               try{
            	      prodPage = fClient.pagedQuery(query, productType, pageNum);
                   products = prodPage.getPageProducts();
               }
               catch(CatalogException ignore){}
           }
           
           int numProducts = -1;
           
           
           if(prodPage.getTotalPages() == 1){
        	     numProducts = prodPage.getPageProducts().size();
           }
           else if(prodPage.getTotalPages() == 0){
        	     numProducts = 0;
           }
           else{
        	     numProducts = (prodPage.getTotalPages()-1)*pageSize;
        	     
        	     //get the last page
        	     ProductPage lastPage = null;
        	     
        	     try{
        	    	   lastPage = fClient.pagedQuery(query, productType, prodPage.getTotalPages());
        	    	   numProducts+=lastPage.getPageProducts().size();
        	     }
        	     catch(Exception ignore){}
           }
           int endIdx = numProducts != 0 ? Math.min(numProducts, (pageSize)*(pageNum)):0;
           int startIdx = numProducts != 0 ? ((pageNum-1)*pageSize)+1:0;

           
                %>
                <h3><%=productTypeName %> Products</h3>
                
                <p>Products <b><%=(startIdx)%></b>-<b><%=(endIdx)%></b> of <b><%=numProducts%></b> total</p>
                
                
               <h3>Existing Criteria</h3>
               
               <form method="POST" action="findProductsByType.jsp" name="f2" id="f2">
                <input type="hidden" name="CMD" value="REMOVE_CRITERIA">
               <input type="hidden" name="typeId" value="<%=productTypeId %>">
               <input type="hidden" name="typeName" value="<%=productTypeName %>"> 
               <input type="hidden" name="remove_criteria_id" value="">             
               
               
               <table>
                 
               <%
                 HashMap elementMap = new HashMap();
               
                  for(int i = 0; i < query.getCriteria().size(); i++){
                     TermQueryCriteria criteria = (TermQueryCriteria)query.getCriteria().get(i);
                     String elementId = criteria.getElementId();
                     Element criteriaElement = fClient.getElementById(elementId);
                     elementMap.put(elementId, criteriaElement);
                     %>
                     <tr>
                       <td>
                         <%=criteriaElement.getElementName() %> = <%=criteria.getValue() %>
                       </td>
                       <td><input type="button" name="Remove" value="Remove" onClick="javascript:document.f2.remove_criteria_id.value = '<%=i %>'; document.f2.submit();"></td>
                     </tr>
                     <%

                  }
                %>
               </table>
               </form>
               
               
               <h3>Specify New Criteria</h3>
               
               
               
               <form method="POST" action="findProductsByType.jsp" name="f1" id="f1">
               <input type="hidden" name="CMD" value="ADD_CRITERIA">
               <input type="hidden" name="typeId" value="<%=productTypeId %>">
               <input type="hidden" name="typeName" value="<%=productTypeName %>">
               
               <table>
                 <tr>
                   <td><select name="element_id">
                   
               <%

                List elements = fClient.getElementsByProductType(productType);

                for(Iterator i = elements.iterator(); i.hasNext(); ){
					Element element = (Element)i.next();

					%>
					<option value="<%=element.getElementId()%>"><%=element.getElementName() %></option>
					<%

                }


                %>
                </select>
                </td>
                <td>
                  <input type="text" size="20" maxlength="255" name="element_value">
                </td>
                <td>
                  <input type="button" name="Add" value="Add" onClick="javascript:document.f1.submit();">
                </td>
                </tr>
                </table>
                
                </form>
                
                <table border="1" cellspacing="2" cellpadding="2" width="800">
                 <tr>
                   <td>Product</td>
                   <td>Transfer Status</td>
                   <td>Percent Complete</td>
                   <td>Received Time</td>
                   <td>References</td>
                   <td>Metadata</td>
                 </tr>
               <%
               if(products != null && products.size() > 0){
                 for(Iterator i = products.iterator(); i.hasNext(); ){
                   Product p = (Product)i.next();
                   p.setProductType(productType);
                   Metadata metadata = fClient.getMetadata(p);
                   String receivedTime = metadata.getMetadata("CAS.ProductReceivedTime");

                   %>
                    <tr>
                     <td><a href="./viewProduct.jsp?product_id=<%=p.getProductId()%>&product_type_id=<%=p.getProductType().getProductTypeId()%>"><%=p.getProductName() %></a></td>
                     <td><%=p.getTransferStatus() %></td>
                     <td><%=NumberFormat.getPercentInstance().format(fClient.getProductPctTransferred(p))%></td>
                     <td><%=receivedTime %></td>
                     <td><a href="javascript:popWin('./productReferences.jsp?product_id=<%=p.getProductId() %>&product_type_id=<%=p.getProductType().getProductTypeId() %>');">View References</a></td>
            	     <td><a href="javascript:popWin('./productMetadata.jsp?product_id=<%=p.getProductId() %>&product_type_id=<%=p.getProductType().getProductTypeId() %>');">View Metadata</a></td>
                   </tr>
                   <%

                 }

                %>
               </table>
               
               <p>&nbsp;</p>
               <p>&nbsp;</p>
               <hr width="*">
               <div align="center">
               <table cellspacing="3" width="*">
                 <tr>
                   <td width="100" nowrap>Result Page</td>
                   
                   <% 
                     int numPages = prodPage.getTotalPages();
                     int currPage = prodPage.getPageNum();
                     int windowSize = 10;
                     
                     int startPage = Math.max(1, (currPage-(windowSize / 2)));
                     int endPage = Math.min(currPage+(windowSize / 2), numPages);
                   
                     for(int i=startPage; i <= endPage; i++){
                    	 
                    	 %>
                          <td><%if(currPage == i){ %><b><%} %><a <%if(currPage == i){ %>style="color:red;"<%} %> href="./findProductsByType.jsp?page=<%=i %>&typeName=<%=productTypeName%>&typeId=<%=productTypeId %>"><%=i %></a><%if(currPage == i){ %></b><%} %></td>                    	 
                    	 <%
                    	 
                     }
                     
                     %>
                     
                </tr>
                </table>
                </div>
               <%
 
            }
            else{

                %>
                <h3>No Products Found!</h3>
              <%
            }

           }
		   }
              
  %>

<jsp:include page="inc/footer.jsp" />
