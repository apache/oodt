package org.apache.oodt.cas.curation.metadata;

import org.apache.oodt.cas.metadata.Metadata;

/**
 * A specific handler for metadata temporary storage. This does not handle the final cataloging,
 * but acts as a temporaty storage of metadata that the user is curating.
 * 
 * @author starchmd
 */
public interface MetadataHandler {
    /**
     * Gets the metadata for a file or null if none exists
     * @param file - file the metadata describes
     * @return OODT metadata object
     * @throws Exception 
     */
    public Metadata get(String file) throws Exception;
    /**
     * Sets the metadata for a file
     * @param file - file the metadata describes
     * @param metadata - metadata object to set
     */
    public void set(String file,Metadata metadata) throws Exception;
}
