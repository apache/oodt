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

require_once ("XML/RPC.php");

/**
 * @author mattmann
 * 
 * A PHP representation of the CAS metadata data structure, which
 * is a structure of key=>List of String values.
 */
class CAS_Filemgr_Metadata {

	public $elemMap;

	function __construct($xmlRpcData = array ()) {
		$this->elemMap = $xmlRpcData;
	}

	function __destruct() {

	}

	function toXmlRpcStruct() {
		$xmlRpcStruct = array ();
		foreach ($this->elemMap as $key => $val) {
			$valList = array ();
			foreach ($val as $v) {
				array_push($valList, new XML_RPC_VALUE($v, 'string'));
			}
			$xmlRpcStruct[$key] = new XML_RPC_VALUE($valList, 'array');
		}

		return new XML_RPC_VALUE($xmlRpcStruct, 'struct');
	}

	function toAssocArray() {
		return $this->elemMap;
	}

	function addMetadata($key, $value) {
		array_push($this->elemMap[$key], $value);
	}

	function replaceMetadata($key, $value) {
		$this->elemMap[$key] = $value;
	}

	function removeMetadata($key) {
		unset ($this->elemMap[$key]);
	}

	function getAllMetadata($key) {
		return $this->elemMap[$key];
	}

	function getMetadata($key) {
		return $this->elemMap[$key][0];
	}

	function containsKey($key) {
		return array_key_exists($key, $this->elemMap);
	}

	function isMultiValued($key) {
		return array_count_values($this->elemMap[$key]) > 1;
	}
}
?>
