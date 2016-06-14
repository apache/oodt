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


package org.apache.oodt.cas.metadata.util;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//APACHE imports
import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;

/**
 * @author mattmann
 * @author bfoster
 * 
 * <p>
 * This is a facade class to insulate CAS Metadata from its underlying Mime Type
 * substrate library, <a href="http://tika.apache.org/">Apache Tika</a>.
 * Any mime handling code should be placed in this utility class, and hidden
 * from the CAS Metadata classes that rely on it.
 * </p>
 */
public final class MimeTypeUtils {

    private static final String SEPARATOR = ";";
    public static final int HEADER_BYTE_SIZE = 1024;

    /* our Tika mime type registry */
    private MimeTypes mimeTypes;

    private Tika tika;

    /* whether or not magic should be employed or not */
    private boolean mimeMagic;

    /* static resource path for the mimeTypesFile */
    public final static String MIME_FILE_RES_PATH = "tika-mimetypes.xml";

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(MimeTypeUtils.class
            .getName());

    public MimeTypeUtils() {
        this(MimeTypeUtils.class.getResourceAsStream(MIME_FILE_RES_PATH), true);
    }

    public MimeTypeUtils(String filePath) throws FileNotFoundException {
        this(filePath, true);
    }

    public MimeTypeUtils(String filePath, boolean magic)
            throws FileNotFoundException {
        this(new FileInputStream(filePath), magic);
    }

    public MimeTypeUtils(InputStream mimeIs, boolean magic) {
    	try {
    		this.mimeTypes = MimeTypesFactory.create(mimeIs);
    		this.mimeMagic = magic;
    		this.tika = new Tika(new DefaultDetector(this.mimeTypes));
    	}catch (Exception e) {
    		LOG.log(Level.SEVERE, "Failed to load MimeType Registry : " + e.getMessage(), e);
    	}
    }

    /**
     * Cleans a {@link MimeType} name by removing out the actual
     * {@link MimeType}, from a string of the form:
     * 
     * <pre>
     *           &lt;primary type&gt;/&lt;sub type&gt; ; &lt; optional params
     * </pre>
     * 
     * @param origType
     *            The original mime type string to be cleaned.
     * @return The primary type, and subtype, concatenated, e.g., the actual
     *         mime type.
     */
    public static String cleanMimeType(String origType) {
        if (origType == null) {
            return null;
        }

        // take the origType and split it on ';'
        String[] tokenizedMimeType = origType.split(SEPARATOR);
        if (tokenizedMimeType.length > 1) {
            // there was a ';' in there, take the first value
            return tokenizedMimeType[0];
        } else {
            // there wasn't a ';', so just return the orig type
            return origType;
        }
    }

    /**
     * Same as {@link #autoResolveContentType(String, String, byte[])}, but
     * this method passes <code>null</code> as the initial type.
     * 
     * @param url
     *            The String URL to use to check glob patterns.
     * @param data
     *            The byte data to potentially use in magic detection.
     * @return The String {@link MimeType}.
     */
    public String autoResolveContentType(String url, byte[] data) {
        return autoResolveContentType(null, url, data);
    }

    /**
     * A facade interface to trying all the possible mime type resolution
     * strategies available within Tika. First, the mime type provided in
     * <code>typeName</code> is cleaned, with {@link #cleanMimeType(String)}.
     * Then the cleaned mime type is looked up in the underlying Tika
     * {@link MimeTypes} registry, by its cleaned name. If the {@link MimeType}
     * is found, then that mime type is used, otherwise {@link URL} resolution
     * is used to try and determine the mime type. If that means is
     * unsuccessful, and if <code>mime.type.magic</code> is enabled in
     * {@link NutchConfiguration}, then mime type magic resolution is used to
     * try and obtain a better-than-the-default approximation of the
     * {@link MimeType}.
     * 
     * @param typeName
     *            The original mime type, returned from a {@link ProtocolOutput}.
     * @param url
     *            The given {@link URL}, that Nutch was trying to crawl.
     * @param data
     *            The byte data, returned from the crawl, if any.
     * @return The correctly, automatically guessed {@link MimeType} name.
     */
    public String autoResolveContentType(String typeName, String url,
            byte[] data) {
        MimeType type;
        String cleanedMimeType = null;

        try {
            cleanedMimeType = MimeTypeUtils.cleanMimeType(typeName) != null ? this.mimeTypes
                    .forName(MimeTypeUtils.cleanMimeType(typeName)).getName()
                    : null;
        } catch (MimeTypeException mte) {
            // Seems to be a malformed mime type name...
        }

        // first try to get the type from the cleaned type name
        try {
            type = cleanedMimeType != null ? this.mimeTypes
                    .forName(cleanedMimeType) : null;
        } catch (MimeTypeException e) {
            type = null;
        }

        // if returned null, or if it's the default type then try url resolution
        if (type == null
                || (type.getName().equals(MimeTypes.OCTET_STREAM))) {
            // If no mime-type header, or cannot find a corresponding registered
            // mime-type, then guess a mime-type from the url pattern
            try {
                type = mimeTypes.forName(tika.detect(url)) != null ? mimeTypes.forName(tika.detect(url)) : type;
            } catch (Exception e) {
                // MimeTypeException or IOException from tika.detect. Ignore.
            }
        }

        // if magic is enabled use mime magic to guess if the mime type returned
        // from the magic guess is different than the one that's already set so
        // far
        // if it is, and it's not the default mime type, then go with the mime
        // type
        // returned by the magic
        if (this.mimeMagic) {
            MimeType magicType;
            try {
                magicType =  mimeTypes.forName(tika.detect(data));
            } catch (Exception e) {
                magicType = null;
            }
            if (magicType != null
                    && !magicType.getName().equals(MimeTypes.OCTET_STREAM)
                    && type != null
                    && !type.getName().equals(magicType.getName())) {
                // If magic enabled and the current mime type differs from that
                // of the
                // one returned from the magic, take the magic mimeType
                type = magicType;
            }

            // if type is STILL null after all the resolution strategies, go for
            // the
            // default type
            if (type == null) {
                try {
                    type = this.mimeTypes.forName(MimeTypes.OCTET_STREAM);
                } catch (Exception ignore) {
                }
            }
        }

        return type != null ? type.getName() : null;
    }

