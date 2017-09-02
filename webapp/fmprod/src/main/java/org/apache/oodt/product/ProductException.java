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


package org.apache.oodt.product;

/**
 * Checked exception to indicate a product fault.
 *
 * @author Kelly
 */
public class ProductException extends Exception {
	/**
	 * Construct a product exception with no detail message.
	 */
	public ProductException() {}

	/**
	 * Construct a product exception with the given detail message.
	 *
	 * @param msg Detail message.
	 */
	public ProductException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new <code>ProductException</code> instance.
	 *
	 * @param cause a <code>Throwable</code> value.
	 */
	public ProductException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new <code>ProductException</code> instance.
	 *
	 * @param msg a <code>String</code> value.
	 * @param cause a <code>Throwable</code> value.
	 */
	public ProductException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = 8240102969482071451L;
}
