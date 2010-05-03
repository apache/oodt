//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.config;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class DataFilesInfo {

    private String queryMetadataElementName;

    private DownloadInfo di;

    public DataFilesInfo(String queryMetadataElementName, DownloadInfo di) {
        this.queryMetadataElementName = queryMetadataElementName;
        this.di = di;
    }

    public DownloadInfo getDownloadInfo() {
        return this.di;
    }

    public String getQueryMetadataElementName() {
        return this.queryMetadataElementName;
    }

}
