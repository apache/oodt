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
/**
 * 
 * $Id$
 * 
 * CAS-Browser Module
 * 
 * This module provides applications a means for browsing a CAS File 
 * Manager catalog and obtaining products from a CAS File Manager repository.
 * 
 * For complete functionality, the following configuration variables
 * are expected to be present in the module's config.ini file:
 * 
 * browser_filemgr_url    - filemanager host (e.g.: http://somehost:9000)
 * browser_filemgr_path   - filemanager url on server (e.g.: /)  
 * browser_datadeliv_url  - the base url to use when downloading products
 *   
 * NOTE: This module has a dependency upon the CAS-Filemgr PHP classes
 * (https://svn.apache.org/repos/asf/oodt/trunk/filemgr/src/main/php)
 *      
 *      To build this dependency, check out the above project and then:
 *      1) cd into the checked out project (you should see a package.xml file)
 *      2) pear package
 *      3) (sudo) pear install --force CAS_Filemgr...tar.gz
 *   
 * @author ahart
 * @author resneck
 *
 */
// Require CAS Filemgr Classes
require_once("CAS/Filemgr/BooleanQueryCriteria.class.php");
require_once("CAS/Filemgr/Element.class.php");
require_once("CAS/Filemgr/Metadata.class.php");
require_once("CAS/Filemgr/Product.class.php");
require_once("CAS/Filemgr/ProductType.class.php");
require_once("CAS/Filemgr/ProductPage.class.php");
require_once("CAS/Filemgr/Query.class.php");
require_once("CAS/Filemgr/RangeQueryCriteria.class.php");
require_once("CAS/Filemgr/TermQueryCriteria.class.php");
require_once("CAS/Filemgr/XmlRpcFilemgrClient.class.php");
require_once(dirname(__FILE__) . "/Utils.class.php");


class CasBrowser {
	
	const VIS_INTERPRET_HIDE     = 'hide';
	const VIS_INTERPRET_SHOW     = 'show';
	const VIS_AUTH_ANONYMOUS     = false;
	const VIS_AUTH_AUTHENTICATED = true;
	const VIS_ALL                = 'all';
	const VIS_LIMIT              = 'limit';
	const VIS_NONE               = 'deny';
	const VIS_DENY               = 'deny';
	
	public $client;
	
	public function __construct() {
		try {
			$this->client = new CAS_Filemgr_XmlRpcFilemgrClient(
				App::Get()->settings['browser_filemgr_url'],
				App::Get()->settings['browser_filemgr_path']);
		} catch (Exception $e) {
			App::Get()->fatal("Unable to instantiate a connection to "
				. App::Get()->settings['browser_filemgr_url']
				. App::Get()->settings['browser_filemgr_path']);
		}
	}
	
	public function getClient() {
		return $this->client;
	}
	
	/**
	 * Use the rules in element-ordering.ini to determine the display order
	 * for product type metadata elements. See element-ordering.ini for more
	 * information on how to specify element order rules.
	 * 
	 * @param integer $productTypeId  The id of the product type to get met for
	 * @param array   $metadataTouse  An optional array of metadata key/vals to sort. If
	 *                this is not provided, the product type metadata will be used.
	 */
	public function getSortedMetadata($productTypeId,$metadataToUse = null, $orderingAttribute) {
		
		if (!is_array($metadataToUse)) {
			$pt = $this->client
				->getProductTypeById($productTypeId)
				->toAssocArray();
			$metadataAsArray = $pt['typeMetadata'];
		} else {
			$metadataAsArray = $metadataToUse;
		}
		
		$orderingPolicyFilePath = dirname(dirname(__FILE__)) . '/element-ordering.ini';
		if (file_exists($orderingPolicyFilePath)) {
			$orderPolicy = parse_ini_file($orderingPolicyFilePath,true);

			$first    = isset($orderPolicy[$productTypeId][$orderingAttribute . '.element.ordering.first']) 
				? $orderPolicy[$productTypeId][$orderingAttribute . '.element.ordering.first']
				: $orderPolicy['*'][$orderingAttribute . '.element.ordering.first'];
			$last     = isset($orderPolicy[$productTypeId][$orderingAttribute . '.element.ordering.last']) 
				? $orderPolicy[$productTypeId][$orderingAttribute . '.element.ordering.last']
				: $orderPolicy['*'][$orderingAttribute . '.element.ordering.last'];
								
			// Using the odering policy, determine the order in which the metadata will be listed
			return $this->sortMetadata($metadataAsArray,$first,$last);	
		} else {
			return $metadataAsArray;
		}
	}
	
