//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface PgeTaskMetadataKeys {

    public static final String NAME = "PGETask_Name";

    public static final String SCI_EXE_PATH = "PGETask_SciExe_Path";

    public static final String SCI_EXE_VERSION = "PGETask_SciExe_Version";

    public static final String PRODUCT_PATH = "PGETask_ProductPath";

    public static final String CONFIG_FILE_PATH = "PGETask_ConfigFilePath";
    
    public static final String LOG_FILE_PATTERN = "PGETask_LogFilePattern";

    public static final String PROPERTY_ADDER_CLASSPATH = "PGETask_PropertyAdderClasspath";

    public static final String PGE_RUNTIME = "PGETask_Runtime";
    
    /* PGE task statuses */
    public static final String STAGING_INPUT = "PGETask_Staging_Input";

    public static final String CONF_FILE_BUILD = "PGETask_Building_Config_File";

    public static final String RUNNING_PGE = "PGETask_Running";

    public static final String CRAWLING = "PGETask_Crawling";

}
