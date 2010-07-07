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


package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Check whether a product exists in the database already
 * </p>.
 */
public class FilemgrUniquenessChecker extends CrawlerAction {

    private String filemgrUrl;

    public FilemgrUniquenessChecker() {}

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.crawl.action.CrawlerAction#performAction(java.io.File,
     *      org.apache.oodt.cas.metadata.Metadata)
     */
    public boolean performAction(File product, Metadata productMetadata)
            throws CrawlerActionException {
        try {
            URL filemgrURL = new URL(this.filemgrUrl);
            XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(
                    filemgrURL);
            return !fmClient.hasProduct(productMetadata
                    .getMetadata(CoreMetKeys.PRODUCT_NAME));
        } catch (Exception e) {
            throw new CrawlerActionException(
                    "Product failed uniqueness check : [" + product + "] : "
                            + e.getMessage());
        }
    }

    @Required
    public void setFilemgrUrl(String filemgrUrl) {
        this.filemgrUrl = filemgrUrl;
    }

}
