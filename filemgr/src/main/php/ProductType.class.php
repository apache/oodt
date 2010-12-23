<?php
//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

require_once("XML/RPC.php");
require_once("CAS/Filemgr/Metadata.class.php");

/**
 * Ports the core file manager data structure, 
 * ProductType, to PHP.
 * 
 * @author ahart
 * @author mattmann
 * 
 */
class CAS_Filemgr_ProductType {
	
	 public $id;
	 public $name;
	 public $description;
	 public $repositoryPath;
	 public $versionerClass;
	 public $typeMetadata;
	
	function __construct($xmlRpcData = array()){
		$this->id = (isset($xmlRpcData['id']))
			? $xmlRpcData['id'] 
			: '';
		$this->name = (isset($xmlRpcData['name']))
			? $xmlRpcData['name'] 
			: '';
		$this->description = (isset($xmlRpcData['description']))
			? $xmlRpcData['description'] 
			: '';
		$this->repositoryPath = (isset($xmlRpcData['repositoryPath']))
			? $xmlRpcData['repositoryPath'] 
			: '';
		$this->versionerClass = (isset($xmlRpcData['versionerClass']))
			? $xmlRpcData['versionerClass'] 
			: '';
		$this->typeMetadata = (isset($xmlRpcData['typeMetadata'])) 
		    ? new Metadata($xmlRpcData['typeMetadata'])
		    : new Metadata();
	}
	
	function __destruct(){
		
	}
	
	function toAssocArray(){
		return array(
			'id' => $this->id,
			'name' => $this->name,
			'description' => $this->description,
			'repositoryPath' => $this->repositoryPath,
			'versionerClass' => $this->versionerClass,
			'typeMetadata' => $this->typeMetadata->toAssocArray());
	}
	
	function toXmlRpcStruct(){
		return new XML_RPC_Value(array(
			'id' => new XML_RPC_Value($this->id,'string'),
			'name' => new XML_RPC_Value($this->name,'string'),
			'description' => new XML_RPC_Value($this->description,'string'),
			'repositoryPath' => new XML_RPC_Value($this->repositoryPath,'string'),
			'versionerClass' => new XML_RPC_Value($this->versionerClass,'string'),
            'typeMetadata' => $this->typeMetadata->toXmlRpcStruct()), 'struct');
	}
	
	/*
	 * Getter/Setter Functions
	 */
	function getId(){return $this->id;}
	function getName(){return $this->name;}
	function getDescription(){return $this->description;}
	function getRepositoryPath(){return $this->repositoryPath;}
	function getVersionerClass(){return $this->versionerClass;}
	function getTypeMetadata(){return $this->typeMetadata;}
	
	function setId($val){$this->id = $val;}
	function setName($val){$this->name = $val;}
	function setDescription($val){$this->description = $val;}
	function setRepositoryPath($val){$this->repositoryPath = $val;}
	function setVersionerClass($val){$this->versionerClass = $val;}
	function setTypeMetadata($val){$this->typeMetadata = $val;}
	
}

?>