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
 * NullAuthorizationProvider
 * 
 * An implementation of the iApplicationAuthorizationProvider interface with
 * no external dependencies (and also limited functionality) intended to be used
 * as a development tool for quickly testing and debugging applications without
 * having to set up detailed auth&auth dependencies. 
 * 
 * This class simply returns an empty array for all usernames, indicating that 
 * the user is not part of any groups/roles.
 * 
 */
class Org_Apache_Oodt_Balance_Providers_Authorization_NullAuthorizationProvider
implements Org_Apache_Oodt_Balance_Interfaces_IApplicationAuthorizationProvider {

	public function __construct() {

	}

	public function retrieveGroupsForUser($username) {
		return array();
	}
		
	public function connect() {
		return true;
	}
}
