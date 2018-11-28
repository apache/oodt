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

require_once("CasBrowser.class.php");

class Utils{
	
	public static $acceptedReturnTypes = array('html', 'json');

	/**
	 * @param types
	 *		An array of PoductTypes.
	 *
	 * @return 
	 *		An array of unique names of Metadata and Elements associated with the given 
	 *		ProductTypes.
	 */
	public static function getMetadataElements($types){
		$cb = new CasBrowser();
		$client = $cb->getClient();
		$metadataNames = array();
		foreach($types as $type){
			foreach(array_keys($type->getTypeMetadata()->toAssocArray()) as $metadata){
				if(!in_array($metadata, $metadataNames)){
					array_push($metadataNames, $metadata);
				}
			}
			foreach($client->getElementsByProductType($type) as $element){
				$elementName = $element->getElementName();
				if(!in_array($elementName, $metadataNames)){
					array_push($metadataNames, $elementName);
				}
			}
		}
		return $metadataNames;
	}
	
	/**
	 * @param productType
	 *		The productType of the ProductPage desired.
	 *
	 * @param pageNum
	 *		The number of the page desired in the set of ProductPages of that ProductType.
	 *
	 * @return
	 *		The requested ProductPage object.
	 */
	public static function getPage($productType, $pageNum){
		$cb = new CasBrowser();
		$client = $cb->getClient();
		
		// Iterate until the proper page is reached
		for($page = $client->getFirstPage($productType);
			$page->getPageNum() < $pageNum && $page->getPageNum() < $page->getTotalPages();
			$page = $client->getNextPage($productType, $page)){}
			
		return $page;
	}
	
	public static function getRequestedReturnType($requestedType){
		$lowerRequestedType = strtolower($requestedType);
		if(!in_array($lowerRequestedType, self::$acceptedReturnTypes)){
			throw new CasBrowserException('Error: The requested return type of '. $requestedType . 'is not accepted.');
		}
		return $lowerRequestedType;
	}
	
	public static function getProductListMetadata($products){
		$payload = array();
		foreach($products as $p){
			$cb = new CasBrowser();
			$client = $cb->getClient();
			$met = $client->getMetadata($p);
			$payload[$p->getId()] = $met;
		}
		return $payload;
	}
	
	// What kind of access should the currently authenticated user have, given the 
	// provided groups (aka roles, permissions) associated with the resource?
	//
	// Possible values are: CasBrowser::{VIS_ALL, VIS_LIMIT, VIS_NONE}. It is up to the 
	// caller to determine what to do based on the return value
	//
	public static function ResourceVisibility( $resourceGroups ) {
		
		// Is the resource considered "public"?
 		if (in_array(App::Get()->settings['browser_data_public_access'],$resourceGroups)) { 
			return CasBrowser::VIS_ALL;
		}
		
		// Get user authentication info
		$authentication = App::Get()->getAuthenticationProvider();
		$username = ($authentication)
		    ? $authentication->getCurrentUsername()
		    : false;	// no authentication info provided in config file
		
		// Does the metadata visibility depend on an access element matching
 		// the `browser_data_access_key` in the config setting?
 		$accessKeyExists = isset(App::Get()->settings['browser_data_access_key']);

 		// If key is set then we look into what groups have access to metadata
 		if ( $accessKeyExists && !empty($resourceGroups) ) {
			
			// Has authentication provider information been specified?
			if ( $authentication ) {
				
			 	// Is the user currently logged in?
			 	if ( $username ) {
	 		
			 		// Obtain the groups for the current user
			 		$userGroups = App::Get()->getAuthorizationProvider()->retrieveGroupsForUser($username);
			 			 		
			 		// Perform a comparison via array intersection to determine overlap
			 		$x = array_intersect($userGroups,$resourceGroups);
			 		
			 		if (empty($x)) { // No intersection found between user and resource groups
			
			 			// Examine `browser_pt_auth_policy` to determine how to handle the failure
			 			switch (strtoupper(App::Get()->settings['browser_pt_auth_policy'])) {
			 				case "LIMIT":
			 					// Allow the user to proceed, the metadata visibility policy
			 					// will be used to determine what is visible to non-authorized
			 					// users.
			 					return CasBrowser::VIS_LIMIT;				
			 				case "DENY":
			 				default:
			 					// Kick the user out at this point, deny all access. 
				 				return CasBrowser::VIS_NONE;
			 			}
			 		} else {
			 			// We have an authorized user
			 			$authorizedUser = true;
			 		}
			 	} else {
			 		// If no logged in user, and policy says DENY, kick the user
			 		if (strtoupper(App::Get()->settings[$metType]) == "DENY") {
			 			return CasBrowser::VIS_NONE;
 			 		} else {
			 			return CasBrowser::VIS_LIMIT;
 			 		}
			 	}
		 	} else {
			 	// If no authentication provider information exists in the application
			 	// configuration file, it is assumed that authentication and authorization
			 	// are not needed for this application, and thus every user is authorized
			 	// by default.
			 	return CasBrowser::VIS_ALL;	
			}				
 		} else {
 			
 			// All data is visible to user if logged in
 			// Has authentication provider information been specified?
			if ( $authentication ) {
				
			 	// Is the user currently logged in?
			 	if ( $username ) {
			 		return CasBrowser::VIS_ALL; // We have an authorized user
			 	} else {
			 		// If no logged in user, and policy says DENY, kick the user
			 		if (strtoupper(App::Get()->settings[$metType]) == "DENY") {
			 			return CasBrowser::VIS_NONE;
			 		}
			 		return CasBrowser::VIS_LIMIT;
			 	}
		 	} else {
			 	// If no authentication provider information exists in the application
			 	// configuration file, it is assumed that authentication and authorization
			 	// are not needed for this application, and thus every user is authorized
			 	// by default.
			 	return CasBrowser::VIS_ALL;	
			}
 		}
	}
	
