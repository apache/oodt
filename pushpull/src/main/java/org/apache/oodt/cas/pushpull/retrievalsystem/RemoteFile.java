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


package org.apache.oodt.cas.pushpull.retrievalsystem;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;

//JDK imports
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class RemoteFile implements RemoteFileMetKeys {

    private Metadata metadata;

    private ProtocolFile pFile;

    public RemoteFile(ProtocolFile pFile) {
        this.pFile = pFile;
        this.metadata = new Metadata();
        this.metadata.addMetadata(RETRIEVED_FROM_LOC, pFile.getProtocolPath()
                .getPathString());
        this.metadata.addMetadata(FILENAME, pFile.getName());
        this.metadata.addMetadata(DATA_PROVIDER, pFile.getHostName());
    }

    public void setUniqueMetadataElement(String uniqueMetadataElement) {
        this.metadata.addMetadata(PRODUCT_NAME, this
                .getMetadata(uniqueMetadataElement));
    }

    public void addMetadata(String key, String value) {
        this.metadata.addMetadata(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.getMetadata(key);
    }

    public Metadata getAllMetadata() {
        return this.metadata;
    }

    public ProtocolFile getProtocolFile() {
        return this.pFile;
    }

    public void writeToPropEqValFile(String filePath,
            String[] metadataToWriteOut) throws IOException {
        try {
            SerializableMetadata sMetadata = new SerializableMetadata("UTF-8",
                    false);
            for (String metadataKey : metadataToWriteOut)
                if (this.metadata.getMetadata(metadataKey) != null
                        && !this.metadata.getMetadata(metadataKey).equals(""))
                    sMetadata.addMetadata(metadataKey, this.metadata
                            .getMetadata(metadataKey));
            sMetadata.writeMetadataToXmlStream(new FileOutputStream(filePath));
        } catch (Exception e) {
            throw new IOException("Failed to write metadata file for "
                    + this.pFile + " : " + e.getMessage());
        }
    }

}
