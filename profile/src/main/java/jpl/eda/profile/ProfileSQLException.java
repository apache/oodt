// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileSQLException.java,v 1.1.1.1 2004/03/02 20:53:16 kelly Exp $

package jpl.eda.profile;

import java.sql.SQLException;

/**
 * A database-related SQL exception from a profile server.
 *
 * @author Kelly
 */
public class ProfileSQLException extends ProfileException {
	/**
	 * Create a profile SQL exception.
	 *
	 * @param cause The SQL exception that caused this profile exception.
	 */
	public ProfileSQLException(SQLException cause) {
		super(cause);
	}
}
