// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Main.java,v 1.2 2004-06-14 15:43:01 kelly Exp $

package jpl.eda.console;

import java.io.*;
import java.util.*;
import jpl.eda.*;
import org.xml.sax.*;
import java.util.Iterator;

/** Start the EDA software.
 *
 * This executes the EDA software in console (text) mode.
 *
 * @author Kelly
 */
final public class Main {
	/** Start the EDA software.
	 *
	 * @param argv The command-line arguments, which are ignored.
	 */
	public static void main(String[] argv) {
		// Enable support of our special URLs, like stdin:
		System.setProperty("java.protocol.handler.pkgs", "jpl.eda.net.protocol");

		// Lawyers:
		System.err.println(JPL_LEGAL_BOILERPLATE);

		// Start the exec-servers specified in the configuration.
		Configuration configuration = getStandardConsoleConfiguration();
		final List executables = new ArrayList();
		for (Iterator i = configuration.getExecServerConfigs().iterator(); i.hasNext();) {
			final Executable exec = (Executable) i.next();
			executables.add(exec);
			exec.execute();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				for (Iterator i = executables.iterator(); i.hasNext();) {
					Executable exec = (Executable) i.next();
					exec.terminate();
				}
			}
		});

		for (;;) try {
			Thread.currentThread().join();
		} catch (InterruptedException ignore) {}
	}

	/** Perform a standard setup of a console-mode application and return the EDA
	 * Configuration object.
	 *
	 * This includes loading the EDA Configuration, creating it if it doesn't exist,
	 * and letting the user know if it had to be created.  If the configuration
	 * doesn't exist and had to be created or if an error occurred during parsing or
	 * reading of the configuration, this method displays appropriate error messages
	 * to stderr and exits.
	 *
	 * @return The configuration.
	 */
	public static Configuration getStandardConsoleConfiguration() {
		Configuration configuration = null;
		try {
			configuration = Configuration.getConfiguration();
			configuration.mergeProperties(System.getProperties());
		} catch (IOException ex) {
			System.err.println("I/O error reading the configuration file: " + ex.getMessage());
			System.exit(1);
		} catch (SAXParseException ex) {
			ex.printStackTrace();
			System.err.println("ERROR! in the configuration file at line " + ex.getLineNumber() + ", column "
				+ ex.getColumnNumber() + ": " + ex.getMessage());
			System.exit(1);
		} catch (SAXException ex) {
			System.err.println("Error while parsing the configuration file: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
		return configuration;
	}

	/** Prevent instantiation.
	 */
	private Main() {
		throw new UnsupportedOperationException("Don't construct objects of class " + getClass().getName());
	}

	/** JPL legal boilerplate. */
	public static final String JPL_LEGAL_BOILERPLATE = "Copyright 1998-2002, California Institute of Technology."
		+ "  ALL RIGHTS RESERVED.  U.S. Government Sponsorship acknowledged.";
}
