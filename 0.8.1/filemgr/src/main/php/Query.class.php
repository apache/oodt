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
 * @author resneck
 *
 * <p>A PHP port of the core filemgr data structure.</p>
 */
class CAS_Filemgr_Query {

	/**
	 *<p>The set of QueryCriteria for this Query.</p>
	 */
	public $criteria = null;
	
	/**
	 * <p>Default Constructor.</p>
	 */
	function __construct(){
		$this->criteria = array();
	}
	
	function __init($criteria){
		$this->criteria = $criteria;
	}
	
	function __initXmlRpc($xmlRpcData){
		$this->criteria = (isset($xmlRpcData['criteria']))
			? $xmlRpcData['criteria']
			: array();
	}
	
	/**
     * @return Returns the criteria.
     */
	function getCriteria(){
		return $this->criteria;
	}
	
	/**
     * @param criteria
     *            The criteria to set.
     */
	function setCriteria($criteria){
		$this->criteria = $criteria;
	}
	
	/**
     * @param criterion
     *            The criterion to add to the query.
     */
	function addCriterion($criterion){
		array_push($this->criteria, $criterion);
	}
	
	function toAssocArray(){
		$assocArray = array(
			'criteria' => array());
		foreach($this->criteria as $c){
			array_push($assocArray['criteria'], $c->toAssocArray());
		}
		return $assocArray;
	}
	
	function toXmlRpcStruct(){
		$critArray = array();
		foreach($this->criteria as $c){
			array_push($critArray, $c->toXmlRpcStruct());
		}
		return new XML_RPC_Value(array(
			'criteria' => new XML_RPC_Value($critArray,'array')), 'struct');
	}

}

?>
