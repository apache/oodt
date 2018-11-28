<?php
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$module = App::Get()->loadModule();
require_once( $module->modulePath . "/classes/CasBrowser.class.php");
require_once( $module->modulePath . "/scripts/widgets/ProductTypeListWidget.php");

// Get a CAS-Browser XML/RPC client
$browser = new CasBrowser();
$client  = $browser->getClient();
$isAlive = $client->isAlive();
if ( !$isAlive ) {
	App::Get()->SetMessage("Filemgr at ". $client->serverURL. " is down. Please start it.<br>",CAS_MSG_ERROR);
} else{
	
// Get a list of the product types managed by this server
$response     = $client->getProductTypes();
$productTypes = array();

foreach ($response as $pt) {
	$ptArray = $pt->toAssocArray();
	
	$ptVisibility = $browser->getProductTypeVisibilityLevel($pt->getId());
	
	if ($ptVisibility == CasBrowser::VIS_NONE) { continue; }

	$merged = array(
		"name" => array(
			$ptArray[App::Get()->settings['browser_pt_name_key']],
			$client->getNumProducts($pt)),
		"description" => array($ptArray[App::Get()->settings['browser_pt_desc_key']]),
		"id" => array($ptArray[App::Get()->settings['browser_pt_id_key']]));
	$merged += $ptArray['typeMetadata'];
	
	$productTypes[$ptArray[App::Get()->settings['browser_pt_id_key']]] = $merged;
}
$productTypeListWidget = new ProductTypeListWidget(array("productTypes" => $productTypes));
$productTypeListWidget->setUrlBase($module->moduleRoot);
}

// Prepare BreadcrumbWigdet
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add("Browse By Type");
?>

<script type="text/javascript" src="<?php echo $module->moduleStatic?>/js/jquery.dataTables.min.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	// Turn the table into a sortable, searchable table
	$("#productTypeSearch").dataTable();
	// Give the search box the initial focus
	$("#productTypeSearch_filter > input").focus();
}); 
</script>

<div class="container">

<?php if ( $isAlive ) :?>
<hr class="space"/>
<div id="cas_browser_container" class="span-22 last prepend-1 append-1">
	<h2 id="cas_browser_title"><?php echo App::Get()->settings['browser_index_title_text']?></h2>
	<ul class="tabmenu">
		<li class="selected"><a href="<?php echo $module->moduleRoot?>/">Browse By Type</a></li>
		<li class=""><a href="<?php echo $module->moduleRoot?>/typesearch/">Browse Across Types</a></li>
	</ul>
	<br class="fiftyPx"/>
<?php $productTypeListWidget->render();?>
</div>
<hr class="space"/>
</div>
<?php endif; ?>