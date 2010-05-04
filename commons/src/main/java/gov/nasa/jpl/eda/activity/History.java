// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: History.java,v 1.1 2004-03-02 19:28:57 kelly Exp $

package jpl.eda.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A history of incidents to be stored in long-term storage.
 *
 * The <code>History</code> tracks Incidents that occurred during the course of an
 * activity.  It's a temporary container for incidents before the activity is deemed
 * complete and stored into whatever the long-term {@link Storage} is.  A
 * <code>History</code> starts out with at least one {@link Incident} received from
 * (usually remote) processing.  Other incidents arrive through {@link #addIncident} and
 * must have the same ID.  Eventually, one of those incidents will be an
 * <code>ActivityStopped</code> incident, which then schedules the History to commit
 * itself.
 *
 * <p>It doesn't commit itself right away though; other incidents may arrive that are part
 * of the activity out of order, so there's a delay of {@link #closeTime} milliseconds
 * before it's sent to {@link Storage}.  Other incidents will be recorded during this delay.
 *
 * <p>It's also possible that the <code>ActivityStopped</code> incident never arrives from
 * the process that deems the activity stopped.  In that case, the idle timer (which is
 * reset whenever an incident arrives) determines when to put the <code>History</code>
 * into its commital state.  The stop can arrive after this point (and will be recorded),
 * but it otherwise doesn't affect the commit-to-close time.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
final class History {
	/**
	 * Creates a new {@link History} instance.
	 *
	 * @param initialIncident Incident which gives the history its ID.
	 * @param storage Where to store the incidents when the history's done.
	 */
	History(Incident initialIncident, Storage storage) {
		id = initialIncident.getActivityID();
		this.storage = storage;
		incidents.add(initialIncident);
		scheduleExpiration();
	}

	/**
	 * Add an incident to this history.
	 *
	 * @param incident an {@link Incident} value.
	 */
	synchronized void addIncident(Incident incident) {
		if (!incident.getActivityID().equals(id))
			throw new IllegalArgumentException("Incident's activity ID " + incident.getActivityID()
				+ " doesn't match History's ID " + id);
		if (incidents == null) return;
		incidents.add(incident);
		if (expireHistoryTask != null)
			expireHistoryTask.cancel();
		if (incident instanceof ActivityStopped)
			commit();
		else if (closeHistoryTask == null)
			scheduleExpiration();
	}

	/**
	 * Commit this history by starting the commit-to-close timer.
	 */
	private void commit() {
		if (closeHistoryTask != null) return;
		TIMER.schedule(closeHistoryTask = new CloseHistoryTask(), closeTime);
	}

	/**
	 * Schedule expiration of this history due to idleness.
	 */
	private void scheduleExpiration() {
		TIMER.schedule(expireHistoryTask = new ExpireHistoryTask(), idleTime);
	}

	/**
	 * Close this history out by storing the incidents to long-term storage.
	 */
	private synchronized void close() {
 		Collections.sort(incidents);
		storage.store(id, incidents);
		incidents = null;
	}

	/** History's ID. */
	private String id;

	/** In what long-term storage to store incidents. */
	private Storage storage;

	/** Timer task that expires an idle history.  If null, this history is in commit-to-close state. */
	private ExpireHistoryTask expireHistoryTask;

	/** Timer task to close a history.  If nonnull, this history is in commit-to-close state. */ 
	private CloseHistoryTask closeHistoryTask;

	/** Incidents to store. */
	private List incidents = new ArrayList();

	/**
	 * Timer task that expires (commits and closes) an idle history.
	 */
	private class ExpireHistoryTask extends TimerTask {
		public void run() {
			synchronized (History.this) {
				// Check to see if a new expireHistoryTask got generated.
				// If it did, it means a new incident got logged while we
				// were about to expire the history.  But if the history's
				// current expiration task is this task, then we're clear
				// to commit the history, and any incidents that arrive
				// won't reset it.
				if (expireHistoryTask == this) commit();
			}
		}
	}

	/**
	 * Timer task that gives time for more incidents to arrive before closing forever a history.
	 */
	private class CloseHistoryTask extends TimerTask {
		public void run() {
			close();
		}
	}

	/** Only timer we'll ever need. */
 	private static final Timer TIMER = new Timer(/*isDaemon*/true);

	/** How many milliseconds to wait before giving up on an idle history. */
	static long idleTime = Long.getLong("jpl.eda.activity.History.idle", 5*60*1000).longValue();

	/** How many milliseconds to wait to give a history extra time to receive incidents before saving it to storage. */
	static long closeTime = Long.getLong("jpl.eda.activity.History.close", 5*60*1000).longValue();
}
