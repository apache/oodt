package org.apache.oodt.cas.cl.option.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;
import org.apache.oodt.cas.cl.option.require.RequirementRule;

public class TestCmdLineOptionUtils extends TestCase {

	public void testGetOptionalOptions() {
		BasicCmdLineOption urlOption = createBasicOption("url", new RequirementRule("operation", Collections.singletonList("test"), false));
		BasicCmdLineOption operationOption = createBasicOption("operation", true); 
		HashSet<CmdLineOption<?>> options = new HashSet<CmdLineOption<?>>();
		options.add(urlOption);
		options.add(createBasicOption("pass", false));
		options.add(createBasicOption("user", false));
		options.add(operationOption);

//		specifiedOptions.add(new BasicCmdLineOptionInstance(urlOption, Collections.singletonList("http://oodt.apache.org")));

		Set<CmdLineOption<?>> optionalOptions = CmdLineOptionUtils.getConditionallyRequiredOptions(options, new BasicCmdLineOptionInstance(operationOption, Collections.singletonList("test")));
		assertEquals(1, optionalOptions.size());
		assertNotNull(CmdLineOptionUtils.getOptionByName("url", optionalOptions));
	}

	private static GroupCmdLineOption createGroupOption(String longName, boolean required) {
		GroupCmdLineOption option = new GroupCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequired(required);
		return option;
	}

	private static BasicCmdLineOption createBasicOption(String longName, RequirementRule rule) {
		BasicCmdLineOption option = new BasicCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequirementRules(Collections.singletonList(rule));
		return option;
	}

	public static BasicCmdLineOption createBasicOption(String longName, boolean required) {
		BasicCmdLineOption option = new BasicCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequired(required);
		return option;
	}
}
