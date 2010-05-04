// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Activity.java,v 1.2 2005-08-03 17:20:36 kelly Exp $

package jpl.eda.activity;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

/**
 * An activity is an occurrence of some active action.  It has a unique ID in the
 * universe, a way to log incidents that occur during the course of the activity, and a
 * way to indicate that the activity has stopped.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public abstract class Activity {
	/**
	 * Creates a new {@link Activity} instance.
	 *
	 */
	protected Activity() {
		setID(generateID());
	}

	/**
	 * Change the activity's ID to the given value.
	 *
	 * @param id New ID.
	 */
	public void setID(String id) {
		if (id == null) throw new IllegalArgumentException("ID required");
		this.id = id;
	}

	/**
	 * Get the activity's ID.
	 *
	 * @return a {@link String} ID.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Stop this activity.  This method logs a <code>ActivityStopped</code> incident.  No
	 * further incidents may be logged after calling this method.
	 */
	public synchronized void stop() {
		if (!started) return;
		this.started = false;
		log(new ActivityStopped());
	}

	/**
	 * Log the given incident.
	 *
	 * @param incident The incident to log.
	 */
	public final synchronized void log(Incident incident) {
		incident.setActivityID(id);
		recordIncident(incident);
	}

	/**
	 * Record the given incident.
	 *
	 * @param incident an {@link Incident} value.
	 */
	protected abstract void recordIncident(Incident incident);

	/**
	 * Generate a unique ID for the activity based on Internet address, a unique
	 * counter, the time, and some random bytes.
	 *
	 * @return a {@link String} value.
	 */
	private String generateID() {
		try {
			InetAddress addr = jpl.eda.net.Net.getLocalHost();	       // Get the local host's IP address
			long nextNum = ++counter;				       // Get the next 64 bit number
			Date date = new Date();					       // Get the current time
			byte[] bytes = new byte[32];				       // Make space for 32 random bytes
			RANDOM.nextBytes(bytes);				       // Fill in 32 random bytes
			StringBuffer input = new StringBuffer();		       // Make space to put the 1st 3 components...
			input.append(addr).append(nextNum).append(date);	       // ...together and put 'em together
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");// Prepare to take a hash
			messageDigest.update(input.toString().getBytes());	       // Add the 1st 3 components
			byte[] sig = messageDigest.digest(bytes);		       // And add the random bytes
			StringBuffer output = new StringBuffer();		       // Make space to store the hash as a string
			for (int i = 0; i < sig.length; ++i)			       // For each byte in the hash
				output.append(Integer.toHexString(((int)sig[i])&0xff));// Store it as a hex value
			return output.toString();				       // And return the string
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("MD5 algorithm not available");
		}
	}

	/** Unique ID of the activity. */
	protected String id;

	/** Flag that tells if this activity is active. */
	private boolean started = true;

	/** Random number generator for unique IDs. */
	private static final Random RANDOM = new Random();

	/** Counter for unique IDs. */
	private static long counter = 0;
}
