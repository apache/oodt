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
package org.apache.oodt.cas.cl.option.validator;

//OODT static imports
import static org.apache.oodt.cas.cl.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cl.test.util.TestUtils.createSimpleOption;

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

//Google imports
import com.google.common.collect.Lists;

/**
 * Test class for {@link AllowedArgsCmdLineOptionValidator}. 
 *
 * @author bfoster (Brian Foster)
 */
public class TestAllowedArgsCmdLineOptionValidator extends TestCase {

	public void testIntialState() {
		// Test no null option instance allowed.
		try {
			new AllowedArgsCmdLineOptionValidator().validate(null);
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException ignore) { /* expect throw */ }

		// Test no allowed args set and valid option instance
		CmdLineOptionInstance instance = createOptionInstance(
				createSimpleOption("test", false), "value");
		assertFalse(new AllowedArgsCmdLineOptionValidator().validate(instance));
	}

	public void testValidate() {
		AllowedArgsCmdLineOptionValidator validator = new AllowedArgsCmdLineOptionValidator();
		validator.setAllowedArgs(Lists.newArrayList("value1", "value2"));

		// Test no null option instance allowed.
		try {
			validator.validate(null);
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException ignore) { /* expect throw */ }

		// Test should fail case.
		CmdLineOptionInstance instance = createOptionInstance(
				createSimpleOption("test", false), "value");
		assertFalse(validator.validate(instance));

		// Test should pass case.
		instance = createOptionInstance(createSimpleOption("test", false), "value1");
		assertTrue(validator.validate(instance));
	}
}
