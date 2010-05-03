// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Transaction.java,v 1.1 2004-04-26 17:58:44 kelly Exp $

package jpl.eda.product;

import java.util.Date;
import jpl.eda.xmlquery.XMLQuery;

/**
 * A transaction with the product servlet.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class Transaction {
	/**
	 * Creates a new <code>Transaction</code> instance.
	 *
	 * @param serverID Identifies the server that received the query
	 * @param query The query passed in
	 * @param started When the query was started
	 * @param numMatches How many products matched
	 * @param numBytes How many bytes returned
	 * @param timeToComplete How long it took in milliseconds
	 */
	public Transaction(String serverID, XMLQuery query, Date started, int numMatches, long numBytes, long timeToComplete) {
		this.serverID = serverID;
		this.query = query;
		this.started = started;
		this.numMatches = numMatches;
		this.numBytes = numBytes;
		this.timeToComplete = timeToComplete;
	}

	public String toString() {
		return "Transaction[serverID=" + serverID + ",query=" + query.getKwdQueryString() + ",started=" + started
			+ ",numMatches=" + numMatches + ",numBytes=" + numBytes + ",ttc=" + timeToComplete + "]";
	}

	/**
	 * Get the ID of the server to which the query.
	 *
	 * @return a <code>String</code> value.
	 */
	public String getServerID() {
		return serverID;
	}

	/**
	 * Get the query passed to the server.
	 *
	 * @return a <code>XMLQuery</code> value.
	 */
	public XMLQuery getQuery() {
		return query;
	}

	/**
	 * Get the time the query was started.
	 *
	 * @return a <code>Date</code> value.
	 */
	public Date getStartTime() {
		return started;
	}

	/**
	 * Get the number of products that matched.
	 *
	 * @return an <code>int</code> value.
	 */
	public int getNumMatches() {
		return numMatches;
	}

	/**
	 * Get the number of bytes of product returned to the client.
	 *
	 * @return a <code>long</code> value.
	 */
	public long getNumBytes() {
		return numBytes;
	}

	/**
	 * Get how long it took (in milliseconds) to perform the complete transaction.
	 *
	 * @return a <code>long</code> value.
	 */
	public long getTimeToComplete() {
		return timeToComplete;
	}

	/** ID of server that received the query. */
	private String serverID;

	/** Query passed to the server. */
	private XMLQuery query;

	/** When the transaction started. */
	private Date started;

	/** Number of matching products. */
	private int numMatches;

	/** Number of bytes returned to querier. */
	private long numBytes;

	/** How long it took, in milliseconds. */
	private long timeToComplete;
}
