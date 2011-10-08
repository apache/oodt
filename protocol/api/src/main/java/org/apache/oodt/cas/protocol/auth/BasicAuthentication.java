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
package org.apache.oodt.cas.protocol.auth;

import org.apache.commons.lang.Validate;

/**
 * Basic username and password {@link Authentication}
 * 
 * @author bfoster
 * @version $Revision$
 */
public class BasicAuthentication implements Authentication {

	private String user;
	private String pass;
	
	public BasicAuthentication(String user, String pass) {
		Validate.notNull(user, "NULL user not allowed");
		Validate.notNull(pass, "NULL pass not allowed");
		this.user = user;
		this.pass = pass;
	}
	
	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}
}