	// Create a criteria subtree that will search for the value at the given criteriaIndex across all 
	// metadata elements associated with the given productTypes.
	public static function createBasicSearchSubtree($criteriaIndex, $queryTypes){
		$criterion = new CAS_Filemgr_BooleanQueryCriteria();
		$criterion->setOperator(CAS_Filemgr_BooleanQueryCriteria::$OR_OP);
		$metadataNames = getMetadataElements($queryTypes);
		foreach($metadataNames as $name){
			$term = new CAS_Filemgr_TermQueryCriteria();
			$term->setElementName($name);
			$term->setValue($_POST['Criteria'][$criteriaIndex]['Value']);
			$criterion->addTerm($term);
		}
		return $criterion;
	}
	
	public static function createTermCriteria($criteriaIndex, $queryTypes){
		if(!isset($_POST['Criteria'][$criteriaIndex]['ElementName'])){
			throw new CasBrowserException("Query Term criterion " . $criteriaIndex . " does not contain 'ElementName' specification");
		}
		if(!isset($_POST['Criteria'][$criteriaIndex]['Value'])){
			throw new CasBrowserException("Query Term criterion " . $criteriaIndex . " does not contain 'Value' specification");
		}
		if($_POST['Criteria'][$criteriaIndex]['ElementName'] == '*'){
			$criterion = self::createBasicSearchSubtree($criteriaIndex, $queryTypes);
		}else{
			$criterion = new CAS_Filemgr_TermQueryCriteria();
			$criterion->setElementName($_POST['Criteria'][$criteriaIndex]['ElementName']);
			$criterion->setValue($_POST['Criteria'][$criteriaIndex]['Value']);
		}
		return $criterion;
	}
	
	public static function createRangeCriteria($criteriaIndex){
		if(!isset($_POST['Criteria'][$criteriaIndex]['ElementName'])){
			throw new CasBrowserException("Query Term criterion " . $criteriaIndex . " does not contain 'ElementName' specification");
		}
		if(!isset($_POST['Criteria'][$criteriaIndex]['Min'])){
			throw new CasBrowserException("Query Range criterion " . $criteriaIndex . " does not contain 'Min' specification");
		}
		if(!isset($_POST['Criteria'][$criteriaIndex]['Max'])){
			throw new CasBrowserException("Query Range criterion " . $criteriaIndex . " does not contain 'Max' specification");
		}
		$criterion = new CAS_Filemgr_RangeQueryCriteria();
		$criterion->setElementName($_POST['Criteria'][$criteriaIndex]['ElementName']);
		$criterion->setStartValue($_POST['Criteria'][$criteriaIndex]['Min']);
		$criterion->setEndValue($_POST['Criteria'][$criteriaIndex]['Max']);
		if(isset($_POST['Criteria'][$criteriaIndex]['Inclusive'])){
			$criterion->setInclusive($_POST['Criteria'][$criteriaIndex]['Inclusive']);
		}
		return $criterion;
	}
	
	public static function createBooleanCriteria($criteriaIndex, $queryTypes, $createdIndices){
		if(!isset($_POST['Criteria'][$criteriaIndex]['Operator'])){
			throw new CasBrowserException("Query Boolean criterion " . $criteriaIndex . " does not contain 'Operator' specification");
		}
		if(!isset($_POST['Criteria'][$criteriaIndex]['CriteriaTerms'])){
			throw new CasBrowserException("Query Boolean criterion " . $criteriaIndex . " does not contain 'CriteriaTerms' specification");
		}
		if(!count($_POST['Criteria'][$criteriaIndex]['CriteriaTerms'])){
			throw new CasBrowserException("Query Boolean criterion " . $criteriaIndex . " does not contain any terms");
		}
		$criterion = new CAS_Filemgr_BooleanQueryCriteria();
		$operator = trim(strtoupper($_POST['Criteria'][$criteriaIndex]['Operator']));
		if($operator == 'AND'){
			$criterion->setOperator(CAS_Filemgr_BooleanQueryCriteria::$AND_OP);
		}elseif($operator == 'OR'){
			$criterion->setOperator(CAS_Filemgr_BooleanQueryCriteria::$OR_OP);
		}elseif($operator == 'NOT'){
			if(count($_POST['Criteria'][$criteriaIndex]['CriteriaTerms']) != 1){
				throw new CasBrowserException("Query Boolean criterion " . $criteriaIndex . " cannot negate more than one term");
			}
			$criterion->setOperator(CAS_Filemgr_BooleanQueryCriteria::$NOT_OP);
		}else{
			throw new CasBrowserException("Error: Query Boolean criterion " . $criteriaIndex . " tries to use undefined operator '" . $operator . "'");
		}
		foreach(array_map("intval", $_POST['Criteria'][$criteriaIndex]['CriteriaTerms']) as $childIndex){
			if(in_array($childIndex, $createdIndices)){		// Check for loops in criteria tree
				throw new CasBrowserException("Criterion " . $criteriaIndex . " lists " . $childIndex . "as a child, making a loop.");
			}
			array_push($createdIndices, $childIndex);
			$child = self::createCriteriaTree($childIndex, $queryTypes);
			$criterion->addTerm($child);
		}
		return $criterion;
	}
	
