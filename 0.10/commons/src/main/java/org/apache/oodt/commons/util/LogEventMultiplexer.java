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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.oodt.commons.io.LogEvent;
import org.apache.oodt.commons.io.LogListener;

/** Multiplexer for logging events.
 *
 * @author Kelly
 */
public class LogEventMultiplexer implements LogListener {
	/** Add an event listener.
	 *
	 * @param listener Listener to add.
	 */
	public void addListener(LogListener listener) {
		listeners.add(listener);
	}

	/** Remove an event listener.
	 *
	 * @param listener Listener to remove.
	 */
	public void removeListener(LogListener listener) {
		listeners.remove(listener);
	}

	public void messageLogged(LogEvent event) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			LogListener listener = (LogListener) i.next();
			listener.messageLogged(event);
		}
	}

	public void streamStarted(LogEvent event) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			LogListener listener = (LogListener) i.next();
			listener.streamStarted(event);
		}
	}

	public void streamStopped(LogEvent event) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			LogListener listener = (LogListener) i.next();
			listener.streamStopped(event);
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			LogListener listener = (LogListener) i.next();
			listener.propertyChange(event);
		}
	}

	/** List of listeners to which I multiplex events. */
	private List listeners = new ArrayList();
}
