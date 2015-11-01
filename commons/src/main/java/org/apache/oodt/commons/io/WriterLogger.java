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

import java.io.Writer;
import java.io.IOException;
import org.apache.oodt.commons.util.*;

/** Log messages to a <code>Writer</code>.
 *
 * This class defines a {@link LogListener} that logs its messages to a character stream
 * {@link Writer}.  This formats and writes to the output stream all log events passed to
 * {@link LogListener#messageLogged}, one per line.  Each line is separated with the
 * system's line separator characters, specified by the <code>line.separator</code> system
 * property.
 *
 * <p>It ignores all events passed to {@link LogListener#streamStarted} and
 * {@link LogListener#streamStopped}.
 *
 * <p>It quietly hides all IO errors that may occur during writing.
 *
 * @see Log
 * @author Kelly
 */
public class WriterLogger implements LogListener {
	/** Constructor given an output stream.
	 *
	 * Construct a writer logger that writes message events to the given output stream
	 * and flushes the stream after every logged message.
	 *
	 * @param outputStream The output stream to which to write events.
	 */
	public WriterLogger(java.io.OutputStream outputStream) {
		this(new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputStream)), /*autoFlush*/ true);
	}

	/** Constructor given a writer.
	 *
	 * Construct a writer logger that writes message events to the given writer and
	 * flushes the stream after every logged message.
	 *
	 * @param writer The writer to which to write events.
	 */
	public WriterLogger(Writer writer) {
		this(writer, /*autoFlush*/ true);
	}

	/** General constructor.
	 *
	 * Construct a writer logger that writes message events to the given writer and
	 * optionally flushes the stream after every logged message.
	 *
	 * @param writer The writer to which to write events.
	 * @param autoFlush If true, call flush on the writer after every message
	 * logged. If false, don't call flush.
	 */
	public WriterLogger(Writer writer, boolean autoFlush) {
		if (writer == null) {
		  throw new IllegalArgumentException("Can't write to a null writer");
		}
		this.writer = writer;
		this.autoFlush = autoFlush;
		this.lineSep = System.getProperty("line.separator", "\n");
	}

	/** Close the writer.
	 *
	 * This closes the writer to which this logger was logging.  Future message events
	 * are ignored and not written.
	 */
	public final void close() {
		if (writer == null) {
		  return;
		}
		try {
			writer.close();
		} catch (IOException ignore) {}
		writer = null;
	}

	/** Log a message to the writer.
	 *
	 * This method first calls {@link #formatMessage} to format the message, and then
	 * writes the message to the output stream.  
	 * 
	 * @param event The event describing the message that was logged.
	 */
	public final void messageLogged(LogEvent event) {
		if (writer == null) {
		  return;
		}
		try {
			writer.write(formatMessage(event.getTimestamp(), (String) event.getSource(), event.getCategory(),
				event.getMessage()) + lineSep);
			if (autoFlush) {
			  writer.flush();
			}
		} catch (IOException ignore) {}
	}

	/** Format a message for logging.
	 *
	 * This method formats a message as follows: 
	 *
	 * <p><code><var>epochTime</var> (<var>time</var>) <var>source</var> <var>category</var>:
	 * <var>message</var> 
	 *
	 * <p>where <var>epochTime</var> is the time in milliseconds since midnight, 1st
	 * January 1970 GMT, <var>time</var> is human-readable time, <var>source</var> is
	 * the source of the message, <var>category</var> is the category under which the
	 * message was logged, and <var>message</var> is the message.
	 *
	 * <p>You can override this method and provide your own formatting.
	 *
	 * @param timestamp Timestamp for the message.
	 * @param source Source of the message.
	 * @param category Category of the message.
	 * @param message The message text.
	 */
	protected String formatMessage(java.util.Date timestamp, String source, Object category, String message) {
		return ("\n" + DateConvert.isoFormat(timestamp) + " " + source + " " + category + ": " + message);
	}

	/** Ignore the stream started event.
	 *
	 * @param event The event to ignore.
	 */
	public void streamStarted(LogEvent event) {
	}

	/** Ignore the stream stopped event.
	 *
	 * @param event The event to ignore.
	 */
	public void streamStopped(LogEvent event) {
	}

	public void propertyChange(java.beans.PropertyChangeEvent ignore) {}

	/** The writer to which we write log messages.
	 *
	 * If null, then we were closed.  This is protected so you can extend this class
	 * and log other events normally ignored.
	 */
	protected Writer writer;
	
	/** If true, flush after every message logged.
	 *
	 * This is protected so you can extend this class and log other events normally ignored.
	 */
	protected boolean autoFlush;

	/** What the line separator is.
	 *
	 * This is protected so you can extend this class and log other events normally ignored.
	 */
	protected String lineSep;
}
