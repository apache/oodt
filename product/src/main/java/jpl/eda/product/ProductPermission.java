// Copyright 1999-2004 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductPermission.java,v 1.1 2004-08-13 21:27:39 kelly Exp $

package jpl.eda.product;

import java.security.BasicPermission;

/**
 * Permission to use a product server.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class ProductPermission extends BasicPermission {
	/**
	 * Creates a new {@link ProductPermission} instance.
	 *
	 * @param name Name of the product feature to use.
	 */
	public ProductPermission(String name) {
		super(name);
	}

	/**
	 * Creates a new {@link ProductPermission} instance.
	 *
	 * @param name Name of the product feature to use.
	 * @param actions Actions to be performed on the feature.
	 */
	public ProductPermission(String name, String actions) {
		super(name, actions);
	}
}
