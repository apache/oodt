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

package org.apache.oodt.cas.resource.batchmgr;

import junit.framework.TestCase;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.system.extern.AvroRpcBatchStub;

import java.net.MalformedURLException;
import java.net.URL;

public class TestBatchMgr extends TestCase {

  public void testFake() {
    
  }
  
    
    //Disabled until API impl can be finished  
    public void XtestAvroBatchMgr(){
        AvroRpcBatchMgrFactory avroRpcBatchMgrFactory = new AvroRpcBatchMgrFactory();
        Batchmgr batchmgr = avroRpcBatchMgrFactory.createBatchmgr();
        assertNotNull(batchmgr);

        try {
            AvroRpcBatchStub avroRpcBatchStub = new AvroRpcBatchStub(50001);
        } catch (Exception e) {

            e.printStackTrace();
            fail(e.getMessage());
        }
        ResourceNode resNode = new ResourceNode();
        try {
            resNode.setIpAddr(new URL("http://localhost:50001"));
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }

        ResourceNode rn = new ResourceNode();
        try {
          rn.setIpAddr(new URL("http://localhost:50001"));
        } catch (MalformedURLException e) {
          e.printStackTrace();
          fail(e.getMessage());
        }
        AvroRpcBatchMgrProxy bmc = new AvroRpcBatchMgrProxy(new JobSpec(), rn,(AvroRpcBatchMgr)batchmgr);
        assertTrue(bmc.nodeAlive());

    }
}
