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
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Deletes a product file as a {@link CrawlerAction} reponse
 * </p>.
 */
public class DeleteFile extends CrawlerAction {

    private String file;

    private String fileExtension;

    public DeleteFile() {
    }

    public boolean performAction(File product, Metadata productMetadata)
            throws CrawlerActionException {
        File fileToDelete = product;
        try {
            if (file != null)
                fileToDelete = new File(file);
            else if (fileExtension != null)
                fileToDelete = new File(fileToDelete.getAbsolutePath() + "."
                        + fileExtension);

            LOG.log(Level.INFO, "Deleting file "
                    + fileToDelete.getAbsolutePath());
            return fileToDelete.delete();
        } catch (Exception e) {
            throw new CrawlerActionException("Error while deleting file "
                    + fileToDelete + " : " + e.getMessage());
        }
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

}
