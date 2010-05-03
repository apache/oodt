<!-- tree view css + js -->
<link rel="stylesheet" type="text/css" href="js/jquery-treeview/jquery.treeview.css"/>
<script type="text/javascript" src="js/jquery/jquery.js"></script>
<script type="text/javascript" src="js/jquery-treeview/jquery.treeview.js"></script>
<script type="text/javascript" src="js/jquery-treeview/jquery.treeview.async.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		// Load up the tree view for the staging area
		$("#datasetView").treeview({
			url: "showExistingDatasetsByPolicy?policy=<%=session.getAttribute("dsCollection")%>"
			
		});
	});
	
	function treeSelection(a) {
		id = $(a).attr('href');
		$("#ds").attr('value',id);
		$("#submitButton").attr('disabled',false);
		return false;
	}
</script>
<!--  end tree view css + js -->



<div class="wizardContent">
	<h4>Add Data : Choose a Dataset</h4>

	<div>
		<form action="addData.jsp?step=chooseMetExtractor" method="POST">
			<table style="border:solid 1px #ccc;width:470px;">
			  <tr><td style="width:190px;">Selected Dataset Collection: </td><td><%=session.getAttribute("dsCollection") %> <a class="actionlink" href="ingestData.jsp">change</a></td></tr>
			  <tr><td>Selected Dataset:</td><td style="padding-right:9px;"><input type="text" id="ds" name="ds" value="select from list below..." style="width:100%;border-width:1px;border-color:#ccc;padding:3px;"/></td></tr>
			  <tr><td colspan="2" style="text-align:right;font-size:90%;color:#333;">Choose a dataset below, and click to continue...&nbsp;&nbsp; <input type="submit" id="submitButton" value="Continue..." style="padding:2px;" disabled="true"/></td></tr>
			</table>
		</form>
		<h5>Datasets in <%=session.getAttribute("dsCollection") %>:</h5>
		<div id="datasetView">
		
		</div>

	</div>
</div>