	/**
	 * Retreives the set of metadata for the provided productTypeId that should be visible
	 * to the current user. This function also applies the sorting policy (if it is defined)
	 * specified in element.ordering.ini.
	 * 
	 * @param string $productTypeId - the unique productType identifier
	 */
	public function getVisibleMetadataForProductType($productTypeId) {
		// Get the metadata for the product type
		$pt = $this->client
			->getProductTypeById($productTypeId)
			->toAssocArray();
			
		// Determine which metadata should be visible to the current user
		$visibleMetadata = $this->getVisibleMetadata($pt['typeMetadata'], $productTypeId);
		
		// Sort the visible metadata according to the ordering policy
		$result  = $this->getSortedMetadata($productTypeId,$visibleMetadata, 'pt');
		
		return $result;
	}
	
	
	/**
	 * Retrieves the set of metadata for the provided productId that should be visible to 
	 * the current user
	 * 
	 * @param string  $productId - the unique product identifier
	 * @param boolean $authState - whether or not the current user is authenticated
	 */
	public function getVisibleMetadataForProduct($productId) {
		$p  = $this->client->getProductById($productId);
		$productTypeInfo = $p->getType()->toAssocArray();
		$productTypeId   = $productTypeInfo[App::Get()->settings['browser_pt_id_key']];
		$productMetadata = $this->client->getMetadata($p);
		
		// Determine which metadata should be visible to the current user
		$visibleMetadata = $this->getVisibleMetadata($productMetadata->toAssocArray(), $productTypeId);
				
		// Sort the visible metadata according to the ordering policy
		$result  = $this->getSortedMetadata($productTypeId,$visibleMetadata, 'p');
		
		return $result;		
	}
	
	
	/**
	 * Determine the visibility level for the current product type and current user.
	 * The level returned is one of VIS_ALL,VIS_LIMIT,VIS_NONE
	 * 
	 * @param string $productTypeId - the unique product type identifier
	 */
	public function getProductTypeVisibilityLevel( $productTypeId ) {
		// If the configuration explicitly states that this dataset is to be ignored,
		// ignore it:
		if (in_array($productTypeId,App::Get()->settings['browser_dataset_ignores'])) {
			return CasBrowser::VIS_NONE;
		}
		
		// Get the metadata for the product type
		$typeInfo = $this->client
			->getProductTypeById($productTypeId)
			->toAssocArray();
		
		if ( App::Get()->getAuthenticationProvider() ) {
			
			// Does the product type define a metadata element matching
			// the `browser_data_access_key` config setting?
			$accessKeyExists = isset($typeInfo['typeMetadata'][App::Get()->settings['browser_data_access_key']]);
			
			// Obtain the groups for the current resource
			$resourceGroups = ($accessKeyExists)
				? $typeInfo['typeMetadata'][App::Get()->settings['browser_data_access_key']]
				: array();
			
			return $this->getResourceVisibility($resourceGroups,
				App::Get()->settings['browser_pt_auth_policy']);
		} else {
			// No authentication provider, everything is public
			return CasBrowser::VIS_ALL;
		}
	}
	
