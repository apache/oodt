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
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/** The log.
 *
 * This class represents the application- or applet-wide logging facility.  If your
 * application needs to log messages, here's your class.
 *
 * <p>To log a message, you call one of the <code>get</code> methods of this class to
 * yield a {@link LogWriter} object.  You can then call methods like {@link
 * LogWriter#println(String)} to log a line of text.  A typical invocation is
 *
 * <pre>Log.get().println("Buffer of length " + length + " allocated.");</pre>
 *
 * <p>This logs the given text using the default source string, and under the default
 * category, and timestamped with the current time.  You can reuse the
 * <code>LogWriter</code>, but the timestamp, source, and category won't change.  You
 * should get a fresh <code>LogWriter</code>.
 *
 * <p><strong>Sources</strong> identify independent origins of log messages, such as
 * independent threads in a program, or independent programs.  Sources are just strings.
 * If you don't specify a source, you get a default source.  You can set the default
 * source with {@link #setDefaultSource}.  You <em>always</em> get a source with every
 * message, even if it's a default source.  If you don't otherwise set a default source
 * string, the source is "app".
 *
 * <p><strong>Categories</strong> identify different classes or priorites of messages.
 * Categories can be simple strings like "warning" or "debug", or they can be complex
 * objects.  You get to define your categories.  Your group ought to agree on categories,
 * though.  If you don't specify a category, you get a default category.  You can set the
 * default category with {@link #setDefaultCategory}.  If you don't call that method, the
 * default category is the String object "message".
 *
 * <p><strong>Streams</strong> identify independent activities within a program, which
 * often have transient lifespans.  They're <em>not</em> separate output streams, but
 * instead are separate, named entities representing separate activities (although a
 * {@link LogListener} may put messages into separate output streams identified by each
 * stream).  Activity streams are identified by strings.  To indicate the start and stop
 * of streams, call {@link #startStream} and {@link #stopStream}.  These send
 * stream-started and stream-stopped events to the log listeners, who may choose to pay
 * attention to them or ignore them.  You can use streams to indicate to the log the start
 * and the stop of activities such as an analyst examining the system, or a server
 * handling a particular client.
 *
 * <p>All messages logged with this class go to one or more {@link LogListener} objects.
 * A LogListener accepts logging events, such as a message being logged, and does
 * something with it, such as writing the message to a file.  Call {@link #addLogListener}
 * to add a log listener.  Logging of a message, starting a stream, and stopping a stream
 * all get turned into {@link LogEvent}s and are multicasted to every registered listener.
 *
 * <p>The logging facility bootstraps itself with one or more log listeners specified by
 * the system property <code>org.apache.oodt.commons.io.Log.loggers</code>.  This property must be a
 * space-separated list of complete class names.  The logging facility will create an
 * object of each class listed and add it as if you had called
 * <code>addLogListener</code>.  (You can specify system properties on the command line or
 * in the applet tag.)
 *
 * @see LogListener
 * @see LogWriter
 * @author Kelly
 */
public class Log {
	/** Currently registered LogListeners.
	 */
	private static Vector listeners;

	static {
		listeners = new Vector();
		String loggers = System.getProperty("org.apache.oodt.commons.io.Log.loggers", "");
		StringTokenizer tokenizer = new StringTokenizer(loggers);
		while (tokenizer.hasMoreTokens()) {
			String className = tokenizer.nextToken();
			try {
				Class clazz = Class.forName(className);
				LogListener listener = (LogListener) clazz.newInstance();
				addLogListener(listener);
			} catch (Exception e) {
				System.err.println("Can't create log listener object from class " + className + ": " + e);
				System.exit(1);
			}
		}
	}

	/** Get a writer to log messages.
	 *
	 * The writer will log messages with the current time, default category, and
	 * default source.  Messages will go into the general log.
	 *
	 * @return A writer with which you can log messages.
	 */
	public static LogWriter get() {
		if (lastWriter != null && !lastWriter.isFlushed()) {
		  return lastWriter;
		} else {
		  return get(new Date(), getDefaultSource(), getDefaultCategory());
		}
	}

	/** Get a writer to log messages.
	 *
	 * The writer will log messages with the current time, specified category, and
	 * default source.  Messages will go into the general log.
	 *
	 * @param category The messages' category.
	 * @return A writer with which you can log messages.
	 */
	public static LogWriter get(Object category) {
		return get(new Date(), getDefaultSource(), category);
	}

