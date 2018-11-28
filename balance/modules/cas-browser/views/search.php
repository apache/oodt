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

// Get a CAS-Browser XML/RPC client
$browser  = new CasBrowser();
$client   = $browser->getClient();
 
// Get a  Product Type object
$productType  = $client->getProductTypeById(App::Get()->request->segments[0]);
$productCount = $client->getNumProducts($productType);
$ptID = $productType->getId();
$ptName = $productType->getName();

// Determine which search widget to show
$widgetClassName = isset(App::Get()->settings['browser_product_search_widget'])
	? App::Get()->settings['browser_product_search_widget']
	: 'FilterWidget';

$querySiteRoot = (isset(App::Get()->settings['query_service_url']))
	? App::Get()->settings['query_service_url']
	: 'http://' . $_SERVER['HTTP_HOST'] . MODULE_ROOT;

// Create the search widget
require_once($module->modulePath . "/scripts/widgets/{$widgetClassName}.php");
$searchWidget = new $widgetClassName(array(
	'productType'=>$productType,
	'htmlID'=>'cas_browser_product_list',
	'siteUrl'=>$querySiteRoot,
	'pagedResults'=>true,
	'resultFormat'=>'json'));

// Render search widget javascript
$searchWidget->renderScript();

// Prepare BreadcrumbWigdet
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add("Browser", $module->moduleRoot . '/');
$bcw->add($ptName, $module->moduleRoot."/dataset/{$ptID}");	
$bcw->add("Product Search");
?>

<div class="container">
<hr class="space"/>
<div id="cas_browser_container" class="span-24 last">
	<ul class="tabs">
	  <li><a id="tab_metadata" href="<?php echo MODULE_ROOT?>/dataset/<?php echo $ptID?>">Metadata</a></li>
	  <li><a id="tab_browse"   href="<?php echo MODULE_ROOT?>/products/<?php echo $ptID?>">Browse</a></li>
	  <li><a id="tab_search"   href="<?php echo MODULE_ROOT?>/search/<?php echo $ptID?>" class="selected">Search</a></li>
	</ul>
	<div id="section_products">
		<h2 class="larger loud">Product Search: <?php echo $ptName?></h2>
		<br/>
		<div id="cas_browser_search_widget" class="span-24 last">
	  		<?php $searchWidget->render(); ?>
			<input type="hidden" id="page_num" value="1">
		</div>
		<div id="cas_browser_product_list" class="span-16 colborder">
	  		<h3>Product Search Results</h3>
		</div>
		<div id="cas_browser_dataset_download" class="span-6 last">
			<a href="<?php echo App::Get()->settings['browser_datadeliv_url']?>/dataset?typeID=<?php echo App::Get()->request->segments[0]?>&format=application/x-zip">
				<img src="<?php echo MODULE_STATIC?>/img/zip-icon-smaller.gif" id="zip_icon" alt="zip-icon"/>
			</a>
			Click on the icon to download all <?php echo $productCount ?> data products associated with
			this search as a single Zip archive.<br/>
		</div>
	</div>
</div>
</div>
