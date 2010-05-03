<%@ page
	import="java.io.File"
	import="java.util.List"
	import="java.util.ArrayList"
	import="java.util.Iterator"
	import="java.util.Map"
	import="gov.nasa.jpl.oodt.cas.curation.policymgr.CurationPolicyManager"
%>
<%@ include file="tools/requireLogin.jsp" %>
<% String projectName =  application.getInitParameter(CuratorConfMetKeys.PROJECT_DISPLAY_NAME);%>

<%
// Build Breadcrumbs for the page
// Breadcrumb specification format: label or label:url
// if :url not provided, breadcrumb will not be clickable
// if url begins with '/', context path will be prepended
session.setAttribute("breadcrumbs",new String[] {"Add Data to "+(projectName != null ? projectName:"")+" CAS"});
%>


<%@page import="gov.nasa.jpl.oodt.cas.curation.servlet.CuratorConfMetKeys"%>
<jsp:include page="views/common/cas-curator-header.jsp" />
<!-- tree view css + js -->
<link rel="stylesheet" type="text/css" href="js/jquery-treeview/jquery.treeview.css"/>
<link rel="stylesheet" type="text/css" href="js/jquery-ui/css/smoothness/jquery-ui-1.7.2.custom.css"/>
<script type="text/javascript" src="js/jquery-1.3.2.js"></script>
<script type="text/javascript" src="js/jquery-ui/jquery-ui-1.7.2.custom.min.js"></script>
<script type="text/javascript" src="js/jquery/jquery.blockUI.js"></script>
<script type="text/javascript" src="js/jquery-treeview/jquery.treeview.js"></script>
<script type="text/javascript" src="js/jquery-treeview/jquery.treeview.async.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		// Load up the tree view for the staging area
		$("#policyView").treeview({
			url: "showExistingPolicies"
			
		});
		// Enable the tabbed interface
		$("#newDatasetCollection").css('display','block');
		$(".tabContainer").tabs();
	});
	
	function treeSelection(a) {
		id = $(a).attr('href');
		$("#dsCollection").attr('value',id);
		$("#submitButton").attr('disabled',false);
		return false;
	}
	
	
	
	$
</script>
<!--  end tree view css + js -->
<div>
	<div id="page-sheet">
		<h4>Add Data to <%=(projectName != null ? projectName:"")%> CAS:</h4>
	
	<div class="tabContainer">
		<ul>
		  <li><a href="#existingDatasetCollection">Existing Dataset Collection</a></li>
		  <li><a href="#newDatasetCollection">New Dataset Collection</a></li>
		</ul>
		<div id="existingDatasetCollection" style="background:url(media/img/icon-new-product.png) scroll 5px 10px no-repeat;">
			<span class="tabLabel">Add products to an existing Dataset Collection...</span>
			<form action="addData.jsp" method="POST" style="margin-top:30px;margin-left:80px;"/>
				<input type="hidden" name="step" value="choosePolicy"/>
				<input type="text" id="dsCollection" name="dsCollection" value="choose from the list below..." style="width:300px;border:solid 1px #888;padding:2px;">
				<input type="submit" id="submitButton" disabled="true" value="Select"/>
				<div id="policyView" style="width:390px;border:solid 0px red;margin-top:5px;background-color:#fff;">
				
				</div>	
			</form>
		</div>
		<div id="newDatasetCollection" style="display:none;background:url(media/img/icon-new-dataset.png) scroll 5px 10px no-repeat;">
			<span class="tabLabel" style="padding-left:90px;">Create a new Dataset Collection...</span>
			<div style="padding-left:90px;padding-top:30px;font-size:0.90em;">
				<p>A dataset consists of three XML files:
				<form action="uploadDatasetDefinitionFiles" method="POST" enctype="multipart/form-data">
				  <ul style="list-style:none;line-height:1.5em;">
				    <li><span style="font-family:Courier New;">product-types.xml</span> - defines dataset names, their
				    archive locations, and the dataset metadata, metadata that does not change on a per-product
				    basis), such as Data Custodian, ProtocolId, etc.<br/>
				    <input type="file" value="" name="product_types_xml"><br/><br/>
				    </li>
				    <li><span style="font-family:Courier New;">elements.xml</span> - defines product metadata elements,
				    metadata that changes on a per product basis, e.g., Filename, File Location, etc.<br/>
				    <input type="file" value="" name="elements_xml"><br/><br/>
				    </li>
				    <li><span style="font-family:Courier New;">product-type-element-map.xml</span> - maps product metadata
				    elements to datasets.<br/>
				    <input type="file" value="" name="product_type_element_map_xml"><br/>
				    </li>
				   </ul>		
				</p>
					Click here to upload your files: <input type="submit" name="submit" value="Upload Files" />
				</form>
			</div>
		</div>
	</div>
	
	</div>
</div>
<jsp:include page="views/common/cas-curator-footer.jsp" />