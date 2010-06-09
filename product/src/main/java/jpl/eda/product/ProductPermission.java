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


package jpl.eda.product;

import java.security.BasicPermission;

/**
 * Permission to use a product server.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class ProductPermission extends BasicPermission {
	/**
	 * Creates a new {@link ProductPermission} instance.
	 *
	 * @param name Name of the product feature to use.
	 */
	public ProductPermission(String name) {
		super(name);
	}

	/**
	 * Creates a new {@link ProductPermission} instance.
	 *
	 * @param name Name of the product feature to use.
	 * @param actions Actions to be performed on the feature.
	 */
	public ProductPermission(String name, String actions) {
		super(name, actions);
	}
}
