/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.commons.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Tracker of activities.  This is the main class applications use to keep track
 * activities.  To track an activity, call the {@link #createActivity} method to generate
 * a new {@link Activity}, then log {@link Incident}s against it with the {@link
 * Activity#log} method.  Finally, top the activity with {@link Activity#stop}.
 *
 * <p>The <code>ActivityTracker</code> generates activities using an {@link
 * ActivityFactory}.  It configures the factory by examining the system properties.  The
 * property <code>org.apache.oodt.commons.activity.factories</code> (or the
 * <code>activity.factories</code> property for those that prefer an abbreviated name and
 * the prior one isn't defined) is a comma- (or vertical bar-) seperated list of class
 * names.  Each class is expected to implement the {@link ActivityFactory} interface.  If
 * there are none (or neither property is defined), then a special null factory is used
 * that throws away all incidents.  If more than one's defined, then a special factory is
 * used that multiplexes all incidents to each one.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class ActivityTracker {
	/**
	 * Generate an activity.
	 *
	 * @return an new {@link Activity} instance.
	 */
	public static Activity createActivity() {
		return factory.createActivity();
	}

	/** What actually makes Activities. */
	static ActivityFactory factory;

	/**
	 * Examine the prope
	 *
	 * @throws Exception if an error occurs.
	 */
	static void initializeFactories() throws Exception {
		String facNames = System.getProperty("org.apache.oodt.commons.activity.factories", System.getProperty("activity.factories", ""));
		List factories = new ArrayList();
		for (StringTokenizer tokens = new StringTokenizer(facNames, ",|"); tokens.hasMoreTokens();) {
			String factoryName = tokens.nextToken();
			Class factoryClass = Class.forName(factoryName);
			factories.add(factoryClass.newInstance());
		}
		if (factories.isEmpty())
			factory = new NullActivityFactory();
		else if (factories.size() == 1)
			factory = (ActivityFactory) factories.get(0);
		else
			factory = new CompositeActivityFactory(factories);
	}

	/**
	 * Initialize the <code>factory</code>.
	 */
	static {
		try {
			initializeFactories();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalStateException("Cannot initialize activity factories: " + ex.getMessage());
		}
	}

	/**
	 * Special factory that creates only null activities.  Null activities toss out every incident.
	 */
	private static class NullActivityFactory implements ActivityFactory {
		/**
		 * Create null activities.
		 *
		 * @return Null activity.
		 */
		public Activity createActivity() {
			return new NullActivity();
		}
	}

	/**
	 * Special factory that multiplexes activites.  This factory takes a collection of
	 * other factories.  It generates activities that then fantail incidents to other
	 * factories' activities.
	 */
	private static class CompositeActivityFactory implements ActivityFactory {
		/**
		 * Creates a new {@link CompositeActivityFactory} instance.
		 *
		 * @param factories a {@link Collection} of ActivityFactories.
		 */
		private CompositeActivityFactory(Collection factories) {
			this.factories = factories;
		}

		/**
		 * Create an activity that fantails to other factories' activities.
		 *
		 * @return an {@link Activity} value.
		 */
		public Activity createActivity() {
			List activities = new ArrayList();
		  for (Object factory1 : factories) {
			ActivityFactory factory = (ActivityFactory) factory1;
			activities.add(factory.createActivity());
		  }
			return new CompositeActivity(activities);
		}

		/** Collection of {@link ActivityFactory} instances. */
		private Collection factories;
	}
}
