//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalmethod;

//JDK imports
import java.io.File;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.config.DataFilesInfo;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.Parser;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem.DataFileToPropFileLinker;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem.FileRetrievalSystem;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface RetrievalMethod {

    public void processPropFile(FileRetrievalSystem frs,
            Parser propFileParser, File propFile, DataFilesInfo dfi,
            DataFileToPropFileLinker linker) throws Exception;

}
