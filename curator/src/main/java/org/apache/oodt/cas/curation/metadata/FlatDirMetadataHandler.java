/**
 * 
 */
package org.apache.oodt.cas.curation.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

/**
 * A metadata handler that uses a directory and fills it with flat-files.
 * @author starchmd
 */
public class FlatDirMetadataHandler implements MetadataHandler {
    public static final String ENCODING = "UTF-8";

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.curation.metadata.MetadataHandler#get(java.lang.String)
     */
    @Override
    public Metadata get(String file) throws InstantiationException, FileNotFoundException, IOException {
        SerializableMetadata met = new SerializableMetadata(ENCODING,false);
        met.loadMetadataFromXmlStream(new FileInputStream(FlatDirMetadataHandler.getLocation(file)));
        return met;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.curation.metadata.MetadataHandler#set(java.lang.String, org.apache.oodt.cas.metadata.Metadata)
     */
    @Override
    public void set(String file, Metadata metadata) throws FileNotFoundException, IOException {
        SerializableMetadata ser = new SerializableMetadata(metadata);
        ser.writeMetadataToXmlStream(new FileOutputStream(FlatDirMetadataHandler.getLocation(file)));
    }
    /**
     * Gets the flat-dir location for the file's metadata
     * @param file - file object
     * @return file representing location of the flat-file
     */
    private static File getLocation(String file) {
        return new File(Configuration.getWithReplacement(Configuration.METADATA_AREA_CONFIG),file.replace(File.separatorChar, '_')+".met");
    }
}