	public function getProductVisibilityLevel( $productId ) {
		
		$product = $this->client->getProductById( $productId );
		$productMetadata = $this->client->getMetadata($product);

		// Get metadata for product and productType as associative arrays
		$productTypeInfo = $product->getType()->toAssocArray();
		$productInfo     = $productMetadata->toAssocArray();
		
		if ( App::Get()->getAuthenticationProvider() ) {
			
			// Does the product type define a metadata element matching
			// the `browser_data_access_key` config setting?
			$accessKeyExists = isset($productInfo[App::Get()->settings['browser_data_access_key']]);
			 
			// Obtain the groups for the current resource
			$resourceGroups = ($accessKeyExists)
				? $productInfo[App::Get()->settings['browser_data_access_key']]
				: array();
			
			return $this->getResourceVisibility($resourceGroups, 
				App::Get()->settings['browser_p_auth_policy']);
		} else {
			// No authentication provider, everything is public
			return CasBrowser::VIS_ALL;
		}
	}
	
	
	/**
	 * Internal helper function for sorting(ordering) a metadata array according to policy. 
	 * 
	 * @param array $unsortedMetadata An associative array of unsorted metadta key/(multi)values
	 * @param array $sortFirst        A scalar array of metadata keys that must be ordered first
	 * @param array $sortLast         A scalar array of metadata keys that must be ordered last
	 * @returns array An associative array of sorted(ordered) metadata key/(multi)values
	 */
	protected function sortMetadata($unsortedMetadata,$sortFirst,$sortLast) {
		$orderedMetadata = array();
		foreach ($sortFirst as $key) {
			if (isset($unsortedMetadata[$key])) {
				$orderedMetadata[$key] = $unsortedMetadata[$key];
				unset($unsortedMetadata[$key]);
			}
		}
		$lastMetadata = array();
		foreach ($sortLast as $key) {
			if (isset($unsortedMetadata[$key])) {
				$lastMetadata[$key] = $unsortedMetadata[$key];
				unset($unsortedMetadata[$key]);
			}
		}
		$orderedMetadata += $unsortedMetadata;
		$orderedMetadata += $lastMetadata;
		
		return $orderedMetadata;
	}
	
	/**
	 * Internal helper function for, given an array of metadata, a productTypeID, and an indication of whether or not the 
	 * current user is authenticated, returning the subset of metadata that should be visible to
	 * the user. 
	 * 
	 * @param array   $metadataAsArray
	 * @param string  $productTypeId
	 * @param boolean $longinState - one of VIS_AUTH_AUTHENTICATED|VIS_AUTH_ANONYMOUS
	 */
	protected function getVisibleMetadata($metadataAsArray, $productTypeId) {
		
		// Determine whether the user is authenticated
		$authState = (($ap = App::Get()->getAuthenticationProvider()) && $ap->isLoggedIn());
		
		$visibilityPolicyFilePath = dirname(dirname(__FILE__)) . '/element-visibility.ini';
		if (file_exists($visibilityPolicyFilePath)) {
			$visibilityPolicy = parse_ini_file($visibilityPolicyFilePath,true);
			
			$interpretation = $visibilityPolicy['interpretation.policy'];
			$globalVisibilityPolicy = $visibilityPolicy['*'];
			$productTypeVisibilityPolicy = isset($visibilityPolicy[$productTypeId])
				? $visibilityPolicy[$productTypeId]
				: array("visibility.always" => array(),
						"visibility.anonymous" => array(),
						"visibility.authenticated" => array());

			// The visibility of a given metadata element is dependent upon
			//   (1) the authentication status of the user (VIS_AUTH_AUTHENTICATED|VIS_AUTH_ANONYMOUS)
			//   (2) the interpretation of the visibility policy (VIS_INTERPRET_SHOW|VIS_INTERPRET_HIDE)
			//   
			//   Using these values, determine which metadata to display:
			switch ($interpretation) {
				// If the policy defines only those metadata which should be hidden:
				case self::VIS_INTERPRET_HIDE:
					$displayMet = $metadataAsArray;                                     // everything is shown unless explicitly hidden via the policy
					foreach ($globalVisibilityPolicy['visibility.always'] as $elm)      // iterate through the global 'always hide' array...
						unset($displayMet[$elm]);                                       // and remove all listed elements
					foreach ($productTypeVisibilityPolicy['visibility.always'] as $elm) // now iterate through the product-type 'always hide' array...
						unset($displayMet[$elm]);                                       // and remove all listed elements
							
					// Determine what to hide given the user's login state 
					switch ($authState) {                                                             // check the login status of the user
						case self::VIS_AUTH_ANONYMOUS:                                                 // if the user is anonymous...
							foreach($globalVisibilityPolicy['visibility.anonymous'] as $elm)           // iterate through the global 'anonymous hide' array...
								unset($displayMet[$elm]);                                              // and remove all listed elements
							foreach ($productTypeVisibilityPolicy['visibility.anonymous'] as $elm)     // now iterate through the product-type 'anonymous hide' array...
								unset($displayMet[$elm]);                                              // and remove all listed elements
							break;                                                                     // done.
						case self::VIS_AUTH_AUTHENTICATED:                                             // if the user is authenticated...
							foreach($globalVisibilityPolicy['visibility.authenticated'] as $elm)       // iterate through the global 'authenticated hide' array...
								unset($displayMet[$elm]);                                              // and remove all listed elements
							foreach ($productTypeVisibilityPolicy['visibility.authenticated'] as $elm) // now iterate through the product-type 'authenticated hide' array...
								unset($displayMet[$elm]);                                              // and remove all listed elements
							break;                                                                     // done.
					}

					break;
				
				// If the policy defines only those metadata which should be shown:
				case self::VIS_INTERPRET_SHOW:
					$displayMet = $globalVisibilityPolicy['visibility.always']                         // merge the global 'always show' array
						+ $productTypeVisibilityPolicy['visibility.always'];                           // with the product-type specific 'always show' array
					switch ($authState) {                                                              // check the login status of the user
						case self::VIS_AUTH_ANONYMOUS:                                                 // if the user is anonymous...
							$displayMet += $globalVisibilityPolicy['visibility.anonymous'];            // merge the global 'anonymous show' array
							$displayMet += $productTypeVisibilityPolicy['visibility.anonymous'];       // and the product-type specific 'anonymous show' array
							break;                                                                     // done.
						case self::VIS_AUTH_AUTHENTICATED:                                             // if the user is authenticated...
							$displayMet += $globalVisibilityPolicy['visibility.authenticated'];        // merge the global 'authenticated show' array 
							$displayMet += $productTypeVisibilityPolicy['visibility.authenticated'];   // and the product-type specific 'authenticated show' array
							break;                                                                     // done.
					}
			}
			
			return $displayMet;	
				
		} else {
			return $metadataAsArray;
		}
	}
		
