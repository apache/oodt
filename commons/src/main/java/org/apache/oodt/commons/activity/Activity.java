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

import org.apache.oodt.commons.net.Net;

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

  public static final int INT = 32;
  public static final int INT1 = 0xff;

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
		if (id == null) {
		  throw new IllegalArgumentException("ID required");
		}
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
		if (!started) {
		  return;
		}
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
			InetAddress addr = Net.getLocalHost();	                       // Get the local host's IP address
			long nextNum = ++counter;				       // Get the next 64 bit number
			Date date = new Date();					       // Get the current time
			byte[] bytes = new byte[INT];				       // Make space for 32 random bytes
			RANDOM.nextBytes(bytes);				       // Fill in 32 random bytes
		  MessageDigest messageDigest = MessageDigest.getInstance("MD5");// Prepare to take a hash
			messageDigest.update((String.valueOf(addr) + nextNum + date).getBytes());	       // Add the 1st 3 components
			byte[] sig = messageDigest.digest(bytes);		       // And add the random bytes
			StringBuilder output = new StringBuilder();		       // Make space to store the hash as a string
		  for (byte aSig : sig) {
			output.append(Integer.toHexString(((int) aSig) & INT1));// Store it as a hex value
		  }
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
