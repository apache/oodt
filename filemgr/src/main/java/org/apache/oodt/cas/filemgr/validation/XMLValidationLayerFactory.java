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

package org.apache.oodt.cas.filemgr.validation;

//JDk imports
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.io.DirectorySelector;


/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating XMLValidationLayer objects.
 * </p>
 * 
 */
public class XMLValidationLayerFactory implements ValidationLayerFactory {

    /*
     * list of dir uris specifying file paths to elements and product type map
     * directories
     */
    private List<String> dirList = null;
    
    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(XMLValidationLayerFactory.class.getName());

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public XMLValidationLayerFactory() {
        String dirUris = System
                .getProperty("org.apache.oodt.cas.filemgr.validation.dirs");
        
        // only returns true if org.apache.oodt.cas.filemgr.validation.dirs.recursive=true
        boolean recursive = Boolean.parseBoolean( 
        		System.getProperty("org.apache.oodt.cas.filemgr.validation.dirs.recursive") );

        if (dirUris != null) {
            dirUris = PathUtils.replaceEnvVariables(dirUris);
            String[] dirUriList = dirUris.split(",");
            
            // recursive directory listing
            if (recursive) {
            	
            	// empty list
            	dirList = new ArrayList<String>();
            	
            	// loop over specified root directories,
            	// add directories and sub-directories that contain both
            	// "elements.xml" and "product-type-element-map.xml"
            	for (String rootDir : dirUriList) {
            		try {
            			
            			DirectorySelector dirsel = new DirectorySelector(
            					Arrays.asList( 
            							new String[] {"product-type-element-map.xml", "elements.xml"} ));
            			dirList.addAll( dirsel.traverseDir(new File(new URI(rootDir))) );
            			
            		} catch (URISyntaxException e) {
            			LOG.log(Level.WARNING, "URISyntaxException when traversing directory: "+rootDir);
            		}
            	}        	

            // non-recursive directory listing
            } else {
            	dirList = Arrays.asList(dirUriList);
            }
            
            LOG.log(Level.FINE,"Collecting XML validation files from the following directories:");
            for (String pdir : dirList) {
            	LOG.log(Level.FINE, pdir);
            }
            
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayerFactory#createValidationLayer()
     */
    public ValidationLayer createValidationLayer() {
        return new XMLValidationLayer(dirList);
    }

}
