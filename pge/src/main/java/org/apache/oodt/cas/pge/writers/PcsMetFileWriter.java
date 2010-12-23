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
package org.apache.oodt.cas.pge.writers;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;

//OODT imports
import java.io.File;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Writes a PCS metadata file for the give data file
 * </p>.
 */
public abstract class PcsMetFileWriter {

	public static final String FILE_SIZE = "FileSize";
	
    public Metadata getMetadataForFile(File sciPgeCreatedDataFile,
            ControlMetadata ctrlMetadata, Object... customArgs) throws Exception {
        try {
            Metadata inputMetadata = ctrlMetadata.asMetadata();

            inputMetadata.replaceMetadata(CoreMetKeys.FILENAME,
                    sciPgeCreatedDataFile.getName());
            inputMetadata.replaceMetadata(CoreMetKeys.FILE_LOCATION,
                    sciPgeCreatedDataFile.getParentFile().getAbsolutePath());
            inputMetadata.replaceMetadata(FILE_SIZE, Long.toString(new File(
					inputMetadata.getMetadata(CoreMetKeys.FILE_LOCATION),
					inputMetadata.getMetadata(CoreMetKeys.FILENAME)).length()));
            
            return this.getSciPgeSpecificMetadata(
                    sciPgeCreatedDataFile, inputMetadata, customArgs);
        } catch (Exception e) {
            throw new Exception("Failed to create PCS metadata file for '"
                    + sciPgeCreatedDataFile + "' : " + e.getMessage(), e);
        }
    }

    protected abstract Metadata getSciPgeSpecificMetadata(
            File sciPgeCreatedDataFile, Metadata inputMetadata,
            Object... customArgs) throws Exception;

}
