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

import java.util.*;
import java.beans.*;

/** Property manager manages updates to properties.
 *
 * @author Kelly
 */
public class PropertyMgr {
	/** Add a listener for property changes.
	 *
	 * @param listener Listener to add.
	 */
	public static void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	/** Remove a listener's interest in property changes.
	 *
	 * @param listener Listener to remove.
	 */
	public static void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/** Set a property and notify listeners.
	 *
	 * @param key Property's name.
	 * @param value New property's value.
	 */
	public static void setProperty(String key, String value) {
		String oldValue = System.getProperty(key);
		System.setProperty(key, value);
		if (!listeners.isEmpty()) {
			PropertyChangeEvent event = new PropertyChangeEvent(System.getProperties(), key, oldValue, value);
		  for (Object listener1 : listeners) {
			PropertyChangeListener listener = (PropertyChangeListener) listener1;
			listener.propertyChange(event);
		  }
		}
	}

	/** Property change listeners. */
	private static List listeners = new ArrayList();
}
