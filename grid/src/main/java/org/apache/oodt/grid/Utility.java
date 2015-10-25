/**
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

package org.apache.oodt.grid;

/**
 * Utility methods for web grid.
 * 
 */
public class Utility {
  /**
   * Provide XHTML-safe escaping for a string. Basically, this is the same as
   * XML-escaping, but since some user agents don't recognize
   * <code>&amp;apos;</code>, we use <code>&amp;#39;</code> instead.
   * 
   * @param str
   *          String to escape.
   * @return <var>str</var> escaped.
   */
  public static String esc(String str) {
    StringBuilder s = new StringBuilder(str.length()); // Assume at least the same
    // length
    for (int i = 0; i < str.length(); ++i) { // For each character
      char c = str.charAt(i); // Grab the character
      switch (c) { // Now consider what it is ...
      case '<': // A less than?
        s.append("&lt;"); // Well, that's &lt;
        break;
      case '>': // A greater than?
        s.append("&gt;"); // That's &gt;
        break;
      case '&': // An ampersand?
        s.append("&amp;"); // We all know what that is
        break;
      case '\'': // A tick?
        s.append("&#39;"); // Not &apos; ! TRICKY!
        break;
      case '\"': // A quote?
        s.append("&quot;"); // Yadda, yadda, yadda
        break;
      default: // Anything else may pass
        s.append(c); // ... through unchanged
        break;
      }
    }
    return s.toString(); // Donenacious.
  }

  /**
   * Tell if a host name refers to the localhost. This checks the standard IPv6
   * address, IPv4 address, and host name for localhost.
   * 
   * @param host
   *          Host name to check.
   * @return True if <var>host</var> names the localhost.
   */
  public static boolean isLocalhost(String host) {
    return "0:0:0:0:0:0:0:1".equals(host) || "127.0.0.1".equals(host)
        || "localhost".equals(host);
  }

  /**
   * Do not call.
   */
  private Utility() {
    throw new IllegalStateException("This is a \u00abutility\u00bb class");
  }
}
