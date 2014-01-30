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

package org.apache.oodt.cas.resource.monitor;

import junit.framework.TestCase;
import org.apache.oodt.cas.resource.monitor.ganglia.GangliaResourceMonitor;
import org.apache.oodt.cas.resource.monitor.ganglia.GangliaResourceMonitorFactory;
import org.apache.oodt.cas.resource.monitor.utils.MockGmetad;
import org.apache.oodt.cas.resource.structs.ResourceNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author rajith
 * @version $Revision$
 *
 * Test Suite for the {@link org.apache.oodt.cas.resource.monitor.ganglia.GangliaResourceMonitor}
 */
public class TestGangliaResourceMonitor extends TestCase {

    private GangliaResourceMonitor gangliaResourceMonitor;
    private ThreadLocal<MockGmetad> mockGmetad = new ThreadLocal<MockGmetad>();

    @Override
    protected void setUp() throws IOException {
        generateTestConfig();
        runMockGmetad();

        gangliaResourceMonitor = (GangliaResourceMonitor)
                new GangliaResourceMonitorFactory().createResourceMonitor();
    }

    @Override
    protected void tearDown(){
        mockGmetad.remove();
    }

    public void testGetLoad() {
        try {
            ResourceNode resourceNode =
                    new ResourceNode("localhost",new URL("http://localhost:9999"), 8);
            assertEquals((float)1.556, gangliaResourceMonitor.getLoad(resourceNode));
        } catch (MalformedURLException ignored) {
            //Exception ignored
        }
    }

    public void testUpdateLoad(){
        try {
            ResourceNode resourceNode =
                    new ResourceNode("remotenode",new URL("http://localhost:9999"), 9);
            assertEquals((float) 1.751, gangliaResourceMonitor.getLoad(resourceNode));

            gangliaResourceMonitor.updateLoad("remotenode", 6);
            assertEquals((float) 6, gangliaResourceMonitor.getLoad(resourceNode));
        }  catch (MalformedURLException ignored) {
            //Exception ignored
        }
    }

    public void testRemoveNodeById(){
        try {
            ResourceNode resourceNode =
                    new ResourceNode("remotenode",new URL("http://localhost:9999"), 9);

            gangliaResourceMonitor.removeNodeById("remotenode");
            /*since node is not available node's capacity is returned as the load*/
            assertEquals((float) 9, gangliaResourceMonitor.getLoad(resourceNode));
        } catch (MalformedURLException ignored) {
            //Exception ignored
        }
    }

    private void runMockGmetad() {
        int port = Integer.valueOf(System
                .getProperty("org.apache.oodt.cas.resource.monitor.ganglia.gemtad.host.port"));
        String sampleXMLfilePath = "." + File.separator + "src" + File.separator +
                "testdata" + File.separator + "resourcemon" + File.separator + "gangliaXMLdump.xml";
        mockGmetad.set(new MockGmetad(port, sampleXMLfilePath));

        Thread mockGmetadServer = new Thread(mockGmetad.get());
        mockGmetadServer.start();
    }

    private void generateTestConfig() throws IOException {
        String propertiesFile = "." + File.separator + "src" + File.separator +
                "testdata" + File.separator + "test.resource.properties";
        System.getProperties().load(new FileInputStream(new File(propertiesFile)));
        System.setProperty("org.apache.oodt.cas.resource.nodes.dirs",
                "file:" + new File("." + File.separator + "src" + File.separator +
                        "testdata" + File.separator + "resourcemon").getAbsolutePath());
    }

}
