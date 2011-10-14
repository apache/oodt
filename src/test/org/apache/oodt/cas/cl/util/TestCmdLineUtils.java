package org.apache.oodt.cas.cl.util;

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
import org.apache.oodt.cas.cl.util.CmdLineUtils;

public class TestCmdLineUtils extends TestCase {

	public void testDetermineRequired() {
		CmdLineAction action = createAction("TestAction");
		ActionCmdLineOption actionOption = createActionOption("operation");

		HashSet<CmdLineOption> options = new HashSet<CmdLineOption>();
		options.add(createSimpleOption("url", createRequiredRequirementRule(action)));
		options.add(createSimpleOption("pass", false));
		options.add(createSimpleOption("user", false));
		options.add(actionOption);

		Set<CmdLineOption> requiredOptions = CmdLineUtils.determineRequired(action, options);
		assertEquals(1, requiredOptions.size());
		assertNotNull(CmdLineUtils.getOptionByName("url", requiredOptions));

		options = new HashSet<CmdLineOption>();
		options.add(createSimpleOption("url", createRequiredRequirementRule(action)));
		options.add(createSimpleOption("pass", true));
		options.add(createSimpleOption("user", false));
		options.add(actionOption);

		requiredOptions = CmdLineUtils.determineRequired(action, options);
		assertEquals(2, requiredOptions.size());
		assertNotNull(CmdLineUtils.getOptionByName("url", requiredOptions));
		assertNotNull(CmdLineUtils.getOptionByName("pass", requiredOptions));
	}

	public void testDetermineOptional() {
		CmdLineAction action = createAction("TestAction");
		ActionCmdLineOption actionOption = new ActionCmdLineOption();

		HashSet<CmdLineOption> options = new HashSet<CmdLineOption>();
		options.add(createSimpleOption("url", createRequiredRequirementRule(action)));
		options.add(createSimpleOption("pass", false));
		options.add(createSimpleOption("user", false));
		options.add(actionOption);

		Set<CmdLineOption> optionalOptions = CmdLineUtils.determineOptional(action, options);
		assertEquals(0, optionalOptions.size());

		options = new HashSet<CmdLineOption>();
		options.add(createSimpleOption("pass", true));
		options.add(createSimpleOption("user", true));
		options.add(actionOption);

		optionalOptions = CmdLineUtils.determineOptional(action, options);
		assertEquals(0, optionalOptions.size());

		options = new HashSet<CmdLineOption>();
		options.add(createSimpleOption("pass", createOptionalRequirementRule(action)));
		options.add(createSimpleOption("user", createOptionalRequirementRule(action)));
		options.add(actionOption);

		optionalOptions = CmdLineUtils.determineOptional(action, options);
		assertEquals(2, optionalOptions.size());
		assertNotNull(CmdLineUtils.getOptionByName("pass", optionalOptions));
		assertNotNull(CmdLineUtils.getOptionByName("user", optionalOptions));
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

	private static RequirementRule createOptionalRequirementRule(CmdLineAction action) {
		StdRequirementRule rule = new StdRequirementRule();
		rule.addActionDependency(new ActionDependency(action.getName(), Relation.OPTIONAL));
		return rule;
	}
}
