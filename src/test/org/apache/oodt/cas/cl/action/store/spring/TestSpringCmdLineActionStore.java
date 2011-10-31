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
package org.apache.oodt.cas.cl.action.store.spring;

//OODT static imports
import static org.apache.oodt.cas.cl.util.CmdLineUtils.findAction;

//JDK imports
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//JUnit imports
import junit.framework.TestCase;

//Apache imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.action.PrintMessageAction;
import org.apache.oodt.cas.cl.test.util.TestSetContextInjectTypeAction;

//Spring imports
import org.springframework.context.ApplicationContext;

/**
 * Test case for {@link SpringCmdLineActionStore}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestSpringCmdLineActionStore extends TestCase {

   private static final String SPRING_CONFIG = "src/testdata/cmd-line-actions.xml";

   public void testActionNamesAutoSet() {
      SpringCmdLineActionStore store = new SpringCmdLineActionStore(
            SPRING_CONFIG);
      ApplicationContext appContext = store.getApplicationContext();
      @SuppressWarnings("unchecked")
      Map<String, CmdLineAction> actionsMap = appContext
            .getBeansOfType(CmdLineAction.class);
      for (Entry<String, CmdLineAction> entry : actionsMap.entrySet()) {
         assertEquals(entry.getKey(), entry.getValue().getName());
      }
   }

   public void testApplicationContextAutoSet() {
      SpringCmdLineActionStore store = new SpringCmdLineActionStore(
            SPRING_CONFIG);
      TestSetContextInjectTypeAction action = (TestSetContextInjectTypeAction) findAction(
            "TestSetContextInjectAction", store.loadSupportedActions());
      assertEquals(action.getContext(), store.getApplicationContext());
   }

   public void testLoadSupportedActions() {
      SpringCmdLineActionStore store = new SpringCmdLineActionStore(
            SPRING_CONFIG);
      Set<CmdLineAction> actions = store.loadSupportedActions();

      // Check that all actions were loaded.
      assertEquals(3, actions.size());

      // Load and verify PrintMessageAction was loaded correctly.
      CmdLineAction action = findAction("PrintMessageAction", actions);
      assertTrue(action instanceof PrintMessageAction);
      PrintMessageAction pma = (PrintMessageAction) action;
      assertEquals("Prints out a given message", pma.getDescription());
      assertNull(pma.getMessage());
      assertEquals(System.out, pma.getOutputStream());

      // Load and verify PrintHelloWorldAction was loaded correctly.
      action = findAction("PrintHelloWorldAction", actions);
      assertTrue(action instanceof PrintMessageAction);
      pma = (PrintMessageAction) action;
      assertEquals("Prints out 'Hello World'", pma.getDescription());
      assertEquals("Hello World", pma.getMessage());
      assertEquals(System.out, pma.getOutputStream());
   }
}
