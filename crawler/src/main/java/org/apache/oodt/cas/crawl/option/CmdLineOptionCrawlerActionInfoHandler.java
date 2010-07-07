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


package org.apache.oodt.cas.crawl.option;

//JDK imports
import java.io.PrintStream;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.commons.option.CmdLineOption;
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.MimeTypeCrawlerAction;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class CmdLineOptionCrawlerActionInfoHandler extends
        CmdLineOptionInfoHandler {

    @Override
    public String getCustomOptionUsage(CmdLineOption option) {
        return "";
    }

    @Override
    public void handleOption(CmdLineOption option, List<String> values) {
        String[] actionIds = this.getApplicationContext().getBeanNamesForType(
                CrawlerAction.class);
        PrintStream ps = new PrintStream(this.getOutStream());
        ps.println("Actions:");
        for (String actionId : actionIds) {
            CrawlerAction ca = (CrawlerAction) this.getApplicationContext()
                    .getBean(actionId);
            ps.println("  Action:");
            ps.println("    Id: " + ca.getId());
            ps.println("    Description: " + ca.getDescription());
            ps.println("    Phases: " + ca.getPhases());
            if (ca instanceof MimeTypeCrawlerAction)
                ps.println("    MimeTypes: " 
                        + ((MimeTypeCrawlerAction) ca).getMimeTypes());
            ps.println();
        }
        ps.close();
    }

}
