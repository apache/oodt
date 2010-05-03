//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.config;

//OODT imports
import gov.nasa.jpl.oodt.cas.pge.metadata.PgeMetadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Interface for building a {@link PgeConfig}
 * </p>.
 */
public interface PgeConfigBuilder {

    public PgeConfig build(PgeMetadata pgeMetadata) throws Exception;

}
