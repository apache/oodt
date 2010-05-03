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
			echo "<h4>Error creating XMLRPC client: " . $e.getMessage();
			exit();
		}
	}
	
	function __destruct(){

	}


	function getProductById($productID){
		$params = array(new XML_RPC_Value($productID,'string'));
		$message = new XML_RPC_Message('filemgr.getProductById',$params);
		$response = $this->client->send($message);
		
		if (!$response){
			echo "<h4>Communication Error: {$this->client->errstr}</h4>";
			exit(); 
		}
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}
	}
	function getProductReferences($product){
		$params = array($product->toXmlRpcStruct());
		$message = new XML_RPC_Message('filemgr.getProductReferences',$params);
		$response = $this->client->send($message);
		
		if (!$response){
			echo "<h4>Communication Error: {$this->client->errstr}</h4>";
			exit(); 
		}
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}
	}
	
	function getMetadata($product){	
		$params = array($product->toXmlRpcStruct());
		$message = new XML_RPC_Message('filemgr.getMetadata',$params);
		$response = $this->client->send($message);
		
		if (!$response){
			echo "<h4>Communication Error: {$this->client->errstr}</h4>";
			exit(); 
		}
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}
	}
	
	function getProductTypeById($typeID){
		$params = array(new XML_RPC_Value($typeID,'string'));
		$message = new XML_RPC_Message("filemgr.getProductTypeById", $params);
		$response = $this->client->send($message);
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}		
		
	}
	
	function getProductsByProductType($type){
		$params = array($type->toXmlRpcStruct());
		$message = new XML_RPC_Message("filemgr.getProductsByProductType", $params);
		$response = $this->client->send($message);
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}		
		
	}

	function getProductTypes(){
		$params = array();
		$message = new XML_RPC_Message("filemgr.getProductTypes", $params);
		$response = $this->client->send($message);
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}		
		
	}
		
	
	
    function getNumProducts($type){
		$params = array($type->toXmlRpcStruct());
		$message = new XML_RPC_Message("filemgr.getNumProducts", $params);
		$response = $this->client->send($message);
		
		if (!$response->faultCode()) {
			$value = $response->value();
			$data = XML_RPC_decode($value);
			return $data;
		} else {
			echo "<h4>Fault Code Returned: (" . $response->faultCode . ") </h4>";
			exit();
		}		
		
	}
}
?>