	/** Get a writer to log messages.
	 *
	 * The writer will log messages with the specified time, specified category, and
	 * specified source.
	 *
	 * @param timestamp The time for messages logged with the returned writer.
	 * @param source The source of the log message.
	 * @param category The messages' category.
	 * @return A writer with which you can log messages.
	 */
	public static synchronized LogWriter get(Date timestamp, String source, Object category) {
		lastWriter = new LogWriter(timestamp, source, category);
		return lastWriter;
	}

	/** Start a new log stream.
	 *
	 * This method notifies the {@link LogListener}s that a new logging stream has
	 * started.
	 *
	 * @param stream The name of the stream.
	 * @param timestamp The time the stream started.  To use the current time, pass a new {@link Date} object.
	 * @param source A string identifying who or what started the stream.
	 */
	public static void startStream(String stream, Date timestamp, String source) {
		LogEvent event = null;
		for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
			// Lazily create the event.
			if (event == null) {
			  event = new LogEvent(stream, timestamp, source);
			}
			((LogListener) e.nextElement()).streamStarted(event);
		}
	}

	/** Stop a stream.
	 *	    
	 * This method notifies the {@link LogListener}s that a logging stream has stopped.
	 *
	 * @param stream The name of the stream that stopped.
	 */
	public static void stopStream(String stream) {
		LogEvent event = null;
		for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
			// Lazily create the event.
			if (event == null) {
			  event = new LogEvent(stream);
			}
			((LogListener) e.nextElement()).streamStopped(event);
		}
	}

	/** Log a message.
	 *
	 * The {@link LogWriter}s call this when they've built up a complete message and
	 * want it multicasted to the {@link LogListener}s.
	 *
	 * @param timestamp The message's timestamp.
	 * @param source The source label of the message.
	 * @param category The message's category.
	 * @param message The message.
	 */
	static void logMessage(Date timestamp, String source, Object category, String message) {
		LogEvent event = null;
		for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
			// Lazily create the event.
			if (event == null) {
			  event = new LogEvent(timestamp, source, category, message);
			}
			((LogListener) e.nextElement()).messageLogged(event);
		}
	}

	/** Set the default source.
	 *
	 * This sets the default source label used for logging.
	 *
	 * @param source The new default source label.
	 */
	public static void setDefaultSource(String source) {
		if (source == null) {
		  throw new IllegalArgumentException("Can't set a null default source");
		}
		defaultSource = source;
	}

	/** Get the default source.
	 *
	 * @return The default source label.
	 */
	public static String getDefaultSource() {
		return defaultSource;
	}

	/** Set the default category.
	 *
	 * This sets the category object that's used by default for logging.
	 *
	 * @param category The new default category object.
	 */
	public static void setDefaultCategory(Object category) {
		if (category == null) {
		  throw new IllegalArgumentException("Can't set a null default category");
		}
		defaultCategory = category;
	}

	/** Get the default category.
	 *
	 * @return The default category object.
	 */
	public static Object getDefaultCategory() {
		return defaultCategory;
	}
	
	/** Add a log listener.
	 *
	 * The listener will be notified whenever a message is logged, a stream started,
	 * or a stream stopped.
	 *
	 * @param listener The listener to add.
	 */
	public static void addLogListener(LogListener listener) {
		if (listener == null) {
		  throw new IllegalArgumentException("Can't add a null log listener");
		}
		listeners.addElement(listener);
	}

	/** Remove a log listener.
	 *  
	 * The listener won't receive anymore events unless it's added back.
	 *
	 * @param listener The listener to remove.
	 */
	public static void removeLogListener(LogListener listener) {
		if (listener == null) {
		  throw new IllegalArgumentException("Can't remove a null log listener");
		}
		listeners.removeElement(listener);
	}

	/** The default source label.
	 */
	private static String defaultSource = "app";

	/** The default category object.
	 */
	private static Object defaultCategory = "message";

	/** Last log writer created so it can be reused. */
	private static LogWriter lastWriter;

	/** Don't allow instantiation.
	 *
	 * When we convert to Java 2, this should throw UnsupportedOperationException.
	 */
	private Log() {}
}
