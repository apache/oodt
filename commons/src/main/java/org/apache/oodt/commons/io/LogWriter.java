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

package org.apache.oodt.commons.io;

import java.util.Date;

/** A writer for log messages.
 *
 * Objects of this class let you write messages into the logging facility. You get these
 * objects by calling one of the <code>get</code> methods of class {@link Log} and use it
 * as you would use a {@link java.io.PrintWriter}. This class automatically flushes a message to
 * the logging facility whenever you call one of the <code>println</code> methods, or
 * {@link #flush}. It prints all values and objects using the platform's default character
 * encoding.
 *
 * <p>Note that the <code>println</code> methods of this class don't actually write
 * <em>any</em> line separation characters into the log.  Log listeners will want the
 * messages without such characters anyway, so this is the correct behavior.
 *
 * @see Log
 * @author Kelly
 */
public class LogWriter extends java.io.Writer {

  public static final int CAPACITY = 80;

  /** Constructor.
	 *
	 * @param timestamp The time for messages logged with the returned writer.
	 * @param source The source of the log message.
	 * @param category The messages' category.
	 */
	LogWriter(Date timestamp, String source, Object category) {
		buf = new StringBuffer(/*length*/  CAPACITY);
		lock = buf;
		this.timestamp = timestamp;
		this.source = source;
		this.category = category;
		flushed = false;
	}

	/** Write a single character.
	 *
	 * This writes a single character (the low order 16 bits of c) to the log
	 * writer. The character isn't flushed to the logging facility until you call one
	 * of the <code>println</code> methods or {@link #flush}.
	 *
	 * @param c The character to write.
	 */
	public void write(int c) {
		if (buf == null) {
		  return;
		}
		buf.append((char) c);
	}

	/** Write a portion of an array of characters.
	 *
	 * This writes the given character array starting at offset and going for length bytes.
	 *
	 * @param array The character array to write.
	 * @param offset Where in the array to get characters to write.
	 * @param length How many characters to write.
	 */
	public void write(char[] array, int offset, int length) {
		if (buf == null || length == 0) {
		  return;
		}
		if (offset < 0 || offset > array.length || length < 0 || (offset+length) > array.length || (offset+length) < 0) {
		  throw new IndexOutOfBoundsException("Can't write " + length + " characters at " + offset
											  + " from array whose length is " + array.length);
		}
		buf.append(array, offset, length);
	}

	/** Write an array of characters.
	 *
	 * This writes the entire given array.
	 *
	 * @param array Array of characters to write.
	 */
	public void write(char[] array) {
		write(array, 0, array.length);
	}

	/** Write a portion of a string.
	 *
	 * This writes length characters from the given string, starting offset characters
	 * into it.
	 *
	 * @param string The string to write.
	 * @param offset Where in the string to get characters to write.
	 * @param length How many characters to write.
	 */
	public void write(String string, int offset, int length) {
		if (buf == null || length == 0) {
		  return;
		}
		buf.append(string.substring(offset, offset + length));
	}

	/** Write a string.
	 *
	 * This writes the entire given string.
	 *
	 * @param string String to write.
	 */
	public void write(String string) {
		write(string, 0, string.length());
	}

	/** Flush the log writer.
	 *
	 * This sends any text sent to the writer on its way to the logging facility, and beyond.
	 */
	public void flush() {
		if (buf == null) {
		  return;
		}
		Log.logMessage(timestamp, source, category, buf.toString());
		buf.setLength(0);
		flushed = true;
	}

	/** Close the log writer.
	 *
	 * <p>This flushes any remaining text to the logging facility and then shuts down
	 * the log writer. You can't use it again after that (but closing a previously
	 * closed log writer is OK).
	 */
	public void close() {
		flush();
		buf = null;
	}

	/** Print a boolean value.
	 *
	 * This prints a boolean value ("true" or "false") into the log.
	 *
	 * @param b The <code>boolean</code> to print.
	 */
	public void print(boolean b) {
		write(b? "true" : "false");
	}

	/** Print a character.
	 *
	 * The character is translated into one or more bytes according to the platform's
	 * default character encoding.
	 *
	 * @param c The <code>char</code> to print.
	 */
	public void print(char c) {
		write(String.valueOf(c));
	}

	/** Print an integer.
	 *
	 * @param i The <code>int</code> to print.
	 */
	public void print(int i) {
		write(String.valueOf(i));
	}

