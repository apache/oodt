// Copyright 1999-2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: LogEventMultiplexer.java,v 1.1.1.1 2004-02-28 13:09:17 kelly Exp $

package jpl.eda.util;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jpl.eda.io.LogEvent;
import jpl.eda.io.LogListener;

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
