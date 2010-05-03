//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.SerializableMetadata;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFile;

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
