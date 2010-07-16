<!-- tree view css + js -->
<link rel="stylesheet" type="text/css" href="js/jquery-treeview/jquery.treeview.css"/>
<script type="text/javascript" src="js/jquery/jquery.js"></script>
<script type="text/javascript" src="js/jquery-treeview/jquery.treeview.js"></script>
<script type="text/javascript" src="js/jquery-treeview/jquery.treeview.async.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		// Load up the tree view for the staging area
		$("#policyView").treeview({
			url: "./showExistingPolicies"
			
		});
	});
	
	function treeSelection(a) {
		id = $(a).attr('href');
		$("#dsCollection").attr('value',id);
		$("#submitButton").attr('disabled',false);
		return false;
	}
</script>
<!--  end tree view css + js -->



<div class="wizardContent">
	<h4>Choose a Dataset Collection:</h4>
	<form action="manageDataset.jsp" method="POST" style="margin-top:30px;margin-left:80px;"/>
		<input type="hidden" name="step" value="chooseDataset"/>
		<input type="text" id="dsCollection" name="dsCollection" value="choose from the list below..." style="width:300px;border:solid 1px #888;padding:2px;">
		<input type="submit" id="submitButton" disabled="true" value="Select"/>
		<div id="policyView" style="width:390px;border:solid 0px red;margin-top:5px;background-color:#fff;">
		
		</div>	
	</form>
</div>
