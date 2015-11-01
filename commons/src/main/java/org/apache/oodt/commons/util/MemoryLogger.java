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

import org.apache.oodt.commons.io.LogEvent;
import org.apache.oodt.commons.io.LogListener;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;

/** In-memory logger.
 *
 * This logger maintains log messages in memory.
 *
 * @author Kelly
 */
public class MemoryLogger implements LogListener {
	/** Create a memory logger.
	 *
	 * The size of the message cache comes from the system property <code>org.apache.oodt.commons.util.MemoryLogger.size</code>,
	 * or {@link #DEF_SIZE} if that property isn't specified.
	 */
	public MemoryLogger() {
		this(Integer.getInteger("org.apache.oodt.commons.util.MemoryLogger.size", DEF_SIZE).intValue());
	}

	/** Create a memory logger.
	 *
	 * @param size Size of the message cache.
	 */
	public MemoryLogger(int size) {
		this.size = size;
	}

	/** Get the list of messages logged so far.
	 *
	 * This returns a list of {@link String}s in time order.
	 *
	 * @return A list of message strings.
	 */
	public List getMessages() {
		return (List) messages.clone();
	}

	/** Get the maximum size of the cache.
	 *
	 * @return The maximum size of the cache.
	 */
	public int getSize() {
		return size;
	}

	/** Set the maximum size of the cache.
	 *
	 * @param size The new maximum cache size.
	 */
	public void setSize(int size) {
		if (size < 0) {
		  throw new IllegalArgumentException("Log cache size can't be negative");
		}
		int delta = this.size - size;
		this.size = size;
		if (delta <= 0) {
		  return;
		}
		if (messages.size() < size) {
		  return;
		}
		while (delta-- > 0) {
		  messages.removeFirst();
		}
	}

	public void streamStarted(LogEvent ignore) {}
	public void streamStopped(LogEvent ignore) {}
	public void propertyChange(PropertyChangeEvent ignore) {}

	public void messageLogged(LogEvent event) {
		messages.add(DateConvert.isoFormat(event.getTimestamp()) + " " + event.getSource() + " " + event.getCategory()
			+ ": " + event.getMessage());
		if (messages.size() > size) {
		  messages.removeFirst();
		}
	}

	/** The list of messages. */
	private LinkedList messages = new LinkedList();

	/** Maximum size of message cache. */
	private int size;

	/** The default size of the message cache, 32. */
	public static final int DEF_SIZE = 32;
}
