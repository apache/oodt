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
require_once($module->modulePath . "/scripts/widgets/CrossTypeSearchWidget.php");

// Get a CAS-Browser XML/RPC client
$browser  = new CasBrowser();
$client   = $browser->getClient();

// Initialize the CrossTypeSearchWidget
$querySiteRoot = (isset(App::Get()->settings['query_service_url']))
	? App::Get()->settings['query_service_url']
	: 'http://' . $_SERVER['HTTP_HOST'] . $module->moduleRoot;
$crossTypeWidget = new CrossTypeSearchWidget(array(
	'htmlID'       => 'cas_browser_product_list',
	'loadingID'    => 'loading_icon_container',
	'siteUrl'      => $querySiteRoot));
$crossTypeWidget->renderScript();
	
// Store any filter and exclusive/inclusive parameters passed in via URL
$urlParams = CrossTypeSearchWidget::parseSegments();
$queryBoolValue = 'and';
$exclusiveChecked = true;
if(isset($urlParams['filterParams'])){
	$filterButtonValue = "Hide Filters";
}else{
	$filterButtonValue = "Show Filters";
}
if(isset($urlParams['exclusive'])){
	$queryBoolValue = $urlParams['exclusive']['bool'];
	$exclusiveChecked = $urlParams['exclusive']['checked'];
}

// Prepare BreadcrumbWigdet
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add("Browse Across Types");	
?>

<script type="text/javascript" src="<?php echo $module->moduleStatic?>/js/jquery.dataTables.min.js"></script>
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

	pPageSize = 20;

	addBooleanCriteria(<?php echo "'" . $queryBoolValue . "'"; ?>, null);

	<?php
	if(isset($urlParams['filterParams'])){
		foreach($urlParams['filterParams'] as $filter){
			echo "createFilter('" . $filter[0] . "','" . $filter[1] . "');\n";
		}
		if($exclusiveChecked == false){
			echo '$("#exclusive").attr("checked", false);';
		}
		echo 'displayPermalink();';
		echo '$("#filter_widget").css("display", "block");';
	}
	?>

	sendCrossTypeRequest();
	
});
</script>
<div class="container">
<hr class="space"/>
<div id="cas_browser_container" class="span-24 last">
	<div id="section_products">
		<h2 ><?php echo "Cross Product Type Search"?></h2>
		<ul class="tabmenu">
			<li class=""><a href="<?php echo $module->moduleRoot?>/">Browse By Type</a></li>
			<li class="selected"><a href="<?php echo $module->moduleRoot?>/typesearch/">Browse Across Types</a></li>
		</ul>
		<hr class="space"/>
		
		<div id="section_filter_tools_container">
			<div id="section_filter_tools_buttons">
				<input type="button" id="showFilters" value=<?php echo '"' . $filterButtonValue . '"';?> />
			</div>
			<div id="section_filter_tools">
				<div id="filter_widget">
					Filter: <?php $crossTypeWidget->render(); ?>
				</div>
			</div>
		</div>
		
		<br/>
		<div id="loading_icon_container">
		<img src="<?php echo App::Get()->request->moduleStatic . '/img/loading.gif'; ?>"/>
		</div>
		<div id="cas_browser_product_list"></div>
	</div>
</div>
</div>