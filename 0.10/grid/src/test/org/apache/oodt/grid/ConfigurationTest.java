/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.grid;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 * Test the {@link Configuration} class.
 * 
 */
public class ConfigurationTest extends TestCase {
  /**
   * Creates a new <code>ConfigurationTest</code> instance.
   * 
   * @param caseName
   *          Test case name.
   */
  public ConfigurationTest(String caseName) {
    super(caseName);
  }

  /**
   * Set up by creating a temporary config file.
   * 
   * @throws Exception
   *           if an error occurs.
   */
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
        "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
    System.setProperty("javax.xml.parsers.SAXParserFactory",
        "org.apache.xerces.jaxp.SAXParserFactoryImpl");
    System.setProperty("javax.xml.transform.TransformerFactory",
        "org.apache.xalan.processor.TransformerFactoryImpl");
    configFile = File.createTempFile("config", ".xml");
    configFile.deleteOnExit();
  }

  /**
   * Tear down by deleting the temporary config file.
   * 
   * @throws Exception
   *           if an error occurs.
   */
  public void tearDown() throws Exception {
    configFile.delete();
    super.tearDown();
  }

  /**
   * Test to see if the default values of the {@link Configuration} are
   * reasonable.
   * 
   * @throws IOException
   *           if an error occurs.
   * @throws SAXException
   *           if an error occurs.
   */
  public void testDefaults() throws IOException, SAXException {
    Configuration config = new Configuration(configFile);
    assertTrue("Expect localhost not required by default", !config
        .isLocalhostRequired());
    assertTrue("Expect https not required by default", !config
        .isHTTPSrequired());
    assertTrue("Expect no product servers by default", config
        .getProductServers().isEmpty());
    assertTrue("Expect no profile servers by default", config
        .getProfileServers().isEmpty());
    assertTrue("Default password not set", Arrays.equals(
        Configuration.DEFAULT_PASSWORD, config.getPassword()));
    assertTrue("Expect no properties", config.getProperties().isEmpty());
    assertTrue("Expect no code bases", config.getCodeBases().isEmpty());
  }

  /**
   * Test to see if the mutators work.
   * 
   * @throws IOException
   *           if an error occurs.
   * @throws SAXException
   *           if an error occurs.
   */
  public void testMutators() throws IOException, SAXException {
    Configuration config = new Configuration(configFile);

    assertTrue(!config.isHTTPSrequired());
    config.setHTTPSrequired(true);
    assertTrue("Cannot set https as required", config.isHTTPSrequired());

    assertTrue(!config.isLocalhostRequired());
    config.setLocalhostRequired(true);
    assertTrue("Cannot set localhost as required", config.isLocalhostRequired());

    byte[] password = { (byte) 'x', (byte) 'y', (byte) 'z' };
    assertTrue(Arrays.equals(Configuration.DEFAULT_PASSWORD, config
        .getPassword()));
    config.setPassword(password);
    assertTrue("Cannot change password", Arrays.equals(password, config
        .getPassword()));

    try {
      config.setPassword(null);
      fail("Null password allowed");
    } catch (IllegalArgumentException good) {
    }

    // Profile/product servers in the config have no mutators to test (they
    // return references to each Set). Same with Properties and code bases.
  }

  /**
   * Test to see if XML serialization works.
   * 
   * @throws IOException
   *           if an error occurs.
   * @throws SAXException
   *           if an error occurs.
   */
  public void testSerialization() throws IOException, SAXException {
    Configuration a = new Configuration(configFile);

    byte[] password = { (byte) 'x', (byte) 'y', (byte) 'z' };
    URL prodServerURL = new URL("http://localhost/prod.jar");
    URL profServerURL = new URL("http://localhost/prof.jar");
    ProductServer prod = new ProductServer(a, "prod");
    ProfileServer prof = new ProfileServer(a, "prof");

    a.setLocalhostRequired(true);
    a.setHTTPSrequired(true);
    a.setPassword(password);
    a.getProductServers().add(prod);
    a.getProfileServers().add(prof);
    a.getProperties().setProperty("a", "b");
    a.getCodeBases().add(prodServerURL);
    a.getCodeBases().add(profServerURL);
    a.save();

    Configuration b = new Configuration(configFile);
    assertTrue("localhost state not saved", b.isLocalhostRequired());
    assertTrue("https state not saved", b.isHTTPSrequired());
    assertTrue("Password not saved", Arrays.equals(password, b.getPassword()));
    assertEquals("Product server not saved", 1, b.getProductServers().size());
    assertEquals("Product server not saved properly", prod, b
        .getProductServers().iterator().next());
    assertEquals("Profile server not saved", 1, b.getProfileServers().size());
    assertEquals("Profile server not saved properly", prof, b
        .getProfileServers().iterator().next());
    assertEquals("Properties not saved properly", "b", b.getProperties()
        .getProperty("a"));
    assertEquals("Code bases not saved properly", 2, b.getCodeBases().size());
    assertTrue(b.getCodeBases().contains(prodServerURL));
    assertTrue(b.getCodeBases().contains(profServerURL));

    assertEquals("Configuration.equals doesn't work", a, b);
  }

  /** Test config file. */
  private File configFile;
}
