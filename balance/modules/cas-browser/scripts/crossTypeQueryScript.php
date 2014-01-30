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

global $messages;
$messages = array();

function searchBooleanMetadata($pts, $booleanCriterion){
	global $messages;
	$resultTypes = array();
	$terms = $booleanCriterion->getTerms();
	if($booleanCriterion->getOperator() == CAS_Filemgr_BooleanQueryCriteria::$AND_OP){
		$resultTypes = $pts;
		foreach($terms as $t){
			try{
				$resultTypes = searchTypeMetadata($resultTypes, $t);
				array_push($messages, count($termResults) . " types returned to boolean type search");
			}catch(Exception $e){
				throw new CasBrowserException("Exception occurred while searching type metadata in boolean AND term ("
						. $t->getElementName() . " = " . $t->getValue() . "): " . $e->getMessage());
			}
			if(!count($resultTypes)){
				array_push($messages, "No types found that match the boolean AND query");
				return array();
			}
		}
	}elseif($booleanCriterion->getOperator() == CAS_Filemgr_BooleanQueryCriteria::$OR_OP){
		foreach($terms as $t){
			try{
				$termResults = searchTypeMetadata($pts, $t);
			}catch(Exception $e){
				throw new CasBrowserException("Exception occurred while searching type metadata in boolean OR term ("
						. $t->getElementName() . " = " . $t->getValue() . "): " . $e->getMessage());
			}
			$resultTypes += $termResults;	// Not sure if this is correct
			if(count($resultTypes) == count($pts)){
				array_push($messages, "All types match the boolean OR query");
				return $pts;
			}
		}
	}elseif($booleanCriterion->getOperator() == CAS_Filemgr_BooleanQueryCriteria::$NOT_OP){
		try{
			$termResults = searchTypeMetadata($pts, $terms[0]);
		}catch(Exception $e){
			throw new CasBrowserException("Exception occurred while searching type metadata in boolean NOT term: "
					. $e->getMessage());
		}
		$resultTypes = array_merge(array_diff($pts, $termResults));
	}else{
		throw new CasBrowserException("A BooleanQueryCriteria object has an invalid operator");
	}
	array_push($messages, count($resultTypes) . " types found that match the boolean query");
	return $resultTypes;
}

function searchTermMetadata($pts, $termCriterion){
	global $messages;
	$key = $termCriterion->getElementName();
	$value = $termCriterion->getValue();
	array_push($messages, "Searching type metadata for " . $key . " = " . $value . " in " . count($pts) . " types");
	$resultTypes = array();
	foreach($pts as $pt){
		$ptMet = $pt->getTypeMetadata();
		if(array_key_exists($key, $ptMet->toAssocArray())){
			if($ptMet->isMultiValued($key)){
				$ptMetValues = $ptMet->getAllMetadata($key);
				for($i = 0; $i < count($ptMetValues); $i++){
					//array_push($messages, "Comparing type " . $pt->getName() . " key " . $key . " value: " . $ptMetValues[$i]);
					if($ptMetValues[$i] == $value){
						array_push($resultTypes, $pt);
						$i = count($ptMetValues);
					}
				}
			}elseif($ptMet->getMetadata($key) == $value){
				array_push($resultTypes, $pt);
			}
		}
	}
	array_push($messages, count($resultTypes) . " types found that match the term");
	return $resultTypes;
}

function searchTypeMetadata($pts, $criterion){
	if($criterion == NULL){
		throw new CasBrowserException("A null criterion was given to search for type metadata");
	}
	if($pts == NULL){
		throw new CasBrowserException("A null list of product types was given to search for type metadata");
	}
	if(!count($pts)){
		throw new CasBrowserException("An empty list of product types was given to search for type metadata");
	}
	if($criterion instanceof CAS_Filemgr_TermQueryCriteria){
		return searchTermMetadata($pts, $criterion);
	}elseif($criterion instanceof CAS_Filemgr_RangeQueryCriteria){
		return array();	// Not yet supported
	}elseif($criterion instanceof CAS_Filemgr_BooleanQueryCriteria){
		return searchBooleanMetadata($pts, $criterion);
	}else{
		throw new CasBrowserException("An unknown query criteria type was encountered while search product type metadata");
	}
}




$outputFormat = 'json';	// Hard-coded for now

// Get client handle
$cb = new CasBrowser();
$client = $cb->getClient();

$results = array('results'=>array());

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

// Create the tree of criteria objects that define the query.  The tree root is returned.
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

// Search inside of product type metadata for results to further refine search
// NOTE: This is all currently a dirty hack!  This needs major refinement!
$allMatchingProducts = array();
$typesByName = array();
foreach($queryTypes as $t){
	$typesByName[$t->getName()] = $t;
}
try{
	$matchingPTs = searchTypeMetadata($queryTypes, $criteriaTree);
}catch(Exception $e){
	Utils::reportError("An exception occurred while searching type metadata: " . $e->getMessage(), $outputFormat);
}
foreach($matchingPTs as $matchingType){
	array_splice($queryTypes, array_search($matchingType, $queryTypes), 1);	// Remove types that match from query
	foreach($client->getProductsByProductType($matchingType) as $p){	// Add all products of matching types
		array_push($allMatchingProducts, array('product'=>$p, 'typeName'=>$matchingType->getName()));
	}
}

// Add criteria to query object
$query = new CAS_Filemgr_Query();
$query->addCriterion($criteriaTree);

// Perform the query and collect results
try{
	foreach($queryTypes as $type){
		$queryResultsOfType = $client->query($query, $type);
		if(count($queryResultsOfType) > 0){
			foreach($queryResultsOfType as $matchingProduct){
				array_push($allMatchingProducts, array('product'=>$matchingProduct, 'typeName'=>$type->getName()));
			}
		}
	}
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}

// Narrow down the given products to the requested page (if page info is given)
$requestedProducts = array();
if(isset($_POST['PageNum']) && isset($_POST['PageSize'])){
	$pageNum = intval($_POST['PageNum']);
	$pageSize = intval($_POST['PageSize']);
	try{
		$requestedProducts = Utils::paginate($allMatchingProducts, $pageNum, $pageSize);
	}catch(Exception $e){
		Utils::reportError($e->getMessage(), $outputFormat);
	}
	$results['totalPages'] = ceil(count($allMatchingProducts) / $pageSize);
}else{
	$requestedProducts = $allMatchingProducts;
}

// Get metadata and format requested products
try{
	$results['results'] = Utils::formatResults($requestedProducts);
}catch(Exception $e){
	Utils::reportError($e->getMessage(), $outputFormat);
}
	
$results['totalProducts'] = count($allMatchingProducts);

$results['messages'] = $messages;

echo json_encode($results);

?>