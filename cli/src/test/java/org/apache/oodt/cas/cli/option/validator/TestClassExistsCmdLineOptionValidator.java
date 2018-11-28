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
package org.apache.oodt.cas.cli.option.validator;

//OODT static imports
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createSimpleOption;

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.validator.ClassExistsCmdLineOptionValidator;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator.Result;

/**
 * Test class for {@link ClassExistsCmdLineOptionValidator}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestClassExistsCmdLineOptionValidator extends TestCase {

   public void testValidate() {
      // Check null option instance not allowed.
      try {
         new ClassExistsCmdLineOptionValidator().validate(null);
      } catch (IllegalArgumentException ignore) { /* expect throw */
      }

      // Check fail case.
      CmdLineOptionInstance instance = createOptionInstance(
            createSimpleOption("test", false), "bogus");
      assertEquals(Result.Grade.FAIL, new ClassExistsCmdLineOptionValidator()
            .validate(instance).getGrade());

      // Check pass case.
      instance = createOptionInstance(createSimpleOption("test", false),
            "java.lang.String");
      assertEquals(Result.Grade.PASS, new ClassExistsCmdLineOptionValidator()
            .validate(instance).getGrade());
   }
}
