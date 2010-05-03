//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.writers;

//JDK imports
import java.io.File;
import java.io.IOException;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Abstract interface for generating PGE config input files defining the input
 * necessary to run the underlying PGE
 * </p>.
 */
public interface SciPgeConfigFileWriter {

    /**
     * 
     * @param sciPgeConfigFilePath
     * @param inputMetadata
     * @param customArgs
     * @return
     * @throws IOException
     */
    public File createConfigFile(String sciPgeConfigFilePath,
            Metadata inputMetadata, Object... customArgs) throws IOException;

}
