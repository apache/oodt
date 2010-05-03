/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.nasa.jpl.oodt.cas.filemgr.structs;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.mime.MimeType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.mime.MimeTypes;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A reference stores the original item reference, and also the item's data
 * store reference, which describes its location within the data store.
 * </p>
 * 
 */
public class Reference {

    /* the item's original location */
    private String origReference = null;

    /* the location of the item within the data store */
    private String dataStoreReference = null;

    /* the size of the file that this reference refers to */
    private long fileSize = 0L;

    /* the mime-type of the file that this reference refers to */
    private MimeType mimeType = null;

    private static MimeTypes mimeTypeRepository;

    /* the static reference to the Mime-Type repository */
    static {
        mimeTypeRepository = MimeTypes.buildRepository(PathUtils
                .replaceEnvVariables(System.getProperty(
                        "gov.nasa.jpl.oodt.cas.filemgr.mime.type.repository",
                        "mime-types.xml")));
    }

    /**
     * <p>
     * Copy Constructor
     * </p>
     * 
     * @param r
     *            The Reference object to copy
     */
    public Reference(Reference r) {
        this(r.getOrigReference(), r.getDataStoreReference(), r.getFileSize(),
                r.getMimeType());
    }

    /**
     * <p>
     * Default constructor
     * </p>
     */
    public Reference() {
        super();
    }

    /**
     * <p>
     * Constructs a new Reference with the specified parameters.
     * </p>
     * 
     * @param origRef
     *            The item's original location.
     * @param dataRef
     *            The item's location within the data store.
     * @param size
     *            The size of the file that this reference refers to.
     */
    public Reference(String origRef, String dataRef, long size) {
        origReference = origRef;
        dataStoreReference = dataRef;
        fileSize = size;
        // TODO: since no mimetype was specified, do the dirty work
        // ourselves to determine the which MimeType class to associate
        // with this reference.
        try {
            this.mimeType = mimeTypeRepository
                    .getMimeType(new URL(origRef));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>
     * Constructs a new Reference with the specified parameters. In particular,
     * a MimeType object is explicitly supplied. This object represents the
     * mime-type of the item this reference refers to
     * </p>
     * 
     * @param origRef
     *            The item's original location.
     * @param dataRef
     *            The item's location within the data store.
     * @param size
     *            The size of the file that this reference refers to.
     * @param mime
     *            A MimeType object representing the mime-type of the item
     */
    public Reference(String origRef, String dataRef, long size, MimeType mime) {
        origReference = origRef;
        dataStoreReference = dataRef;
        fileSize = size;
        mimeType = mime;
    }

    /**
     * @return Returns the dataStoreReference.
     */
    public String getDataStoreReference() {
        return dataStoreReference;
    }

    /**
     * @param dataStoreReference
     *            The dataStoreReference to set.
     */
    public void setDataStoreReference(String dataStoreReference) {
        this.dataStoreReference = dataStoreReference;
    }

    /**
     * @return Returns the origReference.
     */
    public String getOrigReference() {
        return origReference;
    }

    /**
     * @param origReference
     *            The origReference to set.
     */
    public void setOrigReference(String origReference) {
        this.origReference = origReference;
    }

    /**
     * @return Returns the fileSize.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize
     *            The fileSize to set.
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return returns a MimeType obj representing the mime-type of this
     *         reference
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * @param mime
     *            the MimeType object to set for this reference
     */
    public void setMimeType(MimeType mime) {
        this.mimeType = mime;
    }

    /**
     * @param name
     *            the String name of the mimetype of this reference
     */
    public void setMimeType(String name) {
        this.mimeType = mimeTypeRepository.forName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[orig=");
        buf.append(this.origReference);
        buf.append(",dest=");
        buf.append(this.dataStoreReference);
        buf.append(",size=");
        buf.append(this.fileSize);
        buf.append(",mime=");
        buf.append(this.mimeType != null ? this.mimeType.toString() : "N/A");
        buf.append("]");
        return buf.toString();
    }

}
