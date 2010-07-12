// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: EDAException.java,v 1.1.1.1 2004-02-28 13:09:12 kelly Exp $

package jpl.eda;

public class EDAException extends Exception {
	public EDAException(String msg) {
		super(msg);
	}

	public EDAException(Throwable cause) {
		super(cause);
	}

	public EDAException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

