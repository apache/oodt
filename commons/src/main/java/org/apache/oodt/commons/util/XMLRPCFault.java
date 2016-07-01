// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

/** XML-RPC fault.
 *
 * This exception is thrown when a fault is returned from an XML-RPC call.
 *
 * @author Kelly
 */
public class XMLRPCFault extends Exception {
	/** Constructor.
	 *
	 * @param code Fault code.
	 * @param string Fault string.
	 */
	public XMLRPCFault(int code, String string) {
		super(code + ": " + string);
		this.code = code;
		this.string = string;
	}

	/** Get the fault code.
	 *
	 * @return The fault code.
	 */
	public int getCode() {
		return code;
	}

	/** Get the fault string.
	 *
	 * @return The fault string.
	 */
	public String getString() {
		return string;
	}

	/** Fault code. */
	private int code;

	/** Fault string. */
	private String string;
}
