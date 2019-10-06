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

package org.apache.oodt.cas.metadata;

//JUnit imports
import junit.framework.TestCase;

//JDK imports
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

/**
 * Test Case Suite for the Metadata class.
 * 
 * @author mattmann
 * @author bfoster
 * 
 */
public class TestMetadata extends TestCase {

  private Metadata metadata = null;

  /**
   * <p>
   * Default Constructor
   * </p>
   */
  public TestMetadata() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    metadata = new Metadata();
  }

  public void testAddMany() {
    List<String> counting = new Vector<>();
    counting.add("1");
    counting.add("2");
    counting.add("3");
    metadata.addMetadata("ManyTest", counting);

    assertNotNull(metadata.getAllMetadata("ManyTest"));
    assertEquals(3, metadata.getAllMetadata("ManyTest").size());

    // test ordering
    assertEquals("1", (String) metadata.getAllMetadata("ManyTest").get(0));
    assertEquals("2", (String) metadata.getAllMetadata("ManyTest").get(1));
    assertEquals("3", (String) metadata.getAllMetadata("ManyTest").get(2));
  }

  public void testAddHashtable() {
    HashMap<String,Object> testHash = new HashMap<>();
    testHash.put("key1", "val1");
    testHash.put("key2", "val2");

    metadata = new Metadata();
    metadata.addMetadata("key3", "val3");
    metadata.addMetadata(testHash);

    assertNotNull(metadata.getMetadata("key1"));
    assertNotNull(metadata.getMetadata("key2"));

    assertEquals("val1", metadata.getMetadata("key1"));
    assertEquals("val2", metadata.getMetadata("key2"));

    assertNotNull(metadata.getMetadata("key3"));
    assertEquals("val3", metadata.getMetadata("key3"));
  }

  public void testReplace() {
    HashMap<String,Object> testHash = new HashMap<>();
    testHash.put("key1", "val1");
    testHash.put("key2", "val2");

    metadata = new Metadata();
    metadata.addMetadata("blah", "blah2");

    assertNotNull(metadata.getMetadata("blah"));
    assertNull(metadata.getMetadata("key1"));
    assertNull(metadata.getMetadata("key2"));

    metadata.replaceMetadata(testHash);

    assertNull(metadata.getMetadata("blah"));
    assertNotNull(metadata.getMetadata("key1"));
    assertNotNull(metadata.getMetadata("key2"));

    assertEquals("val1", metadata.getMetadata("key1"));
    assertEquals("val2", metadata.getMetadata("key2"));

    metadata.replaceMetadata("key1", "val2");
    metadata.replaceMetadata("key2", "val1");

    assertEquals("val2", metadata.getMetadata("key1"));
    assertEquals("val1", metadata.getMetadata("key2"));

    List<String> twoValues = new Vector<>();
    twoValues.add("firstVal");
    twoValues.add("secondVal");

    metadata.replaceMetadata("key1", twoValues);
    assertNotNull(metadata.getMetadata("key1"));
    assertEquals(2, metadata.getAllMetadata("key1").size());

    assertEquals("firstVal", (String) metadata.getAllMetadata("key1").get(0));
    assertEquals("secondVal", (String) metadata.getAllMetadata("key1").get(1));

  }
  
  public void testGetAllKeysWithName() {
    Metadata m1 = new Metadata();
    m1.addMetadata("Group1/key1", "val1");
    m1.addMetadata("Group1/key2", "val2");
    m1.addMetadata("Group1/sub1/key2", "val3");
    m1.addMetadata("Group2/key2", "val3");
    m1.addMetadata("Group2/key2/key3", "val3");
    m1.addMetadata("Group1/sub2/key2/key3", "val3");
    List<String> keys = m1.getAllKeysWithName("key2");
    assertEquals(keys.size(), 3);
    assertEquals(keys.get(0), "Group1/key2");
    assertEquals(keys.get(1), "Group1/sub1/key2");
    assertEquals(keys.get(2), "Group2/key2");
    keys = m1.getAllKeysWithName("key1");
    assertEquals(keys.size(), 1);
    assertEquals(keys.get(0), "Group1/key1");
  }

  public void testEquals() {
    Metadata m1 = new Metadata();
    m1.addMetadata("key1", "val1");
    m1.addMetadata("key2", "val2");
    m1.addMetadata("key2", "val3");

    Metadata m2 = new Metadata();
    m2.addMetadata("key1", "val1");
    m2.addMetadata("key2", "val2");
    m2.addMetadata("key2", "val3");

    assertEquals(m1, m2);
    assertEquals(m2, m1);

    m2.removeMetadata("key1");

    assertFalse(m1.equals(m2));
    assertFalse(m2.equals(m1));
  }

}
