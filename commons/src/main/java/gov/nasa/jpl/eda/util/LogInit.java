//
// LogInit.java
//
// S. Hardman - 02/13/01
//
// This software was developed by the the Jet Propulsion Laboratory, an
// operating division of the California Institute of Technology, for the
// National Aeronautics and Space Administration, an independent agency of
// the United States Government.
// 
// This software is copyrighted (c) 2001 by the California Institute of
// Technology.  All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, is not permitted under any circumstance without prior
// written permission from the California Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// $Id: LogInit.java,v 1.1.1.1 2004-02-28 13:09:17 kelly Exp $
//

package jpl.eda.util;

import java.util.*;
import jpl.eda.io.*;
import java.beans.PropertyChangeEvent;

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
	public static void init(Properties props, String source) throws Exception {
		// Set up the log event pipeline: filter -> multiplexer -> memory
		// log/user-specified log.  First the multiplexer.
		LogEventMultiplexer mux = new LogEventMultiplexer();

		// One destination out of the multiplexer is the in-memory round-robin logger.
		mux.addListener(MEMORY_LOGGER);

		// Another destination is any user-specified logger.
		String userSpecifiedListener = props.getProperty("jpl.eda.util.LogInit.listener");
		if (userSpecifiedListener != null)
			mux.addListener((LogListener) Class.forName(userSpecifiedListener).newInstance());

		// Ahead of the multiplexer is the filter.
		String categoryList = props.getProperty("jpl.eda.util.LogInit.categories", "");
		StringTokenizer tokens = new StringTokenizer(categoryList);
		Object[] categories = new Object[tokens.countTokens()];
		for (int i = 0; i < categories.length; ++i)
			categories[i] = tokens.nextToken();
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
			if ("jpl.eda.util.LogInit.categories".equals(key)) {
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

