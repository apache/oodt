// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: MultiServerTest.java,v 1.1 2004-04-05 16:21:49 kelly Exp $

package jpl.eda;

import junit.framework.TestCase;
import java.io.InputStream;
import java.io.IOException;
import org.xml.sax.InputSource;
import java.rmi.server.RemoteObject;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.InvocationTargetException;
import org.xml.sax.SAXException;

/**
 * Unit test the MultiServer class.
 *
 * @author Kelly
 */ 
public class MultiServerTest extends TestCase {
	/**
	 * Construct the test case for the MultiServer class.
	 *
	 * @param name Case name
	 */
	public MultiServerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		testConfig = getClass().getResourceAsStream("test-multiserver.xml");
		if (testConfig == null) throw new IOException("Cannot find `test-multiserver.xml'");
		System.setProperty("my.other.setting", "Don't override");
	}

	public void testParsing() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException,
		NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		InputSource is = new InputSource(testConfig);
		MultiServer.parseConfig(is);
		assertEquals("test.app", MultiServer.getAppName());
		assertEquals(4, MultiServer.getServers().size());

		MultiServer.Server server = (MultiServer.Server) MultiServer.getServers().get("urn:eda:rmi:Test1");
		assertEquals("jpl.eda.MultiServerTest$Svr1", server.getClassName());
		assertEquals(MultiServer.BINDING, server.getBindingBehavior());

		server = (MultiServer.Server) MultiServer.getServers().get("urn:eda:rmi:Test2");
		assertEquals("jpl.eda.MultiServerTest$Svr2", server.getClassName());
		assertEquals(MultiServer.NONBINDING, server.getBindingBehavior());

		server = (MultiServer.Server) MultiServer.getServers().get("urn:eda:rmi:Test3");
		assertEquals("jpl.eda.MultiServerTest$Svr3", server.getClassName());
		assertEquals(MultiServer.REBINDING, server.getBindingBehavior());

		MultiServer.AutobindingServer s = (MultiServer.AutobindingServer) MultiServer.getServers().get("urn:eda:rmi:Test4");
		assertEquals("jpl.eda.MultiServerTest$Svr4", s.getClassName());
		assertEquals(MultiServer.AUTO, s.getBindingBehavior());
		assertEquals(360000L, s.getPeriod());

		assertEquals("My Value", System.getProperty("my.setting"));
		assertEquals("Don't override", System.getProperty("my.other.setting"));
	}

	public void tearDown() throws Exception {
		if (testConfig != null) try {
			testConfig.close();
		} catch (IOException ignore) {}
		System.getProperties().remove("my.setting");
		System.getProperties().remove("my.other.setting");
		super.tearDown();
	}

	private InputStream testConfig;

	public static class Svr1 extends RemoteObject {
		public Svr1(ExecServer e) {}
	}
	public static class Svr2 extends RemoteObject {
		public Svr2(ExecServer e) {}
	}
	public static class Svr3 extends RemoteObject {
		public Svr3(ExecServer e) {}
	}
	public static class Svr4 extends RemoteObject {
		public Svr4(ExecServer e) {}
	}
}
