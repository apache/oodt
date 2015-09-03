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

package org.apache.oodt.cas.crawl.daemon;

import junit.framework.TestCase;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

public class TestDaemonCrawler extends TestCase {

    private DummyDaemonCrawler crawler;

    public void setUp(){
        crawler = new DummyDaemonCrawler(1,null,8001);
        crawler.startCrawling();
    }

    public void testAvroDaemonCrawlerController(){
        try {
            AvroCrawlDaemonController controller = new AvroCrawlDaemonController("http://localhost:8001");
            assertTrue(controller.isRunning());
            assertEquals(1,controller.getWaitInterval());
        } catch (InstantiationException e) {
            fail(e.getMessage());
        } catch (CrawlException e) {
            fail(e.getMessage());
        } catch (InstanceRepositoryException e) {
            fail(e.getMessage());
        }
    }

    public void tearDown(){
        crawler.closeServer();
    }
}
