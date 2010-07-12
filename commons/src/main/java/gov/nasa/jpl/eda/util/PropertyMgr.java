// Copyright 2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: PropertyMgr.java,v 1.1.1.1 2004-02-28 13:09:17 kelly Exp $

package jpl.eda.util;

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
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				PropertyChangeListener listener = (PropertyChangeListener) i.next();
				listener.propertyChange(event);
			}
		}
	}

	/** Property change listeners. */
	private static List listeners = new ArrayList();
}
