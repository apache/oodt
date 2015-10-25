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

import java.util.Collection;

/**
 * A composite activity multiplexes incidents to multiple other activities.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class CompositeActivity extends Activity {
	/**
	 * Creates a new {@link CompositeActivity} instance.
	 *
	 * @param activities a {@link Collection} of {@link Activity} instances.
	 */
	public CompositeActivity(Collection activities) {
		if (activities == null)
			throw new IllegalArgumentException("Activities collection required");
	  for (Object activity : activities) {
		if (!(activity instanceof Activity)) {
		  throw new IllegalArgumentException("Non-Activity in activities collection");
		}
	  }
		this.activities = activities;
	}

	/**
	 * Record the given incident in each of our delegate activities.
	 *
	 * @param incident The {@link Incident} to record.
	 */
	public void recordIncident(Incident incident) {
	  for (Object activity : activities) {
		((Activity) activity).recordIncident(incident);
	  }
	}

	/**
	 * A collection of {@link Activity} instances.
	 */
	private Collection activities;
}
