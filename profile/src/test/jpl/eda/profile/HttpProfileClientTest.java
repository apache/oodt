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


package jpl.eda.profile;

import junit.framework.TestCase;
import java.util.List;

import java.io.UnsupportedEncodingException;


/**
* Unit test the Http Profile Client
*
* @author mattmann
*/ 
public class HttpProfileClientTest extends TestCase {
	private List profiles=null;
	
	/** Construct the test case for the HTTPProfileClient class. */
	public HttpProfileClientTest(String name) {
		super(name);
	}

	protected void setUp() {

		
		HTTPProfileClient profClient = new HTTPProfileClient("http://starbrite.jpl.nasa.gov/q","JPL.PDS.Mission");
		
		try{
			profiles = profClient.query("MISSION_NAME = 'VIKING'");
		}
		catch(ProfileException e){
			fail(e.getMessage());
		}
		catch(UnsupportedEncodingException e1){
			fail(e1.getMessage());
		}
	}

	protected void tearDown(){}

	public void testQuery() {

	  //if(profiles != null)
        //System.out.println("Profiles not null");
        
	  //System.out.println("Profiles size = "+profiles.size());
		assertTrue("The returned profiles are null",profiles != null);
		assertTrue("The size of the profiles is 0",profiles.size() > 0);

	}

}
