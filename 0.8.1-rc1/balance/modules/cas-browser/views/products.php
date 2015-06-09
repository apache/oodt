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
require_once($module->modulePath . "/classes/CasBrowser.class.php");
require_once($module->modulePath . "/scripts/widgets/FilterWidget.php");
require_once($module->modulePath . "/scripts/widgets/ProductPageWidget.php");

// Get a CAS-Browser XML/RPC client
$browser  = new CasBrowser();
$client   = $browser->getClient();
 
// Get a  Product Type object
$productType = $client->getProductTypeById(App::Get()->request->segments[0]);
$ptID     = $productType->getId();
$ptName   = $productType->getName();
$typeInfo = $productType->toAssocArray();

// Determine the visibility level based on the current user
$ptVisibilityLevel = $browser->getProductTypeVisibilityLevel($ptID);

// Redirect the user if they are not authorized
if ($ptVisibilityLevel == CasBrowser::VIS_NONE) {
	App::Get()->redirect(SITE_ROOT . '/errors/403');
}

// Store any filter parameters passed in via URL
$filterParams = array();
$segments = App::Get()->request->segments;
for($index = 1; isset($segments[$index]) && $segments[$index] != ""; $index = $index + 2){
	array_push($filterParams, array($segments[$index], $segments[$index + 1]));
}
if(count($filterParams) > 0){
	$filterButtonValue = "Hide Filters";
}else{
	$filterButtonValue = "Show Filters";
}

// Initialize the FilterWidget
$querySiteRoot = (isset(App::Get()->settings['query_service_url']))
	? App::Get()->settings['query_service_url']
	: 'http://' . $_SERVER['HTTP_HOST'] . $module->moduleRoot;
$resultFormat = "json";
$filterWidget = new FilterWidget(array(
	'productType'  => $productType,
	'htmlID'       => 'cas_browser_product_list',
	'loadingID'    => 'loading_icon_container',	
	'siteUrl'      => $querySiteRoot,
	'pagedResults' => true,
	'resultFormat' => $resultFormat));
$filterWidget->renderScript();

// Determine whether to show the download widget
$showDownloadWidget = (isset(App::Get()->settings['browser_show_download_widget']) 
	&& App::Get()->settings['browser_show_download_widget'] == 0)
	? false
	: true;
	
// Prepare BreadcrumbWigdet
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add("Browse By Type", $module->moduleRoot . '/');
$bcw->add($ptName, $module->moduleRoot."/dataset/{$ptID}");	
$bcw->add("Products");
?>

<script type="text/javascript">
$(document).ready(function() {

	$('#showFilters').click(function() {
		if ($(this).val() == "Show Filters") {
			$('#filter_widget').fadeIn('slow');
			$(this).val('Hide Filters');
		} else {
			$('#filter_widget').fadeOut('slow');
			$(this).val('Show Filters');
		}
	});

	<?php
	if(count($filterParams) > 0){
		foreach($filterParams as $filter){
			echo "createFilter('" . $filter[0] . "','" . $filter[1] . "');\n";
		}
		echo 'displayPermalink();';
		echo '$("#filter_widget").css("display", "block");';
	}
	?>

	sendRequest(<?php echo '"' . $resultFormat . '"'; ?>);
	
});
</script>
<div class="container">
<hr class="space"/>
<div id="cas_browser_container" class="span-24 last">

	<div id="section_products">
		<h2 ><?php echo $ptName?></h2>
		<hr/>
		
		<div id="section_type_metadata">
			<h3>Description:</h3>
			<p><?php echo $typeInfo['description']?></p>
			<hr class="space"/>
			
			<ul class="tabmenu">
				<li class="">        <a href="<?php echo $module->moduleRoot?>/dataset/<?php echo $ptID?>">Additional Information</a></li>
				<li class="selected"><a href="<?php echo $module->moduleRoot?>/products/<?php echo $ptID?>">Downloadable Files</a></li>
			</ul>
		</div>
		<?php if ( $ptVisibilityLevel == CasBrowser::VIS_LIMIT ):?>
		<div class="notice">
			Additional information may exist that is not visible due to your current access permissions.
		</div>
		<?php endif;?>
		
		<div id="section_filter_tools_container">
			<div id="section_filter_tools_buttons">
				<input type="button" id="showFilters" value=<?php echo '"' . $filterButtonValue . '"';?>/>
			</div>
			<div id="section_filter_tools">
				<div id="filter_widget">
					Filter: <?php $filterWidget->render(); ?>
					<input type="hidden" id="page_num" value="1">
				</div>
			</div>
		</div>
		<?php if ( !App::Get()->settings['browser_private_products_visibility'] && $ptVisibilityLevel == CasBrowser::VIS_LIMIT ):?>
		<h3>Public Downloadable Files for this Dataset</h3>
		<?php else :?>
		<h3>Downloadable Files for this Dataset</h3>
		<?php endif;?>
		<div id="loading_icon_container">
		<img src="<?php echo App::Get()->request->moduleStatic . '/img/loading.gif'; ?>"/>
		</div>
		<div id="cas_browser_product_list"></div>
		
	</div>
</div>
</div>
