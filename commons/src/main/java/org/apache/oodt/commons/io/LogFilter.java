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


import java.util.HashMap;

/** A filter for log messages.
 *
 * This is a {@link LogListener} that filters out and passes through certain categories of
 * log messages to another <code>LogListener</code>.  When you construct this listener,
 * you pass a boolean flag that indicates its pass-through mode: if true, it passes log
 * messages by default and filters out specified categories; if false, it filters out
 * messages by default and passes through specified categories.
 *
 * <p>Note that this filter only affects events sent to {@link LogListener#messageLogged}.
 * Events sent to {@link LogListener#streamStarted} and {@link LogListener#streamStopped}
 * are passed through regardless.
 *
 * <p>Categories used by this filter should implement their {@link Object#hashCode} and
 * {@link Object#equals} methods.
 *
 * @see Log
 * @author Kelly
 */
public class LogFilter implements LogListener {
	/** Create a log filter.
	 *
	 * The log filter passes through and filters out messages before sending them onto
	 * the given log listener.
	 *
	 * @param listener What object will get filtered log messages.
	 * @param passThrough If true, pass messages through by default and filter out the
	 * specified <var>categories</var> of messages. If false, filter out messages by
	 * default and pass through the specified <var>categories</var>.
	 * @param categories Categories of messages to filter out (if
	 * <var>passThrough</var> is true) or to pass through (if <var>passThrough</var>
	 * is false).
	 */
	public LogFilter(LogListener listener, boolean passThrough, Object[] categories) {
		if (listener == null) {
		  throw new IllegalArgumentException("Can't filter messages to a null listener");
		}
		this.listener = listener;
		this.passThrough = passThrough;
		if (categories == null) {
		  return;
		}
	  for (Object category : categories) {
		this.categories.put(category, DUMMY);
	  }
	}

	/** Create a log filter.
	 *  
	 * The log filter passes through and filters out messages before sending them onto
	 * the given log listener. The filter starts out empty (with no categories).
	 *
	 * @param listener What object will get filtered log messages.
	 * @param passThrough If true, pass messages through by default. If false, filter out messages by default.
	 */
	public LogFilter(LogListener listener, boolean passThrough) {
		this(listener, passThrough, /*categories*/null);
	}

	/** Add an additional category.
	 *
	 * If the filter is in pass-through mode, messages in this category will be
	 * filtered out. If the filter is in filter mode, messages in this category will
	 * be passed through.
	 *
	 * @param category The category to add.
	 */
	public void addCategory(Object category) {
		categories.put(category, DUMMY);
	}

	/** Remove a category.
	 *
	 * If the category isn't in the filter, nothing happens.
	 *
	 * @param category The category to remove.
	 */
	public void removeCategory(Object category) {
		categories.remove(category);
	}

	/** Pass on the event unmodified to the registered listener.
	 *
	 * @param event The event to pass.
	 */
	public void streamStarted(LogEvent event) {
		listener.streamStarted(event);
	}

	/** Pass on the event unmodified to the registered listener.
	 *
	 * @param event The event to pass.
	 */
	public void streamStopped(LogEvent event) {
		listener.streamStopped(event);
	}

	/** Filter the event, and possibly pass it onto the registered listener.
	 *
	 * @param event The event to filter.
	 */
	public void messageLogged(LogEvent event) {
		boolean found = categories.containsKey(event.getCategory());
		if ((passThrough && !found) || (!passThrough && found)) {
		  listener.messageLogged(event);
		}
	}

	/** Ignore this event.
	 */
	public void propertyChange(java.beans.PropertyChangeEvent ignore) {}

	/** If true, pass through by default, otherwise filter out by default.
	 */
	protected boolean passThrough;

	/** Table of categories to filter/pass.
	 *
	 * This table maps all values to {@link #DUMMY}.  In Java2, we can get rid of
	 * <code>DUMMY</code> and use a {@link java.util.HashSet} instead.
	 */
	protected HashMap categories = new HashMap();

	/** The DUMMY value for all mappings in the {@link #categories} table.
	 */
	protected static final Object DUMMY = new Object();

	/** The listener on whose behalf we filter.
	 */
	protected LogListener listener;
}
