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
package org.apache.oodt.cas.cli.parser;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineParserException;
import org.apache.oodt.cas.cli.util.ParsedArg;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link StdCmdLineOptionParser}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestStdCmdLineOptionParser extends TestCase {

   public void testParser() throws CmdLineParserException {
      String[] args =
            "--group --list one two three four --scalar one --none --group --list one --scalar one"
                  .split(" ");

      // Parse args.
      StdCmdLineParser parser = new StdCmdLineParser();
      List<ParsedArg> parsedArgs = parser.parse(args);

      // Check that 14 option instances where returned.
      assertEquals(14, parsedArgs.size());

      // Check that all args where found and assigned the appropriate type.
      assertEquals("group", parsedArgs.get(0).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(0).getType());
      assertEquals("list", parsedArgs.get(1).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(1).getType());
      assertEquals("one", parsedArgs.get(2).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(2).getType());
      assertEquals("two", parsedArgs.get(3).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(3).getType());      
      assertEquals("three", parsedArgs.get(4).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(4).getType());
      assertEquals("four", parsedArgs.get(5).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(5).getType());
      assertEquals("scalar", parsedArgs.get(6).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(6).getType());
      assertEquals("one", parsedArgs.get(7).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(7).getType());
      assertEquals("none", parsedArgs.get(8).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(8).getType());
      assertEquals("group", parsedArgs.get(9).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(9).getType());
      assertEquals("list", parsedArgs.get(10).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(10).getType());
      assertEquals("one", parsedArgs.get(11).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(11).getType());
      assertEquals("scalar", parsedArgs.get(12).getName());
      assertEquals(ParsedArg.Type.OPTION, parsedArgs.get(12).getType());
      assertEquals("one", parsedArgs.get(13).getName());
      assertEquals(ParsedArg.Type.VALUE, parsedArgs.get(13).getType());
   }

   public void testIsOption() {
      assertTrue(StdCmdLineParser.isOption("--arg"));
      assertTrue(StdCmdLineParser.isOption("-arg"));
      assertFalse(StdCmdLineParser.isOption("arg"));
   }

   public void testGetOptionName() {
      assertEquals("arg", StdCmdLineParser.getOptionName("--arg"));
      assertEquals("arg", StdCmdLineParser.getOptionName("-arg"));
      assertNull(StdCmdLineParser.getOptionName("arg"));
   }
}
