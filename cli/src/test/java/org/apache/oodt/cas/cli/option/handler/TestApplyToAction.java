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

//JUnit imports
import org.apache.oodt.cas.cli.option.handler.ApplyToAction;

import junit.framework.TestCase;

/**
 * Test class for {@link ApplyToAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestApplyToAction extends TestCase {

   public void testVariableSetters() {
      ApplyToAction applyToAction = new ApplyToAction();
      assertNull(applyToAction.getActionName());
      assertNull(applyToAction.getMethodName());

      String actionName = "message";
      applyToAction.setActionName(actionName);
      assertEquals(actionName, applyToAction.getActionName());

      String methodName = "setMessage";
      applyToAction.setMethodName(methodName);
      assertEquals(methodName, applyToAction.getMethodName());
   }
}
