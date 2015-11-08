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
		if (obj == this) {
		  return true;
		}
		if (!(obj instanceof Incident)) {
		  return false;
		}
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
		int idComp = id == null && rhs.id != null ? -1 : id == null ? 0 : rhs.id == null ? 1
			: id.compareTo(rhs.id);
		if (idComp == 0) {
		  return time.compareTo(rhs.time);
		} else {
		  return idComp;
		}
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
