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

// Load the module context for this module
$module  = App::Get()->loadModule();

require_once($module->modulePath . "/classes/CasBrowser.class.php");
require_once($module->modulePath . "/scripts/widgets/MetadataDisplayWidget.php");

// Get a CAS-Browser XML/RPC client
$browser  = new CasBrowser();
$client   = $browser->getClient();

// Get a  Product Type object
$productType = $client->getProductTypeById(App::Get()->request->segments[0]);
$ptID = $productType->getId();
$ptName = $productType->getName();

// Determine the visibility level based on the current user
$ptVisibilityLevel = $browser->getProductTypeVisibilityLevel($ptID);

// Redirect the user if they are not authorized
if ($ptVisibilityLevel == CasBrowser::VIS_NONE) {
	App::Get()->redirect(SITE_ROOT . '/errors/403');
}

// Load a MetadataDisplayWidget with the visible metadata for this product type
$typeMetadataWidget = new MetadataDisplayWidget(array());
$typeMetadataWidget->loadMetadata($browser->getVisibleMetadataForProductType($ptID));

// Create a MetadataDisplayWidget to display system metadata (all except typeMetadata)
$typeMetadata = $productType->toAssocArray();
unset($typeMetadata['typeMetadata']);
$systemMetadataWidget = new MetadataDisplayWidget(array());
$systemMetadataWidget->loadMetadata($typeMetadata);


// Prepare BreadcrumbWigdet
$bcw = new BreadcrumbsWidget();
$bcw->add('Home',SITE_ROOT . '/');
$bcw->add("Browse By Type", $module->moduleRoot . '/');
$bcw->add($ptName);
?>


<div class="container">
	<hr class="space" />

	<div id="cas_browser_container">
		<h2><?php echo $sortedMetadata['DataSetName'][0]?> <?php echo $ptName?></h2>
		<hr />

		<div id="section_type_metadata">
			<h3>Description:</h3>
			<p><?php echo $productType->getDescription()?></p>
			<hr class="space" />

			<ul class="tabmenu">
				<li class="selected"><a href="<?php echo $module->moduleRoot?>/dataset/<?php echo $ptID?>">Additional Information</a></li>
				<li class=""><a href="<?php echo $module->moduleRoot?>/products/<?php echo $ptID?>">Downloadable Files</a></li>
			</ul>

			<div id="additional-information">
				<p>The following additional metadata information has been defined for this
				dataset.</p>

				<?php if ($ptVisibilityLevel == CasBrowser::VIS_LIMIT):?>
				<div class="notice">Additional information may exist that is not visible
				due to your current access permissions.</div>
				<?php endif;?>

				<?php echo $typeMetadataWidget->render()?>
			</div>
				
			<div id="data"></div>
		</div>

		<?php if (!App::Get()->settings['browser_suppress_system_metadata']):?>
		<div id="section_system_metadata">
			<h3>System Metadata:</h3>
			<?php echo $systemMetadataWidget->render()?>
		</div>
		<?php endif;?>
	</div>
</div>