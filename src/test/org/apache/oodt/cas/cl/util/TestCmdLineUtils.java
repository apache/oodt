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
package org.apache.oodt.cas.cl.util;

//JDK imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.ActionCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.GroupCmdLineOption;
import org.apache.oodt.cas.cl.option.SimpleCmdLineOption;
import org.apache.oodt.cas.cl.option.require.RequirementRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cl.option.require.StdRequirementRule;
import org.apache.oodt.cas.cl.option.require.ActionDependency;
import org.apache.oodt.cas.cl.util.CmdLineUtils;

//Google imports
import com.google.common.collect.Sets;

/**
 * Test class for {@link CmdLineUtils}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestCmdLineUtils extends TestCase {

	public void testDetermineRequired() {
		CmdLineAction action = createAction("TestAction");
		CmdLineOption urlOption, passOption;

		HashSet<CmdLineOption> options = Sets.newHashSet(
				urlOption = createSimpleOption("url",
						createRequiredRequirementRule(action)),
				passOption = createSimpleOption("pass", false),
				createSimpleOption("user", false), createActionOption("operation"));

		Set<CmdLineOption> requiredOptions = CmdLineUtils.determineRequired(action,
				options);
		assertEquals(Sets.newHashSet(urlOption), requiredOptions);

		options = Sets.newHashSet(
				urlOption = createSimpleOption("url",
						createRequiredRequirementRule(action)),
				passOption = createSimpleOption("pass", true),
				createSimpleOption("user", false), createActionOption("operation"));

		requiredOptions = CmdLineUtils.determineRequired(action, options);
		assertEquals(Sets.newHashSet(urlOption, passOption), requiredOptions);
	}

	public void testIsRequired() {
		CmdLineAction action = createAction("TestAction");
		assertTrue(CmdLineUtils.isRequired(action,
				createSimpleOption("url", createRequiredRequirementRule(action))));
		assertFalse(CmdLineUtils
				.isRequired(action, createSimpleOption("url", true)));
		assertFalse(CmdLineUtils.isRequired(action,
				createSimpleOption("url", false)));
	}

	public void testDetermineOptional() {
		CmdLineAction action = createAction("TestAction");
		CmdLineOption actionOption = new ActionCmdLineOption();

		HashSet<CmdLineOption> options = Sets.newHashSet(
				createSimpleOption("url", createRequiredRequirementRule(action)),
				createSimpleOption("pass", false), createSimpleOption("user", false),
				actionOption);

		Set<CmdLineOption> optionalOptions = CmdLineUtils.determineOptional(action,
				options);
		assertTrue(optionalOptions.isEmpty());

		options = Sets.newHashSet(createSimpleOption("pass", true),
				createSimpleOption("user", true), actionOption);

		optionalOptions = CmdLineUtils.determineOptional(action, options);
		assertTrue(optionalOptions.isEmpty());

		CmdLineOption passOption, userOption;
		options = Sets.newHashSet(
				passOption = createSimpleOption("pass",
						createOptionalRequirementRule(action)),
				userOption = createSimpleOption("user",
						createOptionalRequirementRule(action)), actionOption);

		optionalOptions = CmdLineUtils.determineOptional(action, options);
		assertEquals(Sets.newHashSet(passOption, userOption), optionalOptions);
	}

	public void testIsOptional() {
		CmdLineAction action = createAction("TestAction");
		assertTrue(CmdLineUtils.isOptional(action,
				createSimpleOption("url", createOptionalRequirementRule(action))));
		assertFalse(CmdLineUtils
				.isOptional(action, createSimpleOption("url", true)));
		assertFalse(CmdLineUtils.isOptional(action,
				createSimpleOption("url", false)));
	}

	public void testGetRequiredOptions() {
		CmdLineOption urlOption = createSimpleOption("url", true);
		HashSet<CmdLineOption> options = Sets.newHashSet(urlOption,
				createActionOption("action"), createSimpleOption("user", false),
				createSimpleOption("pass", false));

		assertEquals(Sets.newHashSet(urlOption),
				CmdLineUtils.getRequiredOptions(options));
	}

	public void testGetRequiredOptionsDoNotIgnoreActionOptions() {
		CmdLineOption actionOption, urlOption;
		HashSet<CmdLineOption> options = Sets.newHashSet(
				actionOption = createActionOption("action"),
				urlOption = createSimpleOption("url", true),
				createSimpleOption("user", false), createSimpleOption("pass", false));

		assertEquals(Sets.newHashSet(actionOption, urlOption),
				CmdLineUtils.getRequiredOptions(options, false));
	}

	public void testSortOptionsByRequiredStatus() {
		CmdLineAction action = createAction("action");
		CmdLineOption userOption, urlOption, passOption, actionOption;
		HashSet<CmdLineOption> options = Sets.newHashSet(
				userOption = createSimpleOption("user", false),
				urlOption = createSimpleOption("url",
						createRequiredRequirementRule(action)),
				passOption = createSimpleOption("pass", false),
				actionOption = createActionOption("action"));

		List<CmdLineOption> sortedOptions = CmdLineUtils
				.sortOptionsByRequiredStatus(options);
		assertEquals(options.size(), sortedOptions.size());
		assertEquals(actionOption, sortedOptions.get(0));
		assertEquals(urlOption, sortedOptions.get(1));
		assertEquals(Sets.newHashSet(userOption, passOption),
				Sets.newHashSet(sortedOptions.get(2), sortedOptions.get(3)));
	}

	public void testGetOptionByName() {
		CmdLineAction action = createAction("action");
		CmdLineOption userOption, urlOption, passOption, actionOption;
		HashSet<CmdLineOption> options = Sets.newHashSet(
				userOption = createSimpleOption("user", "username", false),
				urlOption = createSimpleOption("u", "url",
						createRequiredRequirementRule(action)),
				passOption = createSimpleOption("pass", "password", false),
				actionOption = createActionOption("action"));

		assertEquals(userOption,
				CmdLineUtils.getOptionByName(userOption.getShortOption(), options));
		assertEquals(userOption,
				CmdLineUtils.getOptionByName(userOption.getLongOption(), options));
		assertEquals(urlOption,
				CmdLineUtils.getOptionByName(urlOption.getShortOption(), options));
		assertEquals(urlOption,
				CmdLineUtils.getOptionByName(urlOption.getLongOption(), options));
		assertEquals(passOption,
				CmdLineUtils.getOptionByName(passOption.getShortOption(), options));
		assertEquals(passOption,
				CmdLineUtils.getOptionByName(passOption.getLongOption(), options));
		assertEquals(actionOption,
				CmdLineUtils.getOptionByName(actionOption.getShortOption(), options));
		assertEquals(actionOption,
				CmdLineUtils.getOptionByName(actionOption.getLongOption(), options));
	}

	public void testGetOptionInstanceByName() {
		CmdLineAction action = createAction("action");
		CmdLineOptionInstance userOptionInst, urlOptionInst, passOptionInst,
				actionOptionInst;
		HashSet<CmdLineOptionInstance> optionInsts = Sets.newHashSet(
				userOptionInst = new CmdLineOptionInstance(createSimpleOption("user",
						"username", false), new ArrayList<String>()),
				urlOptionInst = new CmdLineOptionInstance(createSimpleOption("u",
						"url", createRequiredRequirementRule(action)),
						new ArrayList<String>()),
				passOptionInst = new CmdLineOptionInstance(createSimpleOption("pass",
						"password", false), new ArrayList<String>()),
				actionOptionInst = new CmdLineOptionInstance(
						createActionOption("action"), new ArrayList<String>()));

		assertEquals(userOptionInst, CmdLineUtils.getOptionInstanceByName(
				userOptionInst.getOption().getShortOption(), optionInsts));
		assertEquals(userOptionInst, CmdLineUtils.getOptionInstanceByName(
				userOptionInst.getOption().getLongOption(), optionInsts));
		assertEquals(urlOptionInst, CmdLineUtils.getOptionInstanceByName(
				urlOptionInst.getOption().getShortOption(), optionInsts));
		assertEquals(urlOptionInst, CmdLineUtils.getOptionInstanceByName(
				urlOptionInst.getOption().getLongOption(), optionInsts));
		assertEquals(passOptionInst, CmdLineUtils.getOptionInstanceByName(
				passOptionInst.getOption().getShortOption(), optionInsts));
		assertEquals(passOptionInst, CmdLineUtils.getOptionInstanceByName(
				passOptionInst.getOption().getLongOption(), optionInsts));
		assertEquals(actionOptionInst, CmdLineUtils.getOptionInstanceByName(
				actionOptionInst.getOption().getShortOption(), optionInsts));
		assertEquals(actionOptionInst, CmdLineUtils.getOptionInstanceByName(
				actionOptionInst.getOption().getLongOption(), optionInsts));
	}

	private static CmdLineAction createAction(String name) {
		return new CmdLineAction(name, "This is an action description") {

			@Override
			public void execute() {
				// do nothing
			}

		};
	}

	private static GroupCmdLineOption createGroupOption(String longName,
			boolean required) {
		GroupCmdLineOption option = new GroupCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		option.setRequired(required);
		return option;
	}

	private static SimpleCmdLineOption createSimpleOption(String longName,
			boolean required) {
		return createSimpleOption(longName, longName, required);
	}

	private static SimpleCmdLineOption createSimpleOption(String shortName,
			String longName, boolean required) {
		SimpleCmdLineOption option = new SimpleCmdLineOption();
		option.setShortOption(shortName);
		option.setLongOption(longName);
		option.setRequired(required);
		return option;
	}

	private static SimpleCmdLineOption createSimpleOption(String longName,
			RequirementRule rule) {
		return createSimpleOption(longName, longName, rule);
	}

	private static SimpleCmdLineOption createSimpleOption(String shortName,
			String longName, RequirementRule rule) {
		SimpleCmdLineOption option = new SimpleCmdLineOption();
		option.setShortOption(shortName);
		option.setLongOption(longName);
		option.setRequirementRules(Collections.singletonList(rule));
		return option;
	}

	public static ActionCmdLineOption createActionOption(String longName) {
		ActionCmdLineOption option = new ActionCmdLineOption();
		option.setLongOption(longName);
		option.setShortOption(longName);
		return option;
	}

	private static RequirementRule createRequiredRequirementRule(
			CmdLineAction action) {
		StdRequirementRule rule = new StdRequirementRule();
		rule.addActionDependency(new ActionDependency(action.getName(),
				Relation.REQUIRED));
		return rule;
	}

	private static RequirementRule createOptionalRequirementRule(
			CmdLineAction action) {
		StdRequirementRule rule = new StdRequirementRule();
		rule.addActionDependency(new ActionDependency(action.getName(),
				Relation.OPTIONAL));
		return rule;
	}
}
