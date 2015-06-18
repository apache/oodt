<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

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
	<h4>Choose a Dataset to Manage:</h4>

	<div>
		<form action="manageDataset.jsp" method="POST">
			<input type="hidden" name="step" value="displayDatasetMetadata"/>
			<table style="border:solid 1px #ccc;width:470px;">
			  <tr><td style="width:190px;">Selected Dataset Collection: </td><td><%=session.getAttribute("dsCollection") %> <a class="actionlink" href="manageDataset.jsp">change</a></td></tr>
			  <tr><td>Selected Dataset:</td><td style="padding-right:9px;"><input type="text" id="ds" name="ds" value="select from list below..." style="width:100%;border-width:1px;border-color:#ccc;padding:3px;"/></td></tr>
			  <tr><td colspan="2" style="text-align:right;font-size:90%;color:#333;">Choose a dataset below, and click to continue...&nbsp;&nbsp; <input type="submit" id="submitButton" value="Continue..." style="padding:2px;" disabled="true"/></td></tr>
			</table>
		</form>
		<h5>Datasets in <%=session.getAttribute("dsCollection") %>:</h5>
		<div id="datasetView">
		
		</div>

	</div>
</div>
