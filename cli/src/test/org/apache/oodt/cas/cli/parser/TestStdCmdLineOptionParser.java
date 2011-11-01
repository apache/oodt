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

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getOptionInstanceByName;

//JDK imports
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.SimpleCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption.SubOption;
import org.apache.oodt.cas.cli.parser.StdCmdLineOptionParser;
import org.apache.oodt.cas.cli.util.Args;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link StdCmdLineOptionParser}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestStdCmdLineOptionParser extends TestCase {

   public void testParser() throws IOException {
      Args args = new Args(
            "--group --list one two three four --scalar one --none --group --list one --scalar one"
                  .split(" "));
      Set<CmdLineOption> options = new HashSet<CmdLineOption>();
      SimpleCmdLineOption listOption, scalarOption, noneOption;
      options.add(listOption = createSimpleOption("list", true));
      options.add(scalarOption = createSimpleOption("scalar", true));
      options.add(noneOption = createSimpleOption("none", false));
      options.add(noneOption = createGroupOption("group", new SubOption(
            listOption, true), new SubOption(scalarOption, true),
            new SubOption(noneOption, false)));
      options.add(new HelpCmdLineOption());

      // Parse args.
      StdCmdLineOptionParser parser = new StdCmdLineOptionParser();
      Set<CmdLineOptionInstance> specifiedOptions = parser.parse(args, options);

      // Check that two option instances where returned.
      assertEquals(2, specifiedOptions.size());

      // Find first and second group.
      Iterator<CmdLineOptionInstance> iter = specifiedOptions.iterator();
      CmdLineOptionInstance firstGroup = iter.next();
      CmdLineOptionInstance secondGroup = iter.next();
      if (getOptionInstanceByName("none", firstGroup.getSubOptions()) == null) {
         CmdLineOptionInstance tmpHold = firstGroup;
         firstGroup = secondGroup;
         secondGroup = tmpHold;
      }

      // verify first group's list was found and found its 4 args.
      CmdLineOptionInstance firstGroupList = getOptionInstanceByName("list",
            firstGroup.getSubOptions());
      assertNotNull(firstGroupList);
      assertEquals(Arrays.asList("one", "two", "three", "four"),
            firstGroupList.getValues());

      // verify first group's scalar was found and found its 1 args.
      CmdLineOptionInstance firstGroupScalar = getOptionInstanceByName(
            "scalar", firstGroup.getSubOptions());
      assertNotNull(firstGroupScalar);
      assertEquals(Arrays.asList("one"), firstGroupScalar.getValues());

      // verify first group's none was found and found no args.
      CmdLineOptionInstance firstGroupNone = getOptionInstanceByName("none",
            firstGroup.getSubOptions());
      assertNotNull(firstGroupNone);
      assertTrue(firstGroupNone.getValues().isEmpty());

      // verify second group's list was found and found its 1 args.
      CmdLineOptionInstance secondGroupList = getOptionInstanceByName("list",
            secondGroup.getSubOptions());
      assertNotNull(secondGroupList);
      assertEquals(Arrays.asList("one"), secondGroupList.getValues());

      // verify second group's scalar was found and found its 1 args.
      CmdLineOptionInstance secondGroupScalar = getOptionInstanceByName(
            "scalar", secondGroup.getSubOptions());
      assertNotNull(secondGroupScalar);
      assertEquals(Arrays.asList("one"), secondGroupScalar.getValues());

      // verify second group's none was not found
      CmdLineOptionInstance secondGroupNone = getOptionInstanceByName("none",
            secondGroup.getSubOptions());
      assertNull(secondGroupNone);
   }

   public void testGetOptions() throws IOException {
      Args args = new Args("--scalar one --none".split(" "));
      SimpleCmdLineOption option = createSimpleOption(
            StdCmdLineOptionParser.getOptionName(args.getAndIncrement()), true);
      CmdLineOptionInstance specifiedOption = StdCmdLineOptionParser.getOption(
            args, option);
      assertEquals(specifiedOption.getOption(), option);
      assertEquals(Arrays.asList("one"), specifiedOption.getValues());
      assertTrue(specifiedOption.getSubOptions().isEmpty());
   }

   public void testUseDefaultValues() throws IOException {
      Args args = new Args("--scalar --none".split(" "));
      SimpleCmdLineOption option = createSimpleOption(
            StdCmdLineOptionParser.getOptionName(args.getAndIncrement()), true);
      option.setDefaultArgs(Lists.newArrayList("one"));
      CmdLineOptionInstance specifiedOption = StdCmdLineOptionParser.getOption(
            args, option);
      assertEquals(specifiedOption.getOption(), option);
      assertEquals(Arrays.asList("one"), specifiedOption.getValues());
      assertTrue(specifiedOption.getSubOptions().isEmpty());
   }

   public void testGetValues() {
      Args args = new Args(
            "--list one two three four --scalar one --none".split(" "));
      assertEquals("--list", args.getAndIncrement());
      assertEquals(Arrays.asList("one", "two", "three", "four"),
            StdCmdLineOptionParser.getValues(args));
      assertEquals("--scalar", args.getAndIncrement());
      assertEquals(Arrays.asList("one"), StdCmdLineOptionParser.getValues(args));
      assertEquals("--none", args.getAndIncrement());
      assertEquals(Collections.emptyList(),
            StdCmdLineOptionParser.getValues(args));
      assertNull(args.getCurrentArg());
   }

   public void testIsOption() {
      assertTrue(StdCmdLineOptionParser.isOption("--arg"));
      assertTrue(StdCmdLineOptionParser.isOption("-arg"));
      assertFalse(StdCmdLineOptionParser.isOption("arg"));
   }

   public void testGetOptionName() {
      assertEquals("arg", StdCmdLineOptionParser.getOptionName("--arg"));
      assertEquals("arg", StdCmdLineOptionParser.getOptionName("-arg"));
      assertNull(StdCmdLineOptionParser.getOptionName("arg"));
   }

   private static GroupCmdLineOption createGroupOption(String longName,
         SubOption... subOptions) {
      GroupCmdLineOption option = new GroupCmdLineOption();
      option.setLongOption(longName);
      option.setShortOption(longName);
      option.setSubOptions(new HashSet<SubOption>(Arrays.asList(subOptions)));
      return option;
   }

   private static SimpleCmdLineOption createSimpleOption(String longName,
         boolean hasArgs) {
      SimpleCmdLineOption option = new SimpleCmdLineOption();
      option.setLongOption(longName);
      option.setShortOption(longName);
      option.setHasArgs(hasArgs);
      return option;
   }
}
