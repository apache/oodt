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

require_once("XML/RPC.php");

/**
 * XML-RPC PHP version of the file manager client.
 * 
 * Not all of the core functionality/methods are ported yet
 * but this is a good start.
 * 
 * @author ahart
 * @author resneck
 * 
 */
class CAS_Filemgr_XmlRpcFilemgrClient {
	private $client;		// The XML/RPC client
	public $serverURL;		// The base URL of the server (default is localhost:9000)
	public $serverPath;		// The path to the XMLRPC handler (/path/to/xmlrpc)
	
	function __construct($serverURL = 'localhost:9000',$serverPath = '/'){
		$this->serverURL = $serverURL;
		$this->serverPath = $serverPath;
		try {
			$this->client = new XML_RPC_Client($this->serverPath,$this->serverURL);
		} catch (Exception $e) {
			echo "<h4>Error creating XMLRPC client: " . $e.getMessage() . "</h4>";
			exit();
		}
	}
	
	function __destruct(){}

	/**
	 * @param methodStr
     *		The string for the method to be invoked on ther server.
	 *
	 * @param params
     *		An array of XML-RPC encoded parameters to the method that will be invoked on the server.
     *
     * @param falseOnFail
     *		If set to true, sendMessage will return false on a failure, rather than exiting.
	 *
	 * @return The un-decoded data from the method invoked on the server.
	 */
	private function sendMessage($methodStr, $params){
		$message = new XML_RPC_Message($methodStr, $params);
		$response = $this->client->send($message);
		if (!$response){
			throw new XmlRpcFilemgrException("No response was received from Xml-Rpc Client\nError msg: " . $this->client->errstr);
		}
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			if(isset($data->faultString)){
				throw new XmlRpcFilemgrException("Fault String in Xml-Rpc response: " . $data["faultString"]);
			}
			return $data;
		} else {
			throw new XmlRpcFilemgrException("Fault Code in Xml-Rpc response: " . $response->faultCode);
		}
	}
	
	/**
	 * Throws an XmlRpcFilemgrException that gives more information than the one given.
	 * 
	 * @param e
	 * 		The exception that needs more info included.
	 * 
	 * @param funcName
	 * 		The name of the function in which this the original erroe/exception occurred.
	 */
	private function reportError($e, $funcName){
		throw new XmlRpcFilemgrException("An error occurred while performing function " . $funcName . "\n" . $e->getMessage());
	}
	
	/**
	 * @return A boolean indicating whether the FileManager is still running.
	 */
	public function isAlive( $returnObject = true ) {
       	$params = array();
       	try{
	       	$data = $this->sendMessage("filemgr.isAlive", $params, $returnObject);
       	}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "isAlive");
       	}
       	return $data;
    }

	/**
	 * @param productID
     *		The id of the desired Product.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return The Product corresponding to the given id.
	 */
	function getProductById($productID, $returnObject = true){
		$params = array(new XML_RPC_Value($productID,'string'));
		try{
			$data = $this->sendMessage("filemgr.getProductById", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getProductById");
       	}
		if($returnObject){
			$product = new CAS_Filemgr_Product($data);
			return $product;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param product
     *		The product containing the desired references.
	 *
	 * @return An associative array specifying the references contained in the given Product.
	 */
	function getProductReferences($product){
		$params = array($product->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getProductReferences", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getProductReferences");
       	}
		return $data;
	}
	
	/**
	 * @param product
     *		The product specified by the desired metadata.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return The metadata specifiying the given Product.
	 */
	function getMetadata($product, $returnObject = true){	
		$params = array($product->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getMetadata", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getMetadata");
       	}
		if($returnObject){
			$metadata = new CAS_Filemgr_Metadata($data);
			return $metadata;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param typeID
     *		The ID of the desired ProductType defined for this instance of the File Manager.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return The ProductType coresponding to the given ID.
	 */
	function getProductTypeById($typeID, $returnObject = true){
		$params = array(new XML_RPC_Value($typeID,'string'));
		try{
			$data = $this->sendMessage("filemgr.getProductTypeById", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getProductTypeById");
       	}
		if($returnObject){
			$pt = new CAS_Filemgr_ProductType($data);
			return $pt;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param typeName
     *		The name of the desired ProductType defined for this instance of the File Manager.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return The ProductType coresponding to the given name.
	 */
	function getProductTypeByName($typeName, $returnObject = true){
		$params = array(new XML_RPC_Value($typeName,'string'));
		try{
			$data = $this->sendMessage("filemgr.getProductTypeByName", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getProductTypeByName");
       	}
		if($returnObject){
			$pt = new CAS_Filemgr_ProductType($data);
			return $pt;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param type
     *		The desired ProductType defined for this instance of the File Manager.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return An array of all Products in the repository of the given ProductType.
	 */
	function getProductsByProductType($type, $returnObject = true){
		$params = array($type->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getProductsByProductType", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getProductsByProductType");
       	}
		if($returnObject){
			$products = array();
			foreach($data as $d){
				$p = new CAS_Filemgr_Product($d);
				array_push($products, $p);
			}
			return $products;
		}else{
			return $data;
		}
	}

	/**
	 * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return An array of all ProductTypes defined for this instance of the File Manager.
	 */
	function getProductTypes($returnObject = true){
		$params = array();
		try{
			$data = $this->sendMessage("filemgr.getProductTypes", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getProductTypes");
       	}
		if($returnObject){
			$types = array();
			foreach($data as $d){
				$pt = new CAS_Filemgr_ProductType($d);
				array_push($types, $pt);
			}
			return $types;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param type
     *		The desired ProductType defined for this instance of the File Manager.
	 *
	 * @return The number of products of the given ProductType in the repository.
	 */
    function getNumProducts($type){
		$params = array($type->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getNumProducts", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getNumProducts");
       	}
		return $data;
	}
	
	/**
	 * @param query
     *		A Query object containing QueryCrteria objects defining the search.
	 *
	 * @param type
     *		A ProductType object defining what types to search.
     *
     * @param pageNum
     *		The desired number of the page in the set of query results.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return A ProductPage object containing query results
	 */
	function pagedQuery($query, $type, $pageNum, $returnObject = true){
		$params = array(
			$query->toXmlRpcStruct(),
			$type->toXmlRpcStruct(),
			new XML_RPC_Value($pageNum,'int'));
		try{
			$data = $this->sendMessage("filemgr.pagedQuery", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "pagedQuery");
       	}
		if($returnObject){
			$productPage = new CAS_Filemgr_ProductPage();
			$productPage->__initXmlRpc($data);
			return $productPage;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param query
     *		A Query object containing QueryCrteria objects defining the search.
	 *
	 * @param type
     *		A ProductType object defining what types to search.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return An array of Product objects as query results.
	 */
	function query($query, $type, $returnObject = true){
		$params = array(
			$query->toXmlRpcStruct(),
			$type->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.query", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "query");
       	}
		if($returnObject){
			$products = array();
			foreach($data as $d){
				$p = new CAS_Filemgr_Product($d);
				array_push($products, $p);
			}
			return $products;
		}else{
			return $data;
		}
	}
	
	/**
	 * @param type
     *		The desired ProductType defined for this instance of the File Manager.
     *
     * @param returnObject
     *		Whether data should be returned as an object as opposed to an associative array.
	 *
	 * @return An array of Element objects that are mapped to the given ProductType.
	 */
	function getElementsByProductType($type, $returnObject = true){
		$params = array($type->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getElementsByProductType", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getElementsByProductType");
       	}
		if($returnObject){
			$elements = array();
			foreach($data as $d){
				$e = new CAS_Filemgr_Element();
				$e->__initXmlRpc($d);
				array_push($elements, $e);
			}
			return $elements;
		}else{
			return $data;
		}
	}
	
	/* Pagination API */
	
	function getFirstPage($type, $returnObject = true){
		$params = array($type->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getFirstPage", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getFirstPage");
       	}
		if($returnObject){
			$page = new CAS_Filemgr_ProductPage();
			$page->__initXmlRpc($data);
			return $page;
		}else{
			return $data;
		}
	}
	
	function getNextPage($type, $page, $returnObject = true){
		$params = array($type->toXmlRpcStruct(), $page->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getNextPage", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getNextPage");
       	}
		if($returnObject){
			$page = new CAS_Filemgr_ProductPage();
			$page->__initXmlRpc($data);
			return $page;
		}else{
			return $data;
		}	
	}
	
	function getPrevPage($type, $page, $returnObject = true){
		$params = array($type->toXmlRpcStruct(), $page->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getPrevPage", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getPrevPage");
       	}
		if($returnObject){
			$page = new CAS_Filemgr_ProductPage();
			$page->__initXmlRpc($data);
			return $page;
		}else{
			return $data;
		}			
	}
	
	function getLastPage($type, $returnObject = true){
		$params = array($type->toXmlRpcStruct());
		try{
			$data = $this->sendMessage("filemgr.getLastPage", $params);
		}catch(XmlRpcFilemgrException $e){
       		$this->reportError($e, "getLastPage");
       	}
		if($returnObject){
			$page = new CAS_Filemgr_ProductPage();
			$page->__initXmlRpc($data);
			return $page;
		}else{
			return $data;
		}		
	}
	
}

class XmlRpcFilemgrException extends Exception{}

?>
