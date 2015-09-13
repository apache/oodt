/*
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
package org.apache.oodt.cas.pge.metadata;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.pge.metadata.PgeMetadata.Type;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link PgeMetadata}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestPgeMetadata extends TestCase {

   public void testReplacePgeMetadata() {
      Metadata staticMetadata = new Metadata();
      staticMetadata.addMetadata("key1", "staticValue1");
      staticMetadata.addMetadata("key2", "staticValue2");
      staticMetadata.addMetadata("key3", "staticValue3");

      Metadata dynamicMetadata = new Metadata();
      dynamicMetadata.addMetadata("key2", "dynValue2");

      PgeMetadata pgeMet = new PgeMetadata(staticMetadata, dynamicMetadata);
      PgeMetadata pgeMet2 = new PgeMetadata(staticMetadata, dynamicMetadata);
      pgeMet2.replaceMetadata("key4", "localValue4");
      pgeMet2.linkKey("keyLink1", "key1");
      pgeMet2.markAsDynamicMetadataKey("key4");
      pgeMet.replaceMetadata(pgeMet2, "test");

      assertEquals("staticValue1", pgeMet.getMetadata("key1"));
      assertEquals("staticValue2", pgeMet.getMetadata("key2"));
      assertEquals("staticValue2",
            pgeMet.getMetadata("key2", PgeMetadata.Type.STATIC));
      assertEquals("staticValue3", pgeMet.getMetadata("key3"));
      assertNull(pgeMet.getMetadata("key4"));

      assertNull(pgeMet.getMetadata("test/key1"));
      assertNull(pgeMet.getMetadata("test/key2"));
      assertNull(pgeMet.getMetadata("test/key2", PgeMetadata.Type.STATIC));
      assertNull(pgeMet.getMetadata("test/key3"));
      assertEquals("localValue4", pgeMet.getMetadata("test/key4"));

      assertNull(pgeMet.getMetadata("keyLink1"));
      assertEquals(pgeMet.getMetadata("key1"),
            pgeMet.getMetadata("test/keyLink1"));
      assertEquals(1, pgeMet.getMarkedAsDynamicMetadataKeys().size());
      assertEquals("test/key4", pgeMet.getMarkedAsDynamicMetadataKeys()
            .iterator().next());
   }

   public void testChangingDynamicMetadata() {
      PgeMetadata pgeMet = new PgeMetadata();

      assertTrue(pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().isEmpty());
      pgeMet.replaceMetadata("key1", "value1");
      assertTrue(pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().isEmpty());
      pgeMet.markAsDynamicMetadataKey();
      assertTrue(pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().isEmpty());
      pgeMet.commitMarkedDynamicMetadataKeys();
      assertEquals(1, pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().size());
      assertEquals("value1", pgeMet.asMetadata(Type.DYNAMIC)
            .getMetadata("key1"));

      pgeMet = new PgeMetadata();

      assertTrue(pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().isEmpty());
      pgeMet.replaceMetadata("key1", "value1");
      assertTrue(pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().isEmpty());
      pgeMet.markAsDynamicMetadataKey("key1");
      assertTrue(pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().isEmpty());
      pgeMet.commitMarkedDynamicMetadataKeys("key1");
      assertEquals(1, pgeMet.asMetadata(Type.DYNAMIC).getAllKeys().size());
      assertEquals("value1", pgeMet.asMetadata(Type.DYNAMIC)
            .getMetadata("key1"));

      pgeMet.linkKey("keyLink1", "key1");
      pgeMet.replaceMetadata("keyLink1", "newValue");
      assertEquals("newValue",
            pgeMet.asMetadata(Type.DYNAMIC).getMetadata("key1"));
   }

   public void testLinking() {
      PgeMetadata pgeMet = new PgeMetadata();

      pgeMet.replaceMetadata("key1", "value1");
      pgeMet.linkKey("keyLink1", "key1");
      pgeMet.linkKey("keyLink2", "keyLink1");
      pgeMet.linkKey("keyLink3", "keyLink1");

      assertEquals("value1", pgeMet.getMetadata("key1"));
      assertEquals("value1", pgeMet.getMetadata("keyLink1"));
      assertEquals("value1", pgeMet.getMetadata("keyLink2"));
      assertEquals("value1", pgeMet.getMetadata("keyLink3"));

      pgeMet.replaceMetadata("keyLink3", "newValue");

      assertEquals("newValue", pgeMet.getMetadata("key1"));
      assertEquals("newValue", pgeMet.getMetadata("keyLink1"));
      assertEquals("newValue", pgeMet.getMetadata("keyLink2"));
      assertEquals("newValue", pgeMet.getMetadata("keyLink3"));

      assertEquals(Lists.newArrayList("keyLink1", "key1"),
            pgeMet.getReferenceKeyPath("keyLink2"));
      assertEquals(Lists.newArrayList("keyLink1", "key1"),
            pgeMet.getReferenceKeyPath("keyLink3"));

      pgeMet.replaceMetadata("key1", "value1");

      assertEquals("value1", pgeMet.getMetadata("key1"));
      assertEquals("value1", pgeMet.getMetadata("keyLink1"));
      assertEquals("value1", pgeMet.getMetadata("keyLink2"));
      assertEquals("value1", pgeMet.getMetadata("keyLink3"));

      pgeMet.unlinkKey("keyLink1");

      assertEquals("value1", pgeMet.getMetadata("key1"));
      assertNull(pgeMet.getMetadata("keyLink1"));
      assertNull(pgeMet.getMetadata("keyLink2"));
      assertNull(pgeMet.getMetadata("keyLink3"));

      pgeMet.linkKey("keyLink1", "key1");

      assertEquals("value1", pgeMet.getMetadata("key1"));
      assertEquals("value1", pgeMet.getMetadata("keyLink1"));
      assertEquals("value1", pgeMet.getMetadata("keyLink2"));
      assertEquals("value1", pgeMet.getMetadata("keyLink3"));

      assertEquals("key1", pgeMet.resolveKey("key1"));
      assertEquals("key1", pgeMet.resolveKey("keyLink1"));
      assertEquals("key1", pgeMet.resolveKey("keyLink2"));
      assertEquals("key1", pgeMet.resolveKey("keyLink3"));
      assertEquals("keyLink4", pgeMet.resolveKey("keyLink4"));
   }
}
