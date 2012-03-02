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
 *
 *
 *   PRODUCT-TYPE FILTER
 *   
 *   HTTP Method: GET
 *   Input:
 *     - key    (string) a ProductType metadata key to search on
 *     - value  (string) the value to use when determining matches
 *   Output:
 *     - json   (default) a json array representing all matching product types
 *              with their defined metadata
 *              
 */
$module = App::Get()->loadModule();
require_once( $module->modulePath . "/classes/CasBrowser.class.php");
require_once( $module->modulePath . "/scripts/widgets/ProductTypeListWidget.php");

// Get a Cas-Browser XML/RPC Client
$browser = new CasBrowser();
$client  = $browser->getClient();

// Get a list of the product types managed by this server
$ptypes = $client->getProductTypes();

// Get the metadata key/val pair that will serve as the needle
$metKey = urldecode($_GET['key']);
$needle = urldecode($_GET['value']);

$productTypes = array();
foreach ($ptypes as $pt) {
	$ptArray = $pt->toAssocArray();
	
	// Check whether the requested met key value matches desired value
	if ($needle == '*' || (isset($ptArray['typeMetadata'][$metKey]) 
		&& $ptArray['typeMetadata'][$metKey][0] == $needle)) {

		$merged = array_merge($ptArray['typeMetadata'],array(
			"name" => array($ptArray[App::Get()->settings['browser_pt_name_key']]),
			"description" => array($ptArray[App::Get()->settings['browser_pt_desc_key']]),
			"id"   => array($ptArray[App::Get()->settings['browser_pt_id_key']])));
		
		$productTypes[] = $merged;
	}	
}

// Format output as json
$json = json_encode($productTypes);

// Output the json result
header('Content-Type: application/json');
echo $json;

// We're done.
exit();