	/** Print a long integer.
	 *
	 * @param l The <code>long</code> to print.
	 */
	public void print(long l) {
		write(String.valueOf(l));
	}

	/** Print a floating-point number.
	 *
	 * @param f The <code>float</code> to print.
	 */
	public void print(float f) {
		write(String.valueOf(f));
	}

	/** Print a double-precision floating-point number.
	 *
	 * @param d The <code>double</code> to print.
	 */
	public void print(double d) {
		write(String.valueOf(d));
	}

	/** Print an array of characters.
	 *
	 * @param a The array of chars to print.
	 */
	public void print(char[] a) {
		write(a);
	}

	/** Print a string.
	 *
	 * If the argument is <code>null</code> then the string
	 * "null" is printed.
	 *
	 * @param s The <code>String</code> to print.
	 */
	public void print(String s) {
		if (s == null) {
		  s = "null";
		}
		write(s);
	}

	/** Print an object.
	 *
	 * @param obj The <code>Object</code> to print.
	 */
	public void print(Object obj) {
		write(String.valueOf(obj));
	}

	/** Print a boolean value and terminate the message.
	 *
	 * This prints a boolean into the log, and flushes the message to the logging
	 * facility.
	 *
	 * @param b The <code>boolean</code> to print.
	 */
	public void println(boolean b) {
		print(b);
		println();
	}

	/** Print a character value and terminate the message.
	 *
	 * This prints a character into the log, and flushes the message to the logging
	 * facility.
	 *
	 * @param c The <code>char</code> to print.
	 */
	public void println(char c) {
		print(c);
		println();
	}

	/** Print an integer value and terminate the message.
	 *
	 * This prints an integer into the log, and flushes the message to the logging
	 * facility.
	 *
	 * @param i The <code>int</code> to print.
	 */
	public void println(int i) {
		print(i);
		println();
	}

	/** Print a long integer value and terminate the message.
	 *
	 * This prints a long integer into the log, and flushes the message to the logging
	 * facility.
	 *
	 * @param l The <code>long</code> to print.
	 */
	public void println(long l) {
		print(l);
		println();
	}

	/** Print a floating-point value and terminate the message.
	 *
	 * This prints a floating-point value into the log, and flushes the message to the
	 * logging facility.
	 *
	 * @param f The <code>float</code> to print.
	 */
	public void println(float f) {
		print(f);
		println();
	}

	/** Print a double-precision floating-point value and terminate the message.
	 *
	 * This prints a double-precision floating-point into the log, and flushes the
	 * message to the logging facility.
	 *
	 * @param d The <code>double</code> to print.
	 */
	public void println(double d) {
		print(d);
		println();
	}

	/** Print a character array and terminate the message.
	 *
	 * This prints a character array into the log, and flushes the message to the
	 * logging facility.
	 *
	 * @param a The array of chars to print.
	 */
	public void println(char[] a) {
		print(a);
		println();
	}

	/** Print a String and terminate the message.
	 *
	 * This prints a String into the log, and flushes the message to the logging
	 * facility.
	 *
	 * @param s The <code>String</code> to print.
	 */
	public void println(String s) {
		print(s);
		println();
	}

	public void println(Throwable t) {
		if (t == null) {
		  println("Null throwable");
		} else {
			StackTraceElement[] frames = t.getStackTrace();
				println(t.getClass().getName() + ":");
			  for (StackTraceElement frame : frames) {
				println(frame);
			  }

		}
	}

	/** Print an Object and terminate the message.
	 *
	 * This prints an Object into the log, and flushes the message to the logging
	 * facility.
	 *
	 * @param obj The <code>Object</code> to print.
	 */
	public void println(Object obj) {
		print(obj);
		println();
	}

	/** Terminate the current message.
	 *
	 * This terminates any message text built up and sends it to the logging facility.
	 */
	public void println() {
		flush();
	}

	/** Are we flushed yet?
	 *
	 * @return True if flushed, false otherwise.
	 */
	public boolean isFlushed() {
		return flushed;
	}

	/** The buffer used to build up the message.  If this is null, then the writer is closed.
	 */
	private StringBuffer buf;

	/** The timestamp this LogWriter will use for log messages.
	 */
	private Date timestamp;

	/** The source label.
	 */
	private String source;

	/** The category of messages generated by this writer.
	 */
	private Object category;

	/** Flushed yet? */
	private boolean flushed;
}
