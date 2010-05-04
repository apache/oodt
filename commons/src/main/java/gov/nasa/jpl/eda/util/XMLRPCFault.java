// Copyright 2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: XMLRPCFault.java,v 1.1 2004-03-01 16:52:07 kelly Exp $

package jpl.eda.util;

import java.io.*;
import java.util.*;
import java.text.ParseException;
import org.w3c.dom.*;
import org.xml.sax.*;

/** XML-RPC fault.
 *
 * This exception is thrown when a fault is returned from an XML-RPC call.
 *
 * @author Kelly
 */
public class XMLRPCFault extends Exception {
	/** Constructor.
	 *
	 * @param code Fault code.
	 * @param string Fault string.
	 */
	public XMLRPCFault(int code, String string) {
		super(code + ": " + string);
		this.code = code;
		this.string = string;
	}

	/** Get the fault code.
	 *
	 * @return The fault code.
	 */
	public int getCode() {
		return code;
	}

	/** Get the fault string.
	 *
	 * @return The fault string.
	 */
	public String getString() {
		return string;
	}

	/** Fault code. */
	private int code;

	/** Fault string. */
	private String string;
}
