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