	/**
	 * Internal helper function to determine the visibility level of a given resource given
	 * its array of security groups. The value returned is one of VIS_ALL,VIS_LIMIT,VIS_NONE.
	 * 
	 * @param array $resourceGroups - an array of security groups for the resource
	 * @param string $policy        - one of VIS_LIMIT or VIS_DENY
	 */	
	protected function getResourceVisibility( $resourceGroups, $policy = CasBrowser::VIS_DENY ) {
		
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
	 				
			 		if ( $authorization = App::Get()->getAuthorizationProvider() ) {
				 		// Obtain the groups for the current user
				 		$userGroups = $authorization->retrieveGroupsForUser($username);
				 		
				 		// Perform a comparison via array intersection to determine overlap
				 		$x = array_intersect($userGroups,$resourceGroups);
				 		
				 		if (empty($x)) { // No intersection found between user and resource groups
				
				 			// Examine the policy to determine how to handle the failure
				 			switch ($policy) {
				 				case CasBrowser::VIS_LIMIT:
				 					// Allow the user to proceed, the metadata visibility policy
				 					// will be used to determine what is visible to non-authorized
				 					// users.
				 					return CasBrowser::VIS_LIMIT;
				 				default:
				 					// Kick the user out at this point, deny all access. 
					 				return CasBrowser::VIS_NONE;
				 			}
				 		} else {
				 			// We have an authorized user
				 			$authorizedUser = true;
				 		}
			 		} else {
			 			
						// If no authorization provider information exists in the application
					 	// configuration file, it is assumed that every user is authorized once
					 	// logged in.
					 	return CasBrowser::VIS_ALL;				 			
			 		}
			 	} else {
			 		
			 		// If no logged in user, and policy says DENY, kick the user
			 		if ($policy == CasBrowser::VIS_DENY) {
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
			 		if ($policy == CasBrowser::VIS_DENY) {
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
 		}
	}
}