	public static function createCriteriaTree($criteriaIndex, $queryTypes, $createdIndices=null){
		if(!isset($createdIndices)){
			$createdIndices = array();
		}
		if(!isset($_POST['Criteria'][$criteriaIndex])){
			throw new CasBrowserException("Query Boolean criterion " . $criteriaIndex . " does not exist.");
		}
		$type = strtolower($_POST['Criteria'][$criteriaIndex]['CriteriaType']);
		if($type == 'term'){
			$criterion = self::createTermCriteria($criteriaIndex, $queryTypes);
		}elseif($type == 'range'){
			$criterion = self::createRangeCriteria($criteriaIndex);
		}elseif($type == 'boolean'){
			$criterion = self::createBooleanCriteria($criteriaIndex, $queryTypes, $createdIndices);
		}else{
			throw new CasBrowserException("Query criterion " . $criteriaIndex . " contains an unknown type " . $type . ".  Please use one of 'term', 'range' or 'boolean'");
		}
		return $criterion;
	}
	
	public static function getMetadataNamesForTypeProducts($type){
		$cb = new CasBrowser();
		$client = $cb->getClient();
		$elementNames = array();
		foreach($client->getElementsByProductType($type) as $e){
			array_push($elementNames, $e->getElementName());
		}
		foreach(array_keys($type->getTypeMetadata()->toAssocArray()) as $typeElementName){
			if(!in_array($typeElementName, $elementNames)){
				array_push($elementNames, $typeElementName);
			}
		}
		return $elementNames;
	}
	
	public static function getFacets(){
		$cb = new CasBrowser();
		$client = $cb->getClient();
		$types = $client->getProductTypes();
		$facets = self::getMetadataNamesForTypeProducts(array_pop($types));
		if(count($types) == 0){
			return $facets;	// In case there is only one product type
		}
		foreach($types as $type){
			$elementNames = self::getMetadataNamesForTypeProducts($type);
			$facets = array_intersect($facets, $elementNames);
		}
		return $facets;
	}
	
	public static function paginate($allProducts, $pageNum, $pageSize){
		if(count($allProducts) == 0){
			return array();
		}
		if($pageSize <= 0){
			throw new CasBrowserException("The given PageSize (" . $pageSize . ") was zero or less.");
		}
		$startIndex = ($pageNum - 1) * $pageSize;
		if($startIndex >= count($allProducts)){
			throw new CasBrowserException("The starting index of the requested page (" .
					$startIndex . ") is greater than the last index of all products (" .
					count($allProducts) . ").");
		}
		$endIndex = $startIndex + $pageSize - 1;
		$endIndex = min($endIndex, count($allProducts) - 1);
		$requestedProducts = array();
		for($i = $startIndex; $i <= $endIndex; $i++){
			array_push($requestedProducts, $allProducts[$i]);
		}
		return $requestedProducts;
	}
	
	public static function formatResults($products){
		$cb = new CasBrowser();
		$client = $cb->getClient();
		$results = array();
		foreach($products as $product){
			try{
				$p = array('id'=>$product['product']->getId(),
						'name'=>urlDecode($product['product']->getName()),
						'metadata'=>$client->getMetadata($product['product'])->toAssocArray());
				if(isset($product['typeName'])){
					$p['type'] = $product['typeName'];
				}
				array_push($results, $p);
			}catch(Exception $e){
				throw new CasBrowserException("An error occured while formatting product [" .
						$product['product']->getId() . "] metadata: " . $e->getMessage());
			}
		}
		return $results;
	}
	
	public static function reportError($message, $outputFormat){
		if($outputFormat == 'html'){
			echo '<div class="error">' . $message . '</div>';
		}elseif($outputFormat == 'json'){
			$payload = array();
			$payload['Error'] = 1;
			$payload['ErrorMsg'] = $message;
			$payload = json_encode($payload);
			echo $payload;
		}
		exit();
	}
	
}

class CasBrowserException extends Exception{}

?>
