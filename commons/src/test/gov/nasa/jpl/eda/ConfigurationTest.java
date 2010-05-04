// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: ConfigurationTest.java,v 1.3 2004-06-14 15:47:28 kelly Exp $

package jpl.eda;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/** Unit test the {@link Configuration} class.
 *
 * @author Kelly
 */ 
public class ConfigurationTest extends TestCase {
	/** Construct the test case for the {@link Configuration} class. */
	public ConfigurationTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		// Create a temporary test configuration file.
		tmpFile = File.createTempFile("conf", ".xml");
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
		byte[] doc = TEST_DOC.getBytes();
		out.write(doc, 0, doc.length);
		out.close();
		oldValue = System.getProperty(Configuration.ENTITY_DIRS_PROP);
		System.setProperty(Configuration.ENTITY_DIRS_PROP, "/dir/1,/dir/2");
	}

	protected void tearDown() throws Exception {
		// Nuke the temporary test configuration file.
		tmpFile.delete();
		if (oldValue == null)
			System.getProperties().remove(Configuration.ENTITY_DIRS_PROP);
		else
			System.setProperty(Configuration.ENTITY_DIRS_PROP, oldValue);
	}

	/** Test the various property methods. */
	public void testConfiguration() throws IOException, SAXException, MalformedURLException {
		Configuration c = new Configuration(tmpFile.toURL());
		Properties props = new Properties();
		props.setProperty("globalKey1", "preset-value");
		c.mergeProperties(props);
		assertEquals("preset-value", props.getProperty("globalKey1"));
		assertEquals("globalValue2", props.getProperty("globalKey2"));
		assertEquals("http://testhost.test.domain:12345", c.getWebServerBaseURL());
		assertEquals("http://testhost.test.domain:12345/ns.ior", c.getNameServerURL());
		assertEquals(42, c.getNameServerStateFrequency());
		assertEquals(4, c.getEntityRefDirs().size());
		assertTrue(c.getEntityRefDirs().contains("/dir/one"));
		assertTrue(c.getEntityRefDirs().contains("/dir/two"));
		assertTrue(c.getEntityRefDirs().contains("/dir/1"));
		assertTrue(c.getEntityRefDirs().contains("/dir/2"));
		assertEquals("/dir/1,/dir/2,/dir/one,/dir/two", System.getProperty(Configuration.ENTITY_DIRS_PROP));
		Collection servers = c.getExecServerConfigs();
		assertEquals(2, servers.size());
		for (Iterator each = servers.iterator(); each.hasNext();) {
			ExecServerConfig esc = (ExecServerConfig) each.next();
			if (esc.getClassName().equals("test.Class1")) {
				assertEquals("Name1", esc.getObjectKey());
				assertEquals(1, esc.getProperties().size());
			} else if (esc.getClassName().equals("test.Class2")) {
				assertEquals("Name2", esc.getObjectKey());
				assertEquals(3, esc.getProperties().size());
				for (Iterator i = esc.getProperties().entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					if (entry.getKey().equals("localKey1"))
						assertEquals("localKey2", entry.getValue());
					else if (entry.getKey().equals("globalKey2"))
						assertEquals("local-override", entry.getValue());
					else if (entry.getKey().equals("jpl.eda.Configuration.url"))
						; // This one's OK.
					else fail("Unknown local property \"" + entry.getKey() + "\" in exec server");
				}
			} else fail("Unknown ExecServerConfig \"" + esc.getClassName() + "\" in servers from Configuration");
		}
	}

	/** The temporary test configuration file. */
	private File tmpFile;

	/** Old value of the {@link Configuration#ENTITY_DIRS_PROP} system property. */
	public String oldValue;

	private static final String TEST_DOC = "<?xml version=\"1.0\"?>\n<!DOCTYPE configuration PUBLIC \"-//JPL//DTD EDA Configuration 1.0//EN\" \"http://enterprise.jpl.nasa.gov/dtd/configuration.dtd\">\n<configuration><webServer><host>testhost.test.domain</host><port>12345</port></webServer><nameServer stateFrequency=\"42\"><iiop><version>1.2</version><host>testhost.test.domain</host><port>12345</port><objectKey>TestService</objectKey></iiop></nameServer><xml><entityRef><dir>/dir/one</dir><dir>/dir/two</dir></entityRef></xml><properties><key>globalKey1</key><value>globalValue1</value><key>globalKey2</key><value>globalValue2</value></properties><programs><execServer><class>test.Class1</class><objectKey>Name1</objectKey></execServer><execServer><class>test.Class2</class><objectKey>Name2</objectKey><properties><key>localKey1</key><value>localKey2</value><key>globalKey2</key><value>local-override</value></properties></execServer></programs></configuration>";
}
