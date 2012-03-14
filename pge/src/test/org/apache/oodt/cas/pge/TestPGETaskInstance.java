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
package org.apache.oodt.cas.pge;

//OODT static imports
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetadataKeys.PROPERTY_ADDERS;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetadataKeys.PROPERTY_ADDER_CLASSPATH;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.pge.PGETaskInstance;
import org.apache.oodt.cas.pge.config.PgeConfig;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link PGETaskInstance}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestPGETaskInstance extends TestCase {

   public void testLoadPropertyAdders() throws Exception {
      PGETaskInstance pgeTask = new PGETaskInstance();
      ConfigFilePropertyAdder propAdder = pgeTask.loadPropertyAdder(MockConfigFilePropertyAdder.class.getCanonicalName());
      assertNotNull(propAdder);
      assertTrue(propAdder instanceof MockConfigFilePropertyAdder);
   }

   public void testRunPropertyAdders() throws Exception {
      PGETaskInstance pgeTask = new PGETaskInstance();
      Metadata staticMet = new Metadata();
      staticMet.addMetadata(PROPERTY_ADDER_CLASSPATH, MockConfigFilePropertyAdder.class.getCanonicalName());
      Metadata dynMet = new Metadata();
      pgeTask.pgeMetadata = new PgeMetadata(staticMet, dynMet);
      pgeTask.pgeConfig = new PgeConfig();
      pgeTask.pgeConfig.setPropertyAdderCustomArgs(new Object[] { "key", "value" });
      pgeTask.runPropertyAdders();
      assertEquals("value", pgeTask.pgeMetadata.getMetadata("key"));

      staticMet = new Metadata();
      dynMet = new Metadata();
      dynMet.addMetadata(PROPERTY_ADDERS, MockConfigFilePropertyAdder.class.getCanonicalName());
      pgeTask.pgeMetadata = new PgeMetadata(staticMet, dynMet);
      pgeTask.pgeConfig = new PgeConfig();
      pgeTask.pgeConfig.setPropertyAdderCustomArgs(new Object[] { "key", "value" });
      pgeTask.runPropertyAdders();
      assertEquals("value", pgeTask.pgeMetadata.getMetadata("key"));
      assertEquals("1", pgeTask.pgeMetadata.getMetadata(MockConfigFilePropertyAdder.RUN_COUNTER));

      staticMet = new Metadata();
      dynMet = new Metadata();
      dynMet.addMetadata(PROPERTY_ADDERS, Lists.newArrayList(MockConfigFilePropertyAdder.class.getCanonicalName(), MockConfigFilePropertyAdder.class.getCanonicalName()));
      pgeTask.pgeMetadata = new PgeMetadata(staticMet, dynMet);
      pgeTask.pgeConfig = new PgeConfig();
      pgeTask.pgeConfig.setPropertyAdderCustomArgs(new Object[] { "key", "value" });
      pgeTask.runPropertyAdders();
      assertEquals("value", pgeTask.pgeMetadata.getMetadata("key"));
      assertEquals("2", pgeTask.pgeMetadata.getMetadata(MockConfigFilePropertyAdder.RUN_COUNTER));
   }
}
