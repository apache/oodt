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
package org.apache.oodt.cas.crawl.cli.option.handler;

//OODT static imports
import static org.apache.oodt.cas.crawl.util.ActionBeanProperties.getProperties;

//JDK imports
import java.util.Collections;

//OODT imports
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link CrawlerBeansPropHandler}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestCrawlerBeansPropHandler extends TestCase {

   public void testHandleOption() {
      AdvancedCmdLineOption option = new AdvancedCmdLineOption();
      option.setShortOption("t");
      option.setLongOption("test");
      option.setHasArgs(true);

      // Test 1 value specified.
      CmdLineOptionInstance instance = new CmdLineOptionInstance(
            option, Lists.newArrayList("value"));
      CrawlerBeansPropHandler handler = new CrawlerBeansPropHandler();
      handler.initialize(option);
      handler.setProperties(Lists.newArrayList("TestBean.prop"));
      handler.handleOption(null, instance);
      assertEquals(1, getProperties().size());
      assertEquals("value", getProperties().getProperty("TestBean.prop"));
      getProperties().clear();

      // Test multiple values specified.
      instance = new CmdLineOptionInstance(
            option, Lists.newArrayList("value1", "value2"));
      handler = new CrawlerBeansPropHandler();
      handler.initialize(option);
      handler.setProperties(Lists.newArrayList("TestBean.prop"));
      handler.handleOption(null, instance);
      assertEquals(2, getProperties().size());
      assertEquals("value1", getProperties().getProperty("TestBean.prop[0]"));
      assertEquals("value2", getProperties().getProperty("TestBean.prop[1]"));
      getProperties().clear();

      // Test no values specified.
      try {
         instance = new CmdLineOptionInstance(
               option, Collections.<String>emptyList());
         handler = new CrawlerBeansPropHandler();
         handler.initialize(option);
         handler.setProperties(Lists.newArrayList("TestBean.prop"));
         handler.handleOption(null, instance);
         fail("Should have thrown RuntimeException");
      } catch (RuntimeException e) { /* expect throw */ }
   }
}
