//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.filerestrictions;

//JDK imports
import java.io.FileInputStream;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ParserException;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface Parser {

    public VirtualFileStructure parse(FileInputStream inputFile)
            throws ParserException;

}
