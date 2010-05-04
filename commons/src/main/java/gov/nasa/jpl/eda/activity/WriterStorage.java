// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: WriterStorage.java,v 1.1 2004-03-02 19:28:58 kelly Exp $

package jpl.eda.activity;

import java.util.List;
import java.io.Writer;
import java.io.PrintWriter;

/**
 * Storage that sends activity reports to a {@link Writer} as plain text.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public abstract class WriterStorage implements Storage {
	/**
	 * Creates a new {@link WriterStorage} instance.
	 *
	 * @param writer Where to write activity reports.
	 */
	protected WriterStorage(Writer writer) {
		this.writer = new PrintWriter(writer, /*autoflush*/true);
	}

	/**
	 * Write a line of text for a stored activity.
	 *
	 * The format is <code><var>activityID</var>[<var>incident</var>,<var>incident</var>...]</code>.
	 *
	 * @param id a {@link String} value.
	 * @param incidents a {@link List} value.
	 */
	public void store(String id, List incidents) {
		writer.println(id + incidents);
	}

	/** Where to send the text. */
	protected PrintWriter writer;
}
