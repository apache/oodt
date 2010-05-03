//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.writers;

//OODT imports
import gov.nasa.jpl.oodt.cas.pge.metadata.PgeMetadata;
import gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

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
            PgeMetadata pgeMetadata, Object... customArgs) throws Exception {
        try {
            Metadata inputMetadata = pgeMetadata.getMetadata();

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
