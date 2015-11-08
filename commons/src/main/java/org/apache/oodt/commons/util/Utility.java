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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

/** Miscellaneous utilities.
 *
 * This class contains various miscellaneous functions as static methods.
 *
 * @author Kelly
 */
public class Utility {
	/** Load properties from a resource.
	 *
	 * This loads properties from the given resource (relative to the given class)
	 * into the given properties object.  If any error occurs, this method prints a
	 * message to stderr but otherwise takes no special action.
	 *
	 * <p>You typically use this to merge properties for your application into the
	 * System properties by calling this method as follows:
	 * <pre>Utility.loadProperties(System.getProperties(), MyClass.class,
	 * "MyClass.properties");</pre> This adds the properties from the file
	 * <code>MyClass.properties</code> (which must exist in the same directory as the
	 * file <code>MyClass.class</code>, whether in the filesystem or in a jar) into
	 * the system properties.  You can then fetch these properties from anywhere in
	 * your program with <code>System.getProperty(String)</code>,
	 * <code>Integer.getInteger</code>, etc.
	 *
	 * <p>You can also use the method to load properties into a newly created
	 * properties object that you provide.  This keeps your properties separate, but
	 * then you have to pass a properties object all over the place, and you can't
	 * take advantage of non-String properties through methods like
	 * <code>Long#getLong(String)</code>, etc.
	 *
	 * @param props The properties object to load.
	 * @param clazz The class used to locate the resource.
	 * @param resourceName The name of the resource.
	 */
	public static void loadProperties(Properties props, final Class clazz, final String resourceName) {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(clazz.getResourceAsStream(resourceName));
			props.load(in);
		} catch (IOException ex) {
			System.err.println("I/O exception while loading \"" + resourceName + "\": " + ex.getMessage());
		} finally {
			if (in != null) {
			  try {
				in.close();
			  } catch (IOException ignore) {
			  }
			}
		}
	}

	/** Parse a list.
	 *
	 * This yields an iterator created from a list where commas (and optional leading
	 * and trailing whitespace) separate each element.  Each element will occur only
	 * once in the iterator regardless of how many times it appeared in the string.
	 *
	 * @param list The list to parse.
	 * @return The iterator over unique elements in the <var>list</var>.
	 */
	public static Iterator parseCommaList(final String list) {
		if (list == null) {
		  return new Iterator() {
			public boolean hasNext() {
			  return false;
			}

			public Object next() {
			  throw new java.util.NoSuchElementException("There weren't ANY elements in this iterator, ever");
			}

			public void remove() {
			  throw new UnsupportedOperationException("Can't remove elements from this iterator");
			}
		  };
		}
		HashSet set = new HashSet();
		StringTokenizer tokens = new StringTokenizer(list, ",");
		while (tokens.hasMoreTokens()) {
		  set.add(tokens.nextToken().trim());
		}
		return set.iterator();
	}

	/** Asynchronously redirect an input stream onto an output stream.
	 *
	 * The output stream is never closed.
	 *
	 * @param in Input stream to redirect.
	 * @param out Where to redirect <var>in</var>.
	 */
	public static void redirect(final InputStream in, final OutputStream out) {
		new Thread() {
			public void run() {
				try {
					byte[] buf = new byte[1024];
					for (;;) {
						int numRead = in.read(buf);
						if (numRead == -1) {
							in.close();
							break;
						}
						out.write(buf, 0, numRead);
					}
				} catch (IOException ex) {
					try {
						in.close();
					} catch (IOException ignore) {}
				}
			}
		}.start();
	}

	/** Log statistics on memory to stderr.
	 *
	 * @param msg Text to include in log.
	 */
	public static void logMemoryStats(String msg) {
		Runtime rt = Runtime.getRuntime();
		System.err.println(msg + ": total=" + (rt.totalMemory() / 1024) + "KB, free=" + (rt.freeMemory() / 1024) + "KB");
	}

	/** Check string s is numeric.  Numeric strings include 5.7, -3.9, 14.981E16, etc.
         *
         * @param s Text to be checked.
	 * @return True is <var>s</var> is a numeric string.
         */
	public static boolean isNumeric(String s) {
                try {
                        Double.parseDouble(s);
                } catch (NumberFormatException ex) {
                         return false;
                }
                return true;
        }

	/**
	 * Delete a file or directory.  If <var>file</var> references a directory, it
	 * recursively deletes all the directory tree.
	 *
	 * @param file File, directory, or other filesystem artifact to delete.
	 * @return True if <var>file</var> was deleted, false otherwise.
	 */
	public static boolean delete(File file) {
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
		  if (entries != null) {
			for (File entry : entries) {
              if (!delete(entry)) {
                return false;
              }
            }
		  }
		}
		return file.delete();
	}

   /**
    * This method will escape any single quotes found in the input string
    * and return the escaped string. This will ready the string for
    * insertion into a database. The single quote is escaped by inserting
    * an additional single quote in front of it in the string. If some
    * considerate developer has already escaped the single quotes in the
    * input string, this method will essentially do nothing.
    *
    * @param inputString The string to be escaped.
    * @return The escaped string.
    */
   public static String escapeSingleQuote(String inputString) {

      int index = inputString.indexOf('\'');
      if (index == -1) {
         return (inputString);
      }

      String outputString = inputString;
      while (index != -1) {

         // If the single quote is the last character in the string or 
         // the next character is not another single quote, insert a
         // single quote in front of the current single quote.
         if ((index == (outputString.length() - 1)) || (outputString.charAt(index + 1) != '\'')) {
            outputString = outputString.substring(0, index) + "'" + outputString.substring(index);
         }

         // If we are not at the end of the string, check for another
         // single quote.
         if ((index + 2) <= (outputString.length() - 1)) {
            index = outputString.indexOf('\'', index + 2);
         }
         else {
            index = -1;
         }
      }
      return (outputString);
   }
}
