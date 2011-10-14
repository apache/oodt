package org.apache.oodt.cas.cl.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption.SubOption;
import org.apache.oodt.cas.cl.option.HelpCmdLineOption;
import org.apache.oodt.cas.cl.option.SimpleCmdLineOption;
import org.apache.oodt.cas.cl.option.util.Args;
import org.apache.oodt.cas.cl.parser.StdCmdLineOptionParser;

import junit.framework.TestCase;

public class TestStdCmdLineOptionParser extends TestCase {

	public void testParser() throws IOException {
		Args args = new Args("--group --list one two three four --scalar one --none --group --list one --scalar one".split(" "));
		Set<CmdLineOption> options = new HashSet<CmdLineOption>();
		SimpleCmdLineOption listOption, scalarOption, noneOption;
		options.add(listOption = createSimpleOption("list", true));
		options.add(scalarOption = createSimpleOption("scalar", true));
		options.add(noneOption = createSimpleOption("none", false));
		options.add(noneOption = createGroupOption("group",
				new SubOption(listOption, true),
				new SubOption(scalarOption, true),
				new SubOption(noneOption, false)));
		options.add(new HelpCmdLineOption());

		StdCmdLineOptionParser parser = new StdCmdLineOptionParser();
		Set<CmdLineOptionInstance> specifiedOptions = parser.parse(args, options);
		assertEquals(2, specifiedOptions.size());
	}

	public void testGetOptions() throws IOException {
		Args args = new Args("--scalar one --none".split(" "));
		SimpleCmdLineOption option = createSimpleOption(StdCmdLineOptionParser.getOptionName(args.getAndIncrement()), true);
		CmdLineOptionInstance specifiedOption = StdCmdLineOptionParser.getOption(args, option);
		assertEquals(specifiedOption.getOption(), option);
		assertEquals(Arrays.asList("one"), specifiedOption.getValues());
		assertTrue(specifiedOption.getSubOptions().isEmpty());
	}

	public void testGetValues() {
		Args args = new Args("--list one two three four --scalar one --none".split(" "));
		assertEquals("--list", args.getAndIncrement());
		assertEquals(Arrays.asList("one", "two", "three", "four"), StdCmdLineOptionParser.getValues(args));
		assertEquals("--scalar", args.getAndIncrement());
		assertEquals(Arrays.asList("one"), StdCmdLineOptionParser.getValues(args));		
		assertEquals("--none", args.getAndIncrement());
		assertEquals(Collections.emptyList(), StdCmdLineOptionParser.getValues(args));		
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

	private static GroupCmdLineOption createGroupOption(String longName, SubOption... subOptions) {
		GroupCmdLineOption option = new GroupCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setSubOptions(new HashSet<SubOption>(Arrays.asList(subOptions)));
		return option;
	}

	private static SimpleCmdLineOption createSimpleOption(String longName, boolean hasArgs) {
		SimpleCmdLineOption option = new SimpleCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setHasArgs(hasArgs);
		return option;
	}
}
