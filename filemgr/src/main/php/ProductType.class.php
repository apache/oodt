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
require_once("Metadata.class.php");

/**
 * Ports the core file manager data structure, 
 * ProductType, to PHP.
 * 
 * @author ahart
 * @author mattmann
 * @author gabe
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
		    ? new CAS_Filemgr_Metadata($xmlRpcData['typeMetadata'])
		    : new CAS_Filemgr_Metadata();
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
