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


package org.apache.oodt.cas.crawl;

//JDK imports
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.commons.option.CmdLineOptionInstance;
import org.apache.oodt.commons.option.util.CmdLineOptionUtils;
import org.apache.oodt.cas.crawl.daemon.CrawlDaemon;

//Spring import
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * @since OODT-190
 * 
 * <p>
 * A command line interface to the new Spring enabled crawler.
 * </p>
 */
public class CrawlerLauncher {

    private static Logger LOG = Logger.getLogger(CrawlerLauncher.class
            .getName());

    private String beanRepo;

    public CrawlerLauncher() {
        this(System.getProperty("org.apache.oodt.cas.crawl.bean.repo",
                "classpath:/org/apache/oodt/cas/crawl/crawler-config.xml"));
    }

    public CrawlerLauncher(String beanRepo) {
        this.beanRepo = beanRepo;
    }

    public void processMain(String[] args) {
        FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(
                this.beanRepo);

        try {
            List<CmdLineOptionInstance> optionInstances = CmdLineOptionUtils
                    .loadValidateAndHandleInstances(appContext, args);

            ProductCrawler pc = (ProductCrawler) appContext
                    .getBean(CmdLineOptionUtils.getOptionValues("crawlerId",
                            optionInstances).get(0));
            System.out.println(pc.getFilemgrUrl());
            System.out.println(pc.getId());

            pc.setApplicationContext(appContext);
            if (pc.getDaemonPort() != -1 && pc.getDaemonWait() != -1)
                new CrawlDaemon(pc.getDaemonWait(), pc, pc.getDaemonPort())
                        .startCrawling();
            else
                pc.crawl();

        } catch (Exception e) {
            System.err.println("Failed to parse options : "
                            + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        new CrawlerLauncher().processMain(args);
    }

}
