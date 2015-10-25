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

/** LDAP services.
 *
 * This class provides LDAP convenience services.
 *
 * @author Kelly
 */
public class LDAP {
	/** Convert the given string into an LDAP-safe query string.
	 *
	 * This method escapes certain characters that are special in LDAP query strings.
	 *
	 * @return An escaped, LDAP-safe string.
	 */
	public static String toLDAPString(String str) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < str.length(); ++i) {
			char ch = str.charAt(i);
			switch (ch) {
				case '*':
					result.append("\\2a");
					break;
				case '(':
					result.append("\\28");
					break;
				case ')':
					result.append("\\29");
					break;
				case '\\':
					result.append("\\5c");
					break;
				default:
					result.append(ch);
					break;
			}
		}
		return result.toString();
	}
}
