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

import org.apache.oodt.commons.io.Log;
import org.apache.oodt.commons.io.LogFilter;
import org.apache.oodt.commons.io.LogListener;

import java.beans.PropertyChangeEvent;
import java.util.Properties;
import java.util.StringTokenizer;

/**
	The <code>LogInit</code> class is intended to be used to initialize
	the logging capability implemented by the {@link Log} class.

	Maybe someday the <code>init</code> method will become part of the 
	{@link Log} class.

	@author S. Hardman
	@version $Revision: 1.1.1.1 $
	@see Log
*/
public class LogInit {

	/**
		Constructor given no arguments.

		This is a static-only class that may not be instantiated.

		@throws IllegalStateException If the class is instantiated.
	*/
	public LogInit() throws IllegalStateException {
		throw new IllegalStateException("LogInit(): Instantiation of this class is not allowed.");
	}


	/**
		Initialize the logging capability as specified in the configuration.

		@param props The system properties.
		@param source The default source to be included in messages.
		@throws Exception If the logging capability cannot be initialized.
	*/
	public static void init(Properties props, String source)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		// Set up the log event pipeline: filter -> multiplexer -> memory
		// log/user-specified log.  First the multiplexer.
		LogEventMultiplexer mux = new LogEventMultiplexer();

		// One destination out of the multiplexer is the in-memory round-robin logger.
		mux.addListener(MEMORY_LOGGER);

		// Another destination is any user-specified logger.
		String userSpecifiedListener = props.getProperty("org.apache.oodt.commons.util.LogInit.listener");
		if (userSpecifiedListener != null) {
		  mux.addListener((LogListener) Class.forName(userSpecifiedListener).newInstance());
		}

		// Ahead of the multiplexer is the filter.
		String categoryList = props.getProperty("org.apache.oodt.commons.util.LogInit.categories", "");
		StringTokenizer tokens = new StringTokenizer(categoryList);
		Object[] categories = new Object[tokens.countTokens()];
		for (int i = 0; i < categories.length; ++i) {
		  categories[i] = tokens.nextToken();
		}
		EnterpriseLogFilter filter = new EnterpriseLogFilter(mux, true, categories);
		Log.addLogListener(filter);

		// And set the source label.
		Log.setDefaultSource(source);
	}

	/** Log filter that uses strings as category objects.
	 */
	public static class EnterpriseLogFilter extends LogFilter {
		/** Construct an EnterpriseLogFilter.
		 *
		 * @param listener What object will get filtered log messages.
		 * @param passThrough If true, pass messages through by default and filter out the
		 * specified <var>categories</var> of messages. If false, filter out messages by
		 * default and pass through the specified <var>categories</var>.
		 * @param categories Categories of messages to filter out (if
		 * <var>passThrough</var> is true) or to pass through (if <var>passThrough</var>
		 * is false).
		 */
		public EnterpriseLogFilter(LogListener listener, boolean passThrough, Object[] categories) {
			super(listener, passThrough, categories);
			PropertyMgr.addPropertyChangeListener(this);
		}

		/** Update the list of categories to either pass-through or filter out.
		 *
		 * @param event Property change event.
		 */
		public void propertyChange(PropertyChangeEvent event) {
			String key = event.getPropertyName();
			if ("org.apache.oodt.commons.util.LogInit.categories".equals(key)) {
				String categoriesList = (String) event.getNewValue();
				categories.clear();
				if (categoriesList != null) {
					StringTokenizer tokens = new StringTokenizer(categoriesList);
					categories.put(tokens.nextToken(), DUMMY);
				}
				Log.get("Info").println("Enterprise log now filtering out " + (categoriesList == null?
					"all categories of messages" : "messages in categories " + categoriesList));
			}
		}
	}

	/** The single memory logger for an application. */
	public static final MemoryLogger MEMORY_LOGGER = new MemoryLogger();
}

