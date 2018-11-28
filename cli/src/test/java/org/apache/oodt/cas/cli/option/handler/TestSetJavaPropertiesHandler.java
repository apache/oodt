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
package org.apache.oodt.cas.cli.option.handler;

//OODT static imports
import static org.apache.oodt.cas.cli.test.util.TestUtils.createAdvancedOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionInstance;

//JDK imports
import java.util.List;

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.handler.SetJavaPropertiesHandler;

//Google imports
import com.google.common.collect.Lists;

/**
 * Test class for {@link SetJavaPropertiesHandler}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestSetJavaPropertiesHandler extends TestCase {

   public void testSettingOfProperties() {
      String property = "test.property";
      SetJavaPropertiesHandler handler = new SetJavaPropertiesHandler();
      handler.setPropertyNames(Lists.newArrayList(property));
      CmdLineOption option = createAdvancedOption("testOption", handler);
      CmdLineOptionInstance optionInstance = createOptionInstance(option, "Hello", "World");

      assertNull(System.getProperty(property));
      
      handler.handleOption(null, optionInstance);
      assertEquals("Hello World", System.getProperty(property));

      option.setType(List.class);
      handler.handleOption(null, optionInstance);
      assertEquals("Hello,World", System.getProperty(property));
   }
}
