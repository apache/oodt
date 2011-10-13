package org.apache.oodt.cas.cl.option.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;
import org.apache.oodt.cas.cl.option.SimpleCmdLineOption;
import org.apache.oodt.cas.cl.option.require.RequirementRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cl.option.require.StdRequirementRule;
import org.apache.oodt.cas.cl.option.require.ActionDependency;

public class TestCmdLineOptionUtils extends TestCase {

	public void testDetermineOptional() {
		CmdLineAction action = createAction("TestAction");
		SimpleCmdLineOption urlOption = createSimpleOption("url", createRequiredRequirementRule(action));
		ActionCmdLineOption operationOption = createActionOption("operation"); 
		HashSet<CmdLineOption> options = new HashSet<CmdLineOption>();
		options.add(urlOption);
		options.add(createSimpleOption("pass", false));
		options.add(createSimpleOption("user", false));
		options.add(operationOption);

		Set<CmdLineOption> optionalOptions = CmdLineOptionUtils.determineOptional(action, options);
		System.out.println(optionalOptions);
		assertEquals(0, optionalOptions.size());
	}

	private static CmdLineAction createAction(String name) {
		return new CmdLineAction(name, "This is an action description") {

			@Override
			public void execute() {
				// do nothing
			}
			
		};
	}
	private static GroupCmdLineOption createGroupOption(String longName, boolean required) {
		GroupCmdLineOption option = new GroupCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequired(required);
		return option;
	}

	private static SimpleCmdLineOption createSimpleOption(String longName, boolean required) {
		SimpleCmdLineOption option = new SimpleCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequired(required);
		return option;
	}

	private static SimpleCmdLineOption createSimpleOption(String longName, RequirementRule rule) {
		SimpleCmdLineOption option = new SimpleCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequirementRules(Collections.singletonList(rule));
		return option;
	}

	public static ActionCmdLineOption createActionOption(String longName) {
		ActionCmdLineOption option = new ActionCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		return option;
	}

	private static RequirementRule createRequiredRequirementRule(CmdLineAction action) {
		StdRequirementRule rule = new StdRequirementRule();
		rule.addActionDependency(new ActionDependency(action.getName(), Relation.REQUIRED));
		return rule;
	}
}
