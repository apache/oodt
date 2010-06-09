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


package jpl.eda.profile.handlers;

import java.util.Collection;
import jpl.eda.profile.ProfileException;
import junit.framework.TestCase;

/**
 * Unit test the <code>Utility</code> class.
 *
 * @author Kelly
 */ 
public class UtilityTest extends TestCase {
	/**
	 * Creates a new <code>ProfileTest</code> instance.
	 *
	 * @param name a <code>String</code> value.
	 */
	public UtilityTest(String name) {
		super (name);
	}

	/**
	 * Test the <code>getProfileCollection</code> method.
	 *
	 * @throws ProfileException if an error occurs.
	 */
	public void testGetProfileCollection() throws ProfileException {
		Collection profiles = Utility.getProfileCollection(PROFILES);
		assertEquals(1, profiles.size());
		
	}

	/** Test document containing perhaps multiple profiles. */
	private static final String PROFILES = "<profiles><profile><profAttributes><profId>1</profId><profType>PROFILE</profType>"
		+ "<profStatusId>ACTIVE</profStatusId></profAttributes><resAttributes><Identifier>1</Identifier>"
		+ "<resContext>TEST</resContext><resClass>TEST</resClass></resAttributes></profile></profiles>";

}
