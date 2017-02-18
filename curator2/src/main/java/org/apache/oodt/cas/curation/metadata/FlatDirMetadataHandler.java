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
    public Metadata get(String file,String user) throws InstantiationException, FileNotFoundException, IOException {
        SerializableMetadata met = new SerializableMetadata(ENCODING,false);
        met.loadMetadataFromXmlStream(new FileInputStream(FlatDirMetadataHandler.getLocation(file,user)));
        return met;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.curation.metadata.MetadataHandler#set(java.lang.String, org.apache.oodt.cas.metadata.Metadata)
     */
    @Override
    public void set(String file,String user, Metadata metadata) throws FileNotFoundException, IOException {
        SerializableMetadata ser = new SerializableMetadata(metadata);
        ser.writeMetadataToXmlStream(new FileOutputStream(FlatDirMetadataHandler.getLocation(file,user)));
    }
    /*
     * (non-Javadoc)
     * @see org.apache.oodt.cas.curation.metadata.MetadataHandler#remove(java.lang.String)
     */
    @Override
    public void remove(String file,String user) throws FileNotFoundException, IOException {
        File metfile = FlatDirMetadataHandler.getLocation(file,user);
        if (metfile.exists()) {
            if (!metfile.delete()) {
                throw new IOException("Failed to delete: "+metfile.getAbsolutePath());
            }
        }
    }
    /**
     * Gets the flat-dir location for the file's metadata
     * @param file - file object
     * @param user - user isolation id
     * @return file representing location of the flat-file
     */
    private static File getLocation(String file,String user) {
        return new File(Configuration.getWithReplacement(Configuration.METADATA_AREA_CONFIG),user.replace(File.separatorChar, '_')+"-"+file.replace(File.separatorChar, '_')+".met");
    }
}
