// Copyright 1999-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductException.java,v 1.2 2004-11-30 21:10:37 kelly Exp $

package jpl.eda.product;

/**
 * Checked exception to indicate a product fault.
 *
 * @author Kelly
 */
public class ProductException extends Exception {
	/**
	 * Construct a product exception with no detail message.
	 */
	public ProductException() {}

	/**
	 * Construct a product exception with the given detail message.
	 *
	 * @param msg Detail message.
	 */
	public ProductException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new <code>ProductException</code> instance.
	 *
	 * @param cause a <code>Throwable</code> value.
	 */
	public ProductException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new <code>ProductException</code> instance.
	 *
	 * @param msg a <code>String</code> value.
	 * @param cause a <code>Throwable</code> value.
	 */
	public ProductException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = 8240102969482071451L;
}
