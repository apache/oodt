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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawler.structs.avrotypes.IntAvroCrawlDaemon;

import java.net.InetSocketAddress;

public class AvroRpcCrawlerDaemon extends CrawlDaemon implements IntAvroCrawlDaemon {

    public AvroRpcCrawlerDaemon(int wait, ProductCrawler crawler, int port) {
        super(wait,crawler,port);
    }

    @Override
    public void startCrawling() {
        Server server = new NettyServer(new SpecificResponder(IntAvroCrawlDaemon.class,this), new InetSocketAddress(this.getDaemonPort()));
        server.start();
        this.crawl();
        server.close();
    }

    @Override
    public double rpcGetAverageCrawlTime() throws AvroRemoteException {
        return  this.getAverageCrawlTime();
    }

    @Override
    public int rpcGetMilisCrawling() throws AvroRemoteException {
        return this.getMilisCrawling();
    }

    @Override
    public int rpcGetWaitInterval() throws AvroRemoteException {
        return this.getWaitInterval();
    }

    @Override
    public int rpcGetNumCrawls() throws AvroRemoteException {
        return this.getNumCrawls();
    }

    @Override
    public boolean rpcIsRunning() throws AvroRemoteException {
        return this.isRunning();
    }

    @Override
    public boolean rpcStop() throws AvroRemoteException {
        return this.stop();
    }

    @Override
    public Void rpcSetWaitInterval(long waitInterval) throws AvroRemoteException {
        this.setWaitInterval(waitInterval);
        return null;
    }
}