    /**
     * Facade interface to Tika's underlying
     * {@link tika.detect(String)} method.
     *
     * @param url
     *            A string representation of the document {@link URL} to sense
     *            the {@link MimeType} for.
     * @return An appropriate {@link MimeType}, identified from the given
     *         Document url in string form.
     */
    public String getMimeType(URL url) {
        try {
    	    return tika.detect(url);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A facade interface to Tika's underlying {@link org.apache.tika.tika.detect(String)}
     * method.
     *
     * @param name
     *            The name of a valid {@link MimeType} in the Tika mime
     *            registry.
     * @return The object representation of the {@link MimeType}, if it exists,
     *         or null otherwise.
     */
    public String getMimeType(String name) {
        try {
            return tika.detect(name);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    /**
     * Facade interface to Tika's underlying {@link org.apache.tika.Tika#detect(File)}
     * method.
     *
     * @param f
     *            The {@link File} to sense the {@link MimeType} for.
     * @return The {@link MimeType} of the given {@link File}, or null if it
     *         cannot be determined.
     */
    public String getMimeType(File f) {
        try {
            return tika.detect(f);
        } catch (Exception e) {
            System.err.println("\n\n\n");
            LOG.log(Level.SEVERE, e.getMessage());
            System.err.println("\n\n\n");
            return null;
        }
    }

    /**
     * Utility method to act as a facade to
     * {@link MimeTypes#getMimeType(byte[])}.
     *
     * @param data
     *            The byte data to get the {@link MimeType} for.
     * @return The String representation of the resolved {@link MimeType}, or
     *         null if a suitable {@link MimeType} is not found.
     */
    public String getMimeTypeByMagic(byte[] data) {
        try {
            return tika.detect(data);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getDescriptionForMimeType(String mimeType) {
    	try {
    		return this.mimeTypes.forName(mimeType).getDescription();
    	}catch (Exception e) {
    		LOG.log(Level.WARNING, "Failed to get description for mimetype " 
    				+ mimeType + " : " + e.getMessage());
    		return null;
    	}
    }

    public String getSuperTypeForMimeType(String mimeType) {
    	try {
    		MediaType mediaType = this.mimeTypes.getMediaTypeRegistry().getSupertype(this.mimeTypes.forName(mimeType).getType());
    		if (mediaType != null) {
                return mediaType.getType() + "/" + mediaType.getSubtype();
            } else {
                return null;
            }
    	}catch (Exception e) {
    		LOG.log(Level.WARNING, "Failed to get super-type for mimetype " 
    				+ mimeType + " : " + e.getMessage());
    		return null;
    	}
    }
    
    /**
     * @return the mimeMagic
     */
    public boolean isMimeMagic() {
        return mimeMagic;
    }

    /**
     * @param mimeMagic the mimeMagic to set
     */
    public void setMimeMagic(boolean mimeMagic) {
        this.mimeMagic = mimeMagic;
    }
    
    public static byte[] readMagicHeader(InputStream stream) throws IOException {
    	return readMagicHeader(stream, HEADER_BYTE_SIZE);
    }
    
    public static byte[] readMagicHeader(InputStream stream, int headerByteSize) 
    		throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("InputStream is missing");
        }

        byte[] bytes = new byte[headerByteSize];
        int totalRead = 0;

        int lastRead = stream.read(bytes);
        while (lastRead != -1) {
            totalRead += lastRead;
            if (totalRead == bytes.length) {
                return bytes;
            }
            lastRead = stream.read(bytes, totalRead, bytes.length - totalRead);
        }

        byte[] shorter = new byte[totalRead];
        System.arraycopy(bytes, 0, shorter, 0, totalRead);
        return shorter;
    }

}
