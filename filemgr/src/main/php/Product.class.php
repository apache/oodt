<?php
//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

require_once("XML/RPC.php");

/**
 * @author ahart
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
			? new ProductType($xmlRpcData['type']) 
			: new ProductType();
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