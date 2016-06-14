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

/** Listener for logging events.
 *
 * Objects of classes that implement this interface are notified when messages are logged
 * and when other logging events occur.
 *
 * @see Log
 * @see LogEvent
 * @author Kelly
 */
public interface LogListener extends java.beans.PropertyChangeListener {
	/** A message got logged.
	 *
	 * The <var>event</var> contains the details of the message, including
	 * 
	 * <ul>
	 *   <li>The timestamp of the message, from {@link LogEvent#getTimestamp}.</li>
	 *   <li>The source of the message, from {@link java.util.EventObject#getSource}, which is always
	 *     a {@link String}.</li>
	 *   <li>The category of the message, from {@link LogEvent#getCategory}.</li>
	 *   <li>The message text, from {@link LogEvent#getMessage}.</li>
	 * </ul>
	 *
	 * @param event The logging event.
	 */
	void messageLogged(LogEvent event);

	/** A stream got started.
	 *
	 * The <var>event</var> contains the details of stream start, including 
	 *
	 * <ul>
	 *   <li>The name of the stream, from {@link LogEvent#getStream}.</li>
	 *   <li>The time the stream got started, from {@link LogEvent#getTimestamp}</li>
	 *   <li>The source of the new stream start, from {@link java.util.EventObject#getSource} (always a {@link String}).</li>
	 * </ul>
	 *
	 * @param event The logging event.
	 */
	void streamStarted(LogEvent event);

	/** A stream was stopped.
	 *
	 * The <var>event</var> contains the detail of the stream stop, which is the name
	 * of the stream, from {@link LogEvent#getStream} or {@link java.util.EventObject#getSource}
	 * (always a {@link String}).
	 *
	 * @param event The logging event.
	 */
	void streamStopped(LogEvent event);
}
