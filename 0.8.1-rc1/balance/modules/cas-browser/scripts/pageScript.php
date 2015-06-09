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
require_once(dirname(dirname(__FILE__)) . '/classes/CasBrowser.class.php');

// Extract desired output format from POST
if(isset($_POST['OutputFormat'])){
	try{
		$outputFormat = Utils::getRequestedReturnType($_POST['OutputFormat']);
	}catch(Exception $e){
		Utils::reportError($e->getMessage(), 'html');
	}
}else{
	$outputFormat = 'html';
}

// Get client handle
$cb = new CasBrowser();
$client = $cb->getClient();

// Extract ProductType from POST
if(!isset($_POST['Type'])){
	Utils::reportError("POST does not contain 'Type' ProductType", $outputFormat);
}
$typeName = $_POST['Type'];
try{
	$allTypes = $client->getProductTypes();
	$allTypeNames = array_map(create_function('$t', 'return $t->getName();'), $allTypes);
	if(!in_array($typeName, $allTypeNames)){
		$errStr = "The type " . $typeName . " is not used in the repository.  Please use one of: ";
		for($i = 0; $i < count($allTypeNames) - 1; $i++){
			$errStr .= $allTypeNames[i] . ", ";
		}
		$errStr .= $allTypeNames[count($allTypeNames) - 1];
		Utils::reportError($errStr, $outputFormat);
	}
	$type = $client->getProductTypeByName($typeName);
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

// Extract page number from POST
if(!isset($_POST['PageNum'])){
	Utils::reportError("POST does not contain 'PageNum'", $outputFormat);
}
$pageNum = intval($_POST['PageNum']);

// Get the requested page
try{
	$page = Utils::getPage($type, $pageNum);
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

// Get the products from the requested page -- what we're really after
$pageProducts = array();
foreach($page->getPageProducts() as $p){
	array_push($pageProducts, array('product'=>$p));
}

// Format results
if($outputFormat == 'html'){
	$payload = '<ul class="pp_productList" id="product_list">';
	foreach($pageProducts as $p){
		$payload .= '<li><a href="' . $module->moduleRoot . '/product/' . $p['product']->getId() . '">';
		$payload .= urlDecode($p['product']->getName()) . '</a></li>';
	}
	$payload .= "</ul>\n";
	$payload .= '<input type="hidden" id="total_pages" value="' . $page->getTotalPages() . '">';
	$payload .= '<input type="hidden" id="page_size" value="' . $page->getPageSize() . '">';
}elseif ($outputFormat == 'json') {
	$payload = array();
	try{
		$payload['results'] = Utils::formatResults($pageProducts);
		$payload['totalProducts'] = $client->getNumProducts($type);
	}catch(Exception $e){
		Utils::reportError($e->getMessage(), $outputFormat);
	}
	$payload['totalPages'] = $page->getTotalPages();
	$payload['pageSize'] = $page->getPageSize();
	$payload = json_encode($payload);
}

echo $payload;

?>