//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.structs.Job;
import gov.nasa.jpl.oodt.cas.resource.structs.NameValueJobInput;
import gov.nasa.jpl.oodt.cas.resource.system.XmlRpcResourceManagerClient;

//JDK imports
import java.net.URL;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * Tests the Job Submission capability of the Resource Manager
 * </p>.
 */
public class TestResmgr {

  public static void main(String[] Args) {

    if (Args.length != 1) {
      System.err.println("Specify a XmlRpcResourceManager Host");
      System.exit(1);
    }

    try {
      URL managerUrl = new URL(Args[0]);
      XmlRpcResourceManagerClient client = new XmlRpcResourceManagerClient(
          managerUrl);

      Job hw1 = new Job("JobOne", "HelloWorldJob",
          "gov.nasa.jpl.oodt.cas.resource.examples.HelloWorldJob",
          "gov.nasa.jpl.oodt.cas.resource.structs.NameValueJobInput", "quick",
          new Integer(1));
      NameValueJobInput hw1Input = new NameValueJobInput();
      hw1Input.setNameValuePair("user.name", "Dave");

      client.submitJob(hw1, hw1Input);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}