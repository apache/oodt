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
require_once($module->modulePath . "/scripts/widgets/MetadataDisplayWidget.php");
require_once($module->modulePath . "/scripts/widgets/ProductDownloadWidget.php");

// Get a CAS-Browser XML/RPC client
$browser  = new CasBrowser();
$client   = $browser->getClient();
    
// Get the specified product
$productId = App::Get()->request->segments[0];
$product = $client->getProductById($productId);
$productName     = $product->getName();
$productTypeName = $product->getType()->getName();
$productTypeId   = $product->getType()->getId();
$productMetadata = $client->getMetadata($product);

// Get metadata for product and productType as associative arrays
$productTypeInfo = $product->getType()->toAssocArray();
$productInfo     = $productMetadata->toAssocArray();

$productVisibilityLevel = $browser->getProductVisibilityLevel($productId);

// Redirect the user if they are not authorized 
if ($productVisibilityLevel == CasBrowser::VIS_NONE) {
	App::Get()->redirect(SITE_ROOT . '/errors/403');
}


// Create and load a MetadataDisplay widget wit the visible metadata
$metadataWidget = new MetadataDisplayWidget(array());
$metadataWidget->loadMetadata($browser->getVisibleMetadataForProduct($productId));

// Record the product page to send the user back to, if provided
$returnPage = isset(App::Get()->request->segments[1]) ? App::Get()->request->segments[1] : 1;

// Create a ProductDownloadWidget
$productDownloadWidget = new ProductDownloadWidget(array(
	"dataDeliveryUrl" => App::Get()->settings['browser_datadeliv_url']));
$productDownloadWidget->setClient($client);
$productDownloadWidget->load($product);

// Add the cas-browser styles
App::Get()->response->addStylesheet($module->moduleStatic . '/css/cas-browser.css');

// Prepare BreadcrumbWigdet
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add('Browse By Type', $module->moduleRoot . '/');
$bcw->add($productTypeName, $module->moduleRoot."/dataset/{$productTypeId}");	
$bcw->add('Products', $module->moduleRoot."/products/{$productTypeId}/page/{$returnPage}");
$bcw->add(App::Get()->request->segments[0]);
?>


<hr class="space"/>
<div class="span-22 last prepend-1 append-1">
	<div id="cas_browser_product_metadata">
		<h2 class="larger loud">Product Metadata: <?php echo wordwrap($productName, 62, "<br />",true);?></h2>
		<?php $metadataWidget->render()?>
	</div>
	<div id="cas_browser_product_download">
		<h2 class="larger loud">Download this Product:</h2>
		<?php $productDownloadWidget->render()?>
	</div>
</div>
