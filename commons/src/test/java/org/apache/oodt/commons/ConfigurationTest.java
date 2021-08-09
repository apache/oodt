// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons;

import java.io.*;
import java.util.*;
import junit.framework.*;

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
	public void testConfiguration() throws IOException, SAXException {
		Configuration c = new Configuration(tmpFile.toURI().toURL());
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
	  for (Object server : servers) {
		ExecServerConfig esc = (ExecServerConfig) server;
		if (esc.getClassName().equals("test.Class1")) {
		  assertEquals("Name1", esc.getObjectKey());
		  assertEquals(1, esc.getProperties().size());
		} else if (esc.getClassName().equals("test.Class2")) {
		  assertEquals("Name2", esc.getObjectKey());
		  assertEquals(3, esc.getProperties().size());
		  for (Map.Entry<Object, Object> objectObjectEntry : esc.getProperties().entrySet()) {
			Map.Entry entry = (Map.Entry) objectObjectEntry;
			if (entry.getKey().equals("localKey1")) {
			  assertEquals("localKey2", entry.getValue());
			} else if (entry.getKey().equals("globalKey2")) {
			  assertEquals("local-override", entry.getValue());
			} else if (entry.getKey().equals("org.apache.oodt.commons.Configuration.url")) {
			  ; // This one's OK.
			} else {
			  fail("Unknown local property \"" + entry.getKey() + "\" in exec server");
			}
		  }
		} else {
		  fail("Unknown ExecServerConfig \"" + esc.getClassName() + "\" in servers from Configuration");
		}
	  }
	}

	/** The temporary test configuration file. */
	private File tmpFile;

	/** Old value of the {@link Configuration#ENTITY_DIRS_PROP} system property. */
	public String oldValue;

	private static final String TEST_DOC = "<?xml version=\"1.0\"?>\n<!DOCTYPE configuration PUBLIC \"-//JPL//DTD EDA Configuration 1.0//EN\" \"http://enterprise.jpl.nasa.gov/dtd/configuration.dtd\">\n<configuration><webServer><host>testhost.test.domain</host><port>12345</port></webServer><nameServer stateFrequency=\"42\"><iiop><version>1.2</version><host>testhost.test.domain</host><port>12345</port><objectKey>TestService</objectKey></iiop></nameServer><xml><entityRef><dir>/dir/one</dir><dir>/dir/two</dir></entityRef></xml><properties><key>globalKey1</key><value>globalValue1</value><key>globalKey2</key><value>globalValue2</value></properties><programs><execServer><class>test.Class1</class><objectKey>Name1</objectKey></execServer><execServer><class>test.Class2</class><objectKey>Name2</objectKey><properties><key>localKey1</key><value>localKey2</value><key>globalKey2</key><value>local-override</value></properties></execServer></programs></configuration>";
}
