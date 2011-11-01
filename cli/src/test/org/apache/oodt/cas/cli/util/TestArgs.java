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
package org.apache.oodt.cas.cli.util;

//JDK imports
import java.util.Arrays;

//OODT imports
import org.apache.oodt.cas.cli.util.Args;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link Args}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestArgs extends TestCase {

   private static final String ARGS_STRING = "--operation download --url http://somewhere.com --user foo --pass bar --toDir /tmp";

   public void testIteration() {
      StringBuffer argsString = new StringBuffer("");
      Args args = createArgs();
      for (String arg : args) {
         argsString.append(arg).append(" ");
         int i = 0;
         for (String argInner : args) {
            argsString.append(argInner).append(" ");
            if (i++ > 1) {
               break;
            }
         }
         argsString.append(args.getAndIncrement()).append(" ");
      }

      assertEquals(Arrays.asList(ARGS_STRING.split(" ")),
            Arrays.asList(argsString.toString().split(" ")));
   }

   public void testIndexOutOfBoundsException() {
      Args args = new Args(new String[] {});
      try {
         args.iterator().next();
         fail("Should have thrown IndexOutOfBoundsException");
      } catch (IndexOutOfBoundsException ignore) { /* expect throw */
      }
   }

   private Args createArgs() {
      return new Args(ARGS_STRING.split(" "));
   }
}
