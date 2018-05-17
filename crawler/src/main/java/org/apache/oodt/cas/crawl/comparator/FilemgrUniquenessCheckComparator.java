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


package org.apache.oodt.cas.crawl.comparator;

//JDK imports
import java.io.File;
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;
import org.apache.oodt.cas.metadata.preconditions.PreConditionComparator;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A pre-ingest, Metadata extractor level comparator that checks whether a
 * product exists before generating metadata for it.
 * </p>.
 */
public class FilemgrUniquenessCheckComparator extends PreConditionComparator<Boolean> {

    private String filemgrUrl;

    @Override
    protected int performCheck(File product, Boolean compareItem)
            throws PreconditionComparatorException {
        try (FileManagerClient fmClient= RpcCommunicationFactory.createClient(new URL(filemgrUrl))){
            boolean returnVal = fmClient.hasProduct(product.getName());
            return Boolean.valueOf(returnVal).compareTo(compareItem);
        } catch (Exception e) {
            throw new PreconditionComparatorException(
                    "Failed to check for product " + product + " : "
                            + e.getMessage());
        }
    }

    @Required
    public void setFilemgrUrl(String filemgrUrl) {
        this.filemgrUrl = filemgrUrl;
    }

}
