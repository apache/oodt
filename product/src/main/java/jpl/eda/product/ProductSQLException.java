// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductSQLException.java,v 1.1.1.1 2004-03-02 19:45:40 kelly Exp $

package jpl.eda.product;

import java.sql.SQLException;

/**
 * Checked exception to indicate a product fault related to SQL.
 *
 * @author Kelly
 */
public class ProductSQLException extends ProductException {
	/**
	 * Creates a new <code>ProductException</code> instance.
	 *
	 * @param cause a <code>Throwable</code> value.
	 */
	public ProductSQLException(SQLException cause) {
		super(cause);
	}
}
