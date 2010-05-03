//Copyright 2000-2005 California Institute of Technology.  ALL RIGHTS RESERVED.
//U.S. Government Sponsorship acknowledged.
//
//$Id: HttpProfileClientTest.java,v 1.2 2005/05/01 22:50:40 cmattmann Exp $

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