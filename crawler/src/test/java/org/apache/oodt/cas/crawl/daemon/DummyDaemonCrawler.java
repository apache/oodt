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

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawler.structs.avrotypes.IntAvroCrawlDaemon;

import java.net.InetSocketAddress;

public class DummyDaemonCrawler extends AvroRpcCrawlerDaemon {

    private int port;

    private Server server;

    public DummyDaemonCrawler(int wait, ProductCrawler crawler, int port) {
        super(wait, crawler, port);
        this.port = port;
    }

    @Override
    public void  startCrawling(){
        server = new NettyServer(new SpecificResponder(IntAvroCrawlDaemon.class,this), new InetSocketAddress(this.port));
        server.start();
    }

    public void closeServer(){
        server.close();
    }
}
