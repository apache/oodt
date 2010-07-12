// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: LDAP.java,v 1.1.1.1 2004-02-28 13:09:17 kelly Exp $

package jpl.eda.util;

import java.io.*;
import java.util.*;

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
		StringBuffer result = new StringBuffer();
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
