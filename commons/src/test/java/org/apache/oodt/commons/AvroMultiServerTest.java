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

package org.apache.oodt.commons;

import junit.framework.TestCase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.server.RemoteObject;

public class AvroMultiServerTest extends TestCase {

    private InputStream testConfig;

    public AvroMultiServerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testConfig = getClass().getResourceAsStream("/test-multiserver.xml");
        if (testConfig == null) throw new IOException("Cannot find `test-multiserver.xml'");
        System.setProperty("my.other.setting", "Don't override");
    }

    public void tearDown() throws Exception {
        if (testConfig != null) try {
            testConfig.close();
        } catch (IOException ignore) {}
        System.getProperties().remove("my.setting");
        System.getProperties().remove("my.other.setting");
        super.tearDown();
    }

    public static class Svr1 extends RemoteObject {
        public Svr1(ExecServer e) {}
    }
    public static class Svr2 extends RemoteObject {
        public Svr2(ExecServer e) {}
    }
    public static class Svr3 extends RemoteObject {
        public Svr3(ExecServer e) {}
    }
    public static class Svr4 extends RemoteObject {
        public Svr4(ExecServer e) {}
    }


    public void testParsing() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        InputSource is = new InputSource(testConfig);
        MultiServer.parseConfig(is);
        assertEquals("test.app", MultiServer.getAppName());
        assertEquals(4, MultiServer.getServers().size());

        MultiServer.Server server = (MultiServer.Server) MultiServer.getServers().get("urn:eda:rmi:Test1");
        assertEquals("org.apache.oodt.commons.MultiServerTest$Svr1", server.getClassName());
        assertEquals(MultiServer.BINDING, server.getBindingBehavior());

        server = (MultiServer.Server) MultiServer.getServers().get("urn:eda:rmi:Test2");
        assertEquals("org.apache.oodt.commons.MultiServerTest$Svr2", server.getClassName());
        assertEquals(MultiServer.NONBINDING, server.getBindingBehavior());

        server = (MultiServer.Server) MultiServer.getServers().get("urn:eda:rmi:Test3");
        assertEquals("org.apache.oodt.commons.MultiServerTest$Svr3", server.getClassName());
        assertEquals(MultiServer.REBINDING, server.getBindingBehavior());

        MultiServer.AutobindingServer s = (MultiServer.AutobindingServer) MultiServer.getServers().get("urn:eda:rmi:Test4");
        assertEquals("org.apache.oodt.commons.MultiServerTest$Svr4", s.getClassName());
        assertEquals(MultiServer.AUTO, s.getBindingBehavior());
        assertEquals(360000L, s.getPeriod());

        assertEquals("My Value", System.getProperty("my.setting"));
        assertEquals("Don't override", System.getProperty("my.other.setting"));
    }

}
