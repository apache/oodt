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

package org.apache.oodt.cas.filemgr.structs;

//JDK imports
import java.io.File;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the Product {@link Reference} class. Currently 
 * exercises that MIME type detection properly occurs.
 * 
 * @since OODT-58
 * 
 */
public class TestReference extends TestCase {

  public TestReference() {
    System.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
        new File("./src/main/resources/mime-types.xml").getAbsolutePath());
  }

  /**
   * @since OODT-58
   */
  public void testMimeType() {
    Reference r = new Reference("file:///tmp/test.he5",
        "file:///archive/test.he5/test.he5", 0L);
    assertNotNull(r);
    assertNotNull(r.getMimeType());
    assertEquals("application/x-hdf", r.getMimeType().getName());
  }
  
  /**
   * @since OODT-136
   */
  public void testSetNullMimeType(){
    Reference r = new Reference("file:///tmp/test.he5",
        "file:///archive/test.he5/test.he5", 0L);
    
    String nullType = null;
    try{
      r.setMimeType("");
      r.setMimeType(nullType);
    }
    catch(Exception e){
      fail(e.getMessage());
    }
  }

}
