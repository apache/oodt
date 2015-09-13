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

$outputFormat = 'json';	// Hard-coded for now

// Get client handle
$cb = new CasBrowser();
$client = $cb->getClient();

$results = array('results'=>array());

// Get all products and their types
$allProducts = array();
foreach($client->getProductTypes() as $type){
	foreach($client->getProductsByProductType($type) as $product){
		array_push($allProducts, array('product'=>$product, 'typeName'=>$type->getName()));
	}
}

// Narrow down the given products to the requested page (if page info is given)
$requestedProducts = array();
if(isset($_POST['PageNum']) && isset($_POST['PageSize'])){
	$pageNum = intval($_POST['PageNum']);
	$pageSize = intval($_POST['PageSize']);
	try{
		$requestedProducts = Utils::paginate($allProducts, $pageNum, $pageSize);
	}catch(Exception $e){
		Utils::reportError($e->getMessage(), $outputFormat);
	}
	$results['totalPages'] = ceil(count($allProducts) / $pageSize);
}else{
	$requestedProducts = $allProducts;
}

// Get metadata and format requested products
try{
	$results['results'] = Utils::formatResults($requestedProducts);
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

$results['totalProducts'] = count($allProducts);

echo json_encode($results);

?>