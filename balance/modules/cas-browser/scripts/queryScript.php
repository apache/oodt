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

// Ceate an array of ProductTypes to be queried
try{
	if(!isset($_POST['Types'])){
		Utils::reportError("POST does not contain 'Types' sub-array", $outputFormat);
	}
	if(count($_POST['Types']) == 0){
		Utils::reportError("No product types were specified in POST", $outputFormat);
	}
	$queryTypes = array();
	$allTypes = $client->getProductTypes();
	if($_POST['Types'][0] == '*'){
		$queryTypes = $allTypes;
	}else{
		$allTypeNames = array_map(create_function('$t', 'return $t->getName();'), $allTypes);
		foreach($_POST['Types'] as $type){
			if(!in_array($type, $allTypeNames)){
				$errStr = "Error: The type " . $type . " is not used in the repository.  Please use one of: ";
				$errStr .= $allTypeNames[0];
				for($i = 1; $i < count($allTypeNames); $i++){
					$errStr .= ", " . $allTypeNames[$i];
				}
				Utils::reportError($errStr, $outputFormat);
			}
			array_push($queryTypes, $client->getProductTypeByName($type));
		}
		if(!count($queryTypes)){
			Utils::reportError("No ProductTypes were given to query", $outputFormat);
		}
	}
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

// Check if results are desired in a ProductPage and which page of results is desired
$pagedResults = false;
$pageNum = 1;
if(isset($_POST['PagedResults'])){
	if($_POST['PagedResults']){
		if(count($queryTypes) != 1){
			Utils::reportError("Paged queries can only be performed on one ProductType", $outputFormat);
		}
		$pagedResults = true;
		if(isset($_POST['PageNum'])){
			$pageNum = intval($_POST['PageNum']);
		}		
	}
}

// Create the tree of criteria objects that define the query
if(!isset($_POST['Criteria'])){
	Utils::reportError("POST does not contain 'Criteria' sub-array", $outputFormat);
}
if(!count($_POST['Criteria'])){
	Utils::reportError("POST sub-array 'Criteria' contains no criteria", $outputFormat);
}
$rootIndex = (isset($_POST['RootIndex']))
			? intval($_POST['RootIndex'])
			: 0;
try{
	$criteriaTree = Utils::createCriteriaTree($rootIndex, $queryTypes, null);
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}
	
// Add criteria to query object
$query = new CAS_Filemgr_Query();
$query->addCriterion($criteriaTree);

// Perform the query and collect results
$results = array();
try{
	if($pagedResults){
		$resultPage = $client->pagedQuery($query, $queryTypes[0], $pageNum);
		foreach($resultPage->getPageProducts() as $p){
			array_push($results, array('product'=>$p));
		}
	}else{
		foreach($queryTypes as $type){
			foreach($client->query($query, $type) as $p){
				array_push($results, array('product'=>$p));
			}
		}
	}
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

// Format results
try{
	if($outputFormat == 'html'){
		$payload = '<ul class="pp_productList" id="product_list">';
		foreach($results as $r){
			$payload .= '<li><a href="' . $module->moduleRoot . '/product/' . $r['product']->getId() . '">';
			$payload .= urlDecode($r['product']->getName()) . '</a></li>';
		}
		$payload .= "</ul>\n";
		if($pagedResults){
			$payload .= '<input type="hidden" id="total_pages" value="' . $resultPage->getTotalPages() . '">';
			$payload .= '<input type="hidden" id="page_size" value="' . $resultPage->getPageSize() . '">';
			$payload .= '<input type="hidden" id="total_type_products" value="' . $client->getNumProducts($queryTypes[0]) . '">';
		}
	}elseif ($outputFormat == 'json') {
		$payload = array();
		$payload['results'] = Utils::formatResults($results);
		if($pagedResults){
			$payload['totalPages'] = $resultPage->getTotalPages();
			$payload['pageSize'] = $resultPage->getPageSize();
			$payload['totalProducts'] = $client->getNumProducts($queryTypes[0]);
		}
		$payload = json_encode($payload);
	}
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

echo $payload;

?>

