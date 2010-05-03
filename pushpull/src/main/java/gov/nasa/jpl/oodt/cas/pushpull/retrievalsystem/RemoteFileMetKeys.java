//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.retrievalsystem;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met keys needed by the {@link RemoteFile}
 * </p>.
 */
public interface RemoteFileMetKeys {

    public static final String PRODUCT_NAME = "ProductName";

    public static final String RETRIEVED_FROM_LOC = "RetrievedFromLoc";

    public static final String FILENAME = "Filename";

    public static final String DATA_PROVIDER = "DataProvider";

    public static final String FILE_SIZE = "FileSize";

    public static final String RENAMING_STRING = "RenamingString";

    public static final String DOWNLOAD_TO_DIR = "DownloadToDir";

    public static final String PRODUCT_TYPE = "ProductType";

    public static final String SUPER_TYPE = "SuperType";

    public static final String DELETE_AFTER_DOWNLOAD = "DeleteAfterDownload";

}
