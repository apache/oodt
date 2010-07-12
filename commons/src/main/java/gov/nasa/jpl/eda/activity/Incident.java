// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Incident.java,v 1.3 2005-08-03 17:22:11 kelly Exp $

package jpl.eda.activity;

import java.io.Serializable;
import java.util.Date;

/**
 * An incident signifies some occurrence within an activity.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
public class Incident implements Serializable, Comparable {
	/**
	 * Creates a new {@link Incident} instance, timestamping it.
	 */
	public Incident() {
		time = new Date();
	}

	/**
	 * Get the timestamp of this incident.
	 *
	 * @return a {@link Date} value.
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * Get the ID of the activity to which the incident belongs.
	 *
	 * @return a {@link String} value.
	 */
	public String getActivityID() {
		return id;
	}

	/**
	 * Set the ID of the activity to which this incident belongs.
	 *
	 * @param id a {@link String} value.
	 */
	void setActivityID(String id) {
		this.id = id;
	}

	/**
	 * Two incidents are equal if they're the same object, or if they both have the
	 * equal timestamps and either both their IDs are null or are equal.
	 *
	 * @param obj an {@link Object} value.
	 * @return True if incidents are equal.
	 */
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Incident)) return false;
		Incident rhs = (Incident) obj;
		return ((id == null && rhs.id == null) || id.equals(rhs.id)) && time.equals(rhs.time);
	}

	/**
	 * Two incidents compare with each other mostly by their timestamps
	 *
	 * @param obj an {@link Object} value.
	 * @return -1 if this object is less than <var>obj</var>, 0 if they're equal,
	 * or 1 if this object is greater than <var>obj</var>
	 */
	public int compareTo(Object obj) {
		Incident rhs = (Incident) obj;
		int idComp = id == null && rhs.id != null? -1 : id == null && rhs.id == null? 0 : id != null && rhs.id == null? 1
			: id.compareTo(rhs.id);
		if (idComp == 0)
			return time.compareTo(rhs.time);
		else
			return idComp;
	}

	public String toString() {
		return getClass().getName();
	}

	public int hashCode() {
		return time.hashCode() ^ (id == null? 0xaaaaaaaa : id.hashCode());
	}

	/** Timestamp of this incident. */
	protected Date time;

	/** ID of the activity to which this incident belongs. */
	private String id;

       /** Serial version unique ID. */
       static final long serialVersionUID = -8119061795944984581L;
}
