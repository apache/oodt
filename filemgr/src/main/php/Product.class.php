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
 * @author ahart
 * @author gabe
 * <p>The core file manager data structure, ported
 * to PHP.
 * </p>
 */
class CAS_Filemgr_Product {
	
	 public $id;
	 public $name;
	 public $type;
	 public $structure;
	 public $transferStatus;
	 public $references;
	

	function __construct($xmlRpcData = array()) {
		$this->id = (isset($xmlRpcData['id']))
			? $xmlRpcData['id'] 
			: '';
		$this->name = (isset($xmlRpcData['name']))
			? $xmlRpcData['name'] 
			: '';
		$this->type = (isset($xmlRpcData['type']))
			? new CAS_Filemgr_ProductType($xmlRpcData['type']) 
			: new CAS_Filemgr_ProductType();
		$this->structure = (isset($xmlRpcData['structure']))
			? $xmlRpcData['structure'] 
			: '';
		$this->transferStatus = (isset($xmlRpcData['transferStatus']))
			? $xmlRpcData['transferStatus'] 
			: '';
		$this->references = (isset($xmlRpcData['references']))
			? $xmlRpcData['references'] 
			: array();
	}
	
	function __destruct() {
		
	}
	
	function toAssocArray(){
		return array(
			'id' => $this->id,
			'name' => $this->name,
			'type' => $this->type->toAssocArray(),
			'structure' => $this->structure,
			'transferStatus' => $this->transferStatus,
			'references' => $this->references);
	}
	
	function toXmlRpcStruct(){
		return new XML_RPC_VALUE(array(
			'id' => new XML_RPC_Value($this->id,'string'),
			'name' => new XML_RPC_Value($this->name,'string'),
			'type' => $this->type->toXmlRpcStruct(),
			'structure' => new XML_RPC_Value($this->structure,'string'),
			'transferStatus' => new XML_RPC_Value($this->transferStatus,'string'),
			'references' => new XML_RPC_Value($this->references,'array')),'struct');
	}
	
	/*
	 * Getter/Setter functions
	 */
	function getId(){return $this->id;}
	function getName(){return $this->name;}
	function getType(){return $this->type;}
	function getStructure(){return $this->structure;}
	function getTransferStatus(){return $this->transferStatus;}
	function getReferences(){return $this->references;}
	
	function setId($val){$this->id = $val;}
	function setName($val){$this->name = $val;}
	function setType($val){$this->type = $val;}
	function setStructure($val){$this->structure = $val;}
	function setTransferStatus(){$this->transferStatus = $val;}
	function setReferences($val){$this->references = $val;}
	
}

?>
