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
package org.apache.oodt.cas.crawl.cli.action;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawl.daemon.CrawlDaemon;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A {@link CmdLineAction} which is responsible for launching crawlers.
 *
 * @author bfoster (Brian Foster)
 */
public class CrawlerLauncherCliAction extends CmdLineAction {

   private String beanRepo;
   private String crawlerId;

   public CrawlerLauncherCliAction() {
      this.beanRepo = System.getProperty("org.apache.oodt.cas.crawl.bean.repo",
            "classpath:/org/apache/oodt/cas/crawl/crawler-config.xml");
   }

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {

      FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(
            this.beanRepo);

      try {
         ProductCrawler pc = (ProductCrawler) appContext
               .getBean(crawlerId != null ? crawlerId : getName());
         pc.setApplicationContext(appContext);
         if (pc.getDaemonPort() != -1 && pc.getDaemonWait() != -1) {
            new CrawlDaemon(pc.getDaemonWait(), pc, pc.getDaemonPort())
                  .startCrawling();
            printer.println("Finished crawler daemon");
         } else {
            pc.crawl();
            printer.println("Finished crawling");
         }
         pc.shutdown();
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to launch crawler : "
               + e.getMessage(), e);
      }
   }

   public void setCrawlerId(String crawlerId) {
      this.crawlerId = crawlerId;
   }
}
