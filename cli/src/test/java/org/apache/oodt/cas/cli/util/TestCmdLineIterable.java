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
import org.apache.oodt.cas.cli.exception.CmdLineParserException;
import org.apache.oodt.cas.cli.parser.StdCmdLineParser;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link Args}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestCmdLineIterable extends TestCase {

   private static final String ARGS_STRING = "--operation download --url http://somewhere.com --user foo --pass bar --toDir /tmp";

   public void testIteration() throws CmdLineParserException {
      StringBuilder argsString = new StringBuilder("");
      CmdLineIterable<ParsedArg> args = createArgs();
      for (ParsedArg arg : args) {
         if (arg.getType().equals(ParsedArg.Type.OPTION)) {
            argsString.append("--");
         }
         argsString.append(arg.getName()).append(" ");
         int i = 0;
         for (ParsedArg argInner : args) {
            if (argInner.getType().equals(ParsedArg.Type.OPTION)) {
               argsString.append("--");
            }
            argsString.append(argInner.getName()).append(" ");
            if (i++ > 1) {
               break;
            }
         }
         arg = args.incrementAndGet();
         if (arg.getType().equals(ParsedArg.Type.OPTION)) {
            argsString.append("--");
         }
         argsString.append(arg.getName()).append(" ");
      }

      assertEquals(Arrays.asList(ARGS_STRING.split(" ")),
            Arrays.asList(argsString.toString().split(" ")));
   }

   public void testIndexOutOfBoundsException() throws CmdLineParserException {
      CmdLineIterable<ParsedArg> args = new CmdLineIterable<ParsedArg>(
            new StdCmdLineParser().parse(new String[] {}));
      try {
         args.iterator().next();
         fail("Should have thrown IndexOutOfBoundsException");
      } catch (IndexOutOfBoundsException ignore) { /* expect throw */
      }
   }

   private CmdLineIterable<ParsedArg> createArgs() throws CmdLineParserException {
      return new CmdLineIterable<ParsedArg>(
            new StdCmdLineParser().parse(ARGS_STRING.split(" ")));
   }
}
