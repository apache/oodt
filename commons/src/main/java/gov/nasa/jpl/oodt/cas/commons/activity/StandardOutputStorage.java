// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: StandardOutputStorage.java,v 1.1 2004-03-02 19:28:58 kelly Exp $

package jpl.eda.activity;

import java.io.OutputStreamWriter;

/**
 * Storage that sends activity reports to the standard output as plain text.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class StandardOutputStorage extends WriterStorage {
	/**
	 * Creates a new {@link StandardOutputStorage} instance.
	 */
	public StandardOutputStorage() {
		super(new OutputStreamWriter(System.out));
	}
}
