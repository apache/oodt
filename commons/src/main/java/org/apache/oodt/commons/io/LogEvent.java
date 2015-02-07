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
import java.util.EventObject;

/** Logging event.
 *
 * A logging event is generated and delivered to all registered log listeners when a
 * message was logged, a logging stream was started, or when a logging stream was stopped.
 *
 * <p>Use the various query methods to determine the details of the event.  The event
 * source (from {@link EventObject#getSource}) is always a {@link String}.
 *
 * @see Log
 * @see LogListener
 * @author Kelly
 */
public class LogEvent extends EventObject {
	/** Create a "message logged" event.
	 *
	 * @param timestamp The message's timestamp.
	 * @param source The source label of the message.
	 * @param category The message's category.
	 * @param message The message.
	 */
	public LogEvent(Date timestamp, String source, Object category, String message) {
		super(source);
		type = MSG_LOGGED;
		this.timestamp = timestamp;
		this.category = category;
		this.message = message;
	}

	/** Create a "stream started" event.
	 *
	 * @param stream The name of the stream.
	 * @param timestamp The time the stream started.
	 * @param source A string identifying who or what started the stream.
	 */
	public LogEvent(String stream, Date timestamp, String source) {
		super(source);
		type = STREAM_STARTED;
		this.stream = stream;
		this.timestamp = timestamp;
	}

	/** Create a "stream stopped" event.
	 *
	 * @param stream The name of the stream.
	 */
	public LogEvent(String stream) {
		super(stream);
		type = STREAM_STOPPED;
		this.stream = stream;
	}

	/** Get the category.
	 *
	 * For message logged events, this is the category for the message. For all other
	 * events, this returns null.
	 *
	 * @return The category, or null.
	 */
	public Object getCategory() {
		return category;
	}

	/** Get the message.
	 *  
	 * For message logged events, this is the actual message text. For all other
	 * events, this returns null.
	 *
	 *@return The message, or null.
	 */
	public String getMessage() {
		return message;
	}

	/** Get the stream.
	 *
	 * For stream started and stopped events, this is the name of the stream that was
	 * started or stopped.  For message logged events, this is null.
	 *
	 * @return The stream, or null.
	 */
	public String getStream() {
		return stream;
	}

	/** Get the timestamp.
	 *
	 * For message logged and stream started events, this is the timestamp of the
	 * event.  For stream stopped events, this is null.
	 *
	 * @return The timestamp, or null.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/** Return a string representation of this event.
	 *
	 * @return A string identifying the type of the event.
	 */
	public String toString() {
		switch (type) {
			case MSG_LOGGED:
				return "Log Event (message logged)";
			case STREAM_STARTED:
				return "Log Event (stream started)";
			case STREAM_STOPPED:
				return "Log Event (stream stopped)";
			default:
				return "Unknown Log Event Type";
		}
	}

	/** The timestamp: null if this is a "stream stopped" event.
	 */
	private Date timestamp;

	/** The stream: null for logging.
	 */
	private String stream;

	/** The category: null for stream started/stopped events.
	 */
	private Object category;

	/** The message: nonnull only for "message logged" events.
	 */
	private String message;

	/** The type of event this is.
	 *
	 * This gets one of the values <code>MSG_LOGGED</code>, <code>STREAM_STARTED</code>, or <code>STREAM_STOPPED</code>.
	 */
	private int type;

	/** The "message logged" event type.
	 *
	 * @see #type
	 */
	private static final int MSG_LOGGED = 1;

	/** The "stream started" event type.
	 *
	 * @see #type
	 */
	private static final int STREAM_STARTED = 2;

	/** The "stream stopped" event type.
	 *
	 * @see #type
	 */
	private static final int STREAM_STOPPED = 3;
}
