//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge;

//OODT imports
import gov.nasa.jpl.oodt.cas.pge.metadata.PgeMetadata;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An interface for adding dynamic properties to the {@link Metadata} based on
 * metadata supplied. This interface is used by the PGETask to dynamically add
 * PGE-specific properties that aren't general to all PGEs.
 * </p>
 * 
 */
public interface ConfigFilePropertyAdder {

    public void addConfigProperties(PgeMetadata metadata, Object... objs);

